package org.knime.knip.core.ops.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;

/**
 * Convolution, using ImgLib2Fourier implementation
 *
 * @author Christian Dietz (University of Konstanz)
 */
public class ImgLib2FourierConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                implements Convolver<T, O, K> {

        private RandomAccessible<T> m_lastImg = null;

        private KNIPFFTConvolution<T, O, K> m_fc = null;

        // Empty constructor for extension point
        public ImgLib2FourierConvolver() {
        }

        @Override
        public ImgLib2FourierConvolver<T, O, K> copy() {
                return new ImgLib2FourierConvolver<T, O, K>();
        }

        @Override
        public RandomAccessibleInterval<O> compute(
                        final RandomAccessible<T> in,
                        final RandomAccessibleInterval<K> kernel,
                        final RandomAccessibleInterval<O> out) {

                if (in.numDimensions() != kernel.numDimensions()) {
                        throw new IllegalStateException(
                                        "Kernel dimensions do not match to Img dimensions in ImgLibImageConvolver!");
                }

                if (m_lastImg != in) {
                        m_lastImg = new TmpWrapper<T>(in, out, kernel);
                        m_fc = new KNIPFFTConvolution<T, O, K>(m_lastImg, out,
                                        kernel, kernel, out,
                                        new ArrayImgFactory<ComplexFloatType>());
                        m_fc.setKernel(kernel);
                        m_fc.setKeepImgFFT(true);
                } else {
                        m_fc.setKernel(kernel);
                        m_fc.setOutput(out);
                }

                m_fc.run();

                return out;
        }

        // Workaround
        private class TmpWrapper<TT extends RealType<TT>> implements
                        RandomAccessible<TT> {

                private final RandomAccessible<TT> m_accessible;
                private final FinalInterval m_finalInterval;

                public TmpWrapper(RandomAccessible<TT> accessible,
                                Interval dims, Interval kernel) {
                        // TODO: Workaround until fix in imglib2 (outofbounds
                        // gets lost
                        // during optimization of transformation)
                        long[] min = new long[accessible.numDimensions()];
                        long[] max = new long[accessible.numDimensions()];

                        for (int d = 0; d < kernel.numDimensions(); d++) {
                                min[d] = -kernel.dimension(d);
                                max[d] = kernel.dimension(d)
                                                + dims.dimension(d);
                        }

                        m_accessible = accessible;
                        m_finalInterval = new FinalInterval(min, max);
                }

                @Override
                public RandomAccess<TT> randomAccess() {
                        return m_accessible.randomAccess(m_finalInterval);
                }

                @Override
                public int numDimensions() {
                        return m_accessible.numDimensions();
                }

                @Override
                public RandomAccess<TT> randomAccess(Interval interval) {
                        return m_accessible.randomAccess(interval);
                };

        }

}
