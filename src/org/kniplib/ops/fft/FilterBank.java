package org.kniplib.ops.fft;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.ops.operation.unary.img.CopyImgOperation;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ops.img.IterableIntervalCopy;

/**
 * Perform multiple convolution operations and projects the results into one
 * image.
 *
 * @author schoenen
 *
 * @param <T>
 * @param <K>
 */
public class FilterBank<T extends RealType<T>, K extends RealType<K>>
                implements UnaryOutputOperation<Img<T>, Img<T>> {

        private final ImageConvolution<T, K, Img<T>, Img<K>> m_conv;

        private final Img<K>[] m_kernels;

        /**
         *
         * @param conv
         *                ImageConvolution implementation.
         * @param op
         *                Projection operation.
         * @param kernels
         *                Convolution kernels.
         */
        public FilterBank(final ImageConvolution<T, K, Img<T>, Img<K>> conv,
                        final Img<K>... kernels) {
                m_conv = conv;
                m_kernels = kernels;
        }

        @Override
        public final Img<T> createEmptyOutput(final Img<T> op) {
                if (m_kernels.length > 1) {
                        final long[] dims = new long[op.numDimensions() + 1];
                        for (int d = 0; d < op.numDimensions(); d++) {
                                dims[d] = op.dimension(d);
                        }
                        dims[dims.length - 1] = m_kernels.length;
                        return op.factory().create(dims,
                                        op.firstElement().createVariable());
                }
                return op.factory().create(op,
                                op.firstElement().createVariable());
        }

        @Override
        public final Img<T> compute(final Img<T> op, final Img<T> r) {
                switch (m_kernels.length) {
                case 0:
                        new IterableIntervalCopy<T>().compute(op, r);
                        return r;
                case 1:
                        m_conv.setKernel(m_kernels[0]);
                        if (m_conv instanceof DirectImageConvolution)
                                m_conv.compute(op, r);
                        else if (m_conv instanceof ImgLibImageConvolution) {
                                m_conv.compute(op);
                                new CopyImgOperation<T>().compute(op, r);
                        }
                        return r;
                default:
                        final long[] min = new long[r.numDimensions()];
                        r.min(min);
                        final long[] max = new long[r.numDimensions()];
                        r.max(max);
                        for (int i = 0; i < m_kernels.length; i++) {
                                max[max.length - 1] = i;
                                min[min.length - 1] = i;
                                final SubImg<T> subimg = new SubImg<T>(r,
                                                new FinalInterval(min, max),
                                                false);

                                m_conv.setKernel(m_kernels[i]);

                                // TODO: Hack until fourier is a op
                                if (m_conv instanceof DirectImageConvolution)
                                        m_conv.compute(op, subimg);
                                else if (m_conv instanceof ImgLibImageConvolution) {
                                        m_conv.compute(op);
                                        new CopyImgOperation<T>().compute(op,
                                                        subimg);
                                }

                        }
                        return r;
                }
        }

        /**
         * Helper method
         */
        @Override
        public Img<T> compute(final Img<T> in) {
                return compute(in, createEmptyOutput(in));
        }

        @Override
        public UnaryOutputOperation<Img<T>, Img<T>> copy() {
                return new FilterBank<T, K>(
                                (ImageConvolution<T, K, Img<T>, Img<K>>) m_conv
                                                .copy(),
                                Arrays.copyOf(m_kernels, m_kernels.length));
        }
}
