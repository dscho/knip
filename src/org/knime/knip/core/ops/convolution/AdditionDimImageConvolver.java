package org.knime.knip.core.ops.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz (University of Konstanz)
 */
public class AdditionDimImageConvolver<T extends RealType<T>, O extends RealType<O> & NativeType<O>, K extends RealType<K>>
                implements MultiKernelConvolver<T, O, K> {

        private final Convolver<T, O, K> m_convolver;

        public AdditionDimImageConvolver(Convolver<T, O, K> convolver) {
                m_convolver = convolver;
        }

        @Override
        public RandomAccessibleInterval<O> compute(RandomAccessible<T> input,
                        RandomAccessibleInterval<K>[] kernels,
                        RandomAccessibleInterval<O> output) {

                final long[] min = new long[output.numDimensions()];
                output.min(min);
                final long[] max = new long[output.numDimensions()];
                output.max(max);
                for (int i = 0; i < kernels.length; i++) {
                        max[max.length - 1] = i;
                        min[min.length - 1] = i;
                        m_convolver.compute(input, kernels[i], SubsetViews
                                        .iterableSubsetView(output,
                                                        new FinalInterval(min,
                                                                        max)));
                }

                return output;
        }

        @Override
        public AdditionDimImageConvolver<T, O, K> copy() {
                return new AdditionDimImageConvolver<T, O, K>(
                                (Convolver<T, O, K>) m_convolver.copy());
        }

        public static <T extends RealType<T>, O extends RealType<O> & NativeType<O>> Img<O> createEmptyOutput(
                        Img<T> in, int numKernels, O resType) {
                ImgFactory<O> imgFactory;
                try {
                        imgFactory = in.factory().imgFactory(resType);
                } catch (IncompatibleTypeException e) {
                        imgFactory = new ArrayImgFactory<O>();
                }
                final long[] dims = new long[in.numDimensions() + 1];
                for (int d = 0; d < in.numDimensions(); d++)
                        dims[d] = in.dimension(d);
                dims[dims.length - 1] = numKernels;
                return imgFactory.create(dims, resType);
        }
}
