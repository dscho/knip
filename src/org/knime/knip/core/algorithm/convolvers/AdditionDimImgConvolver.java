package org.knime.knip.core.algorithm.convolvers;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz (University of Konstanz)
 */
public class AdditionDimImgConvolver<T extends RealType<T>, K extends RealType<K>, O extends RealType<O> & NativeType<O>>
        implements MultiKernelConvolver<T, K, O> {

    private final Convolver<T, K, O> m_convolver;

    public AdditionDimImgConvolver(final Convolver<T, K, O> convolver) {
        m_convolver = convolver;
    }

    @Override
    public RandomAccessibleInterval<O> compute(final RandomAccessible<T> input,
                                               final RandomAccessibleInterval<K>[] kernels,
                                               final RandomAccessibleInterval<O> output) {

        final long[] min = new long[output.numDimensions()];
        output.min(min);
        final long[] max = new long[output.numDimensions()];
        output.max(max);
        for (int i = 0; i < kernels.length; i++) {
            max[max.length - 1] = i;
            min[min.length - 1] = i;
            m_convolver.compute(input, kernels[i], SubsetOperations.subsetview(output, new FinalInterval(min, max)));
        }

        return output;
    }

    @Override
    public AdditionDimImgConvolver<T, K, O> copy() {
        return new AdditionDimImgConvolver<T, K, O>((Convolver<T, K, O>)m_convolver.copy());
    }

}
