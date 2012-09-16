package org.knime.knip.core.ops.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.subset.views.ImgView;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz (University of Konstanz)
 */
public class AdditionDimImageConvolver<T extends RealType<T>, O extends RealType<O> & NativeType<O>, K extends RealType<K>>
                implements MultiKernelImageConvolver<T, O, K> {

        private final O m_resType;

        private final Convolver<T, O, K, Img<T>, Img<O>, Img<K>> m_convolver;

        public AdditionDimImageConvolver(
                        Convolver<T, O, K, Img<T>, Img<O>, Img<K>> convolver,
                        O resType) {
                m_convolver = convolver;
                m_resType = resType;
        }

        @Override
        public Img<O> compute(Img<T> input, Img<K>[] kernels, Img<O> output) {

                final long[] min = new long[output.numDimensions()];
                output.min(min);
                final long[] max = new long[output.numDimensions()];
                output.max(max);
                for (int i = 0; i < kernels.length; i++) {
                        max[max.length - 1] = i;
                        min[min.length - 1] = i;
                        m_convolver.compute(
                                        input,
                                        kernels[i],
                                        new ImgView<O>(
                                                        SubsetViews.iterableSubsetView(
                                                                        output,
                                                                        new FinalInterval(
                                                                                        min,
                                                                                        max)),
                                                        output.factory()));
                }

                return output;
        }

        @Override
        public Img<O> compute(Img<T> in, Img<K>[] kernels) {
                return compute(in, kernels, createEmptyOutput(in, kernels));
        }

        @Override
        public Img<O> createEmptyOutput(Img<T> in, Img<K>[] kernels) {
                ImgFactory<O> imgFactory;
                try {
                        imgFactory = in.factory().imgFactory(m_resType);
                } catch (IncompatibleTypeException e) {
                        imgFactory = new ArrayImgFactory<O>();
                }
                        final long[] dims = new long[in.numDimensions() + 1];
                        for (int d = 0; d < in.numDimensions(); d++)
                                dims[d] = in.dimension(d);
                        dims[dims.length - 1] = kernels.length;
                        return imgFactory.create(dims, m_resType);
        }

        @SuppressWarnings("unchecked")
        @Override
        public AdditionDimImageConvolver<T, O, K> copy() {
                return new AdditionDimImageConvolver<T, O, K>(
                                (Convolver<T, O, K, Img<T>, Img<O>, Img<K>>) m_convolver
                                                .copy(), m_resType);
        }

}
