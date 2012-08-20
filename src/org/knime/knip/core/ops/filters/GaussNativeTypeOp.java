package org.knime.knip.core.ops.filters;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gauss3.SeparableSymmetricConvolution;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class GaussNativeTypeOp<T extends RealType<T> & NativeType<T>, TYPE extends RandomAccessibleInterval<T>>
                implements UnaryOperation<TYPE, TYPE> {

        private final double[] m_sigmas;
        private final OutOfBoundsFactory<T, TYPE> m_fac;
        private final int m_numThreads;

        public GaussNativeTypeOp(int numThreads, double[] sigmas,
                        OutOfBoundsFactory<T, TYPE> factory) {
                m_sigmas = sigmas;
                m_fac = factory;
                m_numThreads = 1;
        }

        @SuppressWarnings("unchecked")
        @Override
        public TYPE compute(final TYPE input, TYPE output) {

                if (m_sigmas.length != input.numDimensions()) {
                        throw new IllegalArgumentException(
                                        "Size of sigma array doesn't fit to input image");
                }

                final RandomAccessible<FloatType> rIn = (RandomAccessible<FloatType>) Views
                                .extend(input, m_fac);

                try {
                        SeparableSymmetricConvolution.convolve(
                                        Gauss3.halfkernels(m_sigmas), rIn,
                                        output, 1);

                } catch (IncompatibleTypeException e) {
                        throw new RuntimeException(e);
                }

                return output;
        }

        @Override
        public UnaryOperation<TYPE, TYPE> copy() {
                return new GaussNativeTypeOp<T, TYPE>(m_numThreads,
                                m_sigmas.clone(), m_fac);
        }

}
