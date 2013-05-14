package org.knime.knip.core.ops.spotdetection;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.SeparableSymmetricConvolution;
import net.imglib2.converter.Converters;
import net.imglib2.display.RealFloatConverter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.ops.img.BinaryOperationAssignment;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.real.binary.RealSubtract;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Op to create a a trous wavelet decomposition of a 2D image as stack on the z
 * axis.
 * 
 * @author zinsmaie
 * 
 * @param <T>
 */
public class ATrousWaveletCreator<T extends RealType<T>>
                implements
                UnaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<FloatType>> {

        private final Integer[] m_skipLevels;

        public ATrousWaveletCreator() {
                m_skipLevels = new Integer[] {};
        }

        /**
         * @param skipLevels
         *                at these indices the output will only contain
         *                temporary results and not the wavelet plane. This can
         *                be used to speed up the computations if not all
         *                wavelet planes are required.
         */
        public ATrousWaveletCreator(final Integer... skipLevels) {
                m_skipLevels = skipLevels;
        }

        @Override
        public UnaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<FloatType>> copy() {
                return new ATrousWaveletCreator<T>(m_skipLevels);
        }

        /**
         * Computes a a trous wavelet representation with output.z - 1 wavelet
         * planes W_i and one residual plane A_z. Depending on the
         * {@link #m_skipLevels} parameter some of the planes may contain only
         * temporary results.<br>
         * <br>
         * The image can be recomputed as follows A_z + W_i | i � (0..z-1) <br>
         * sum(output_0 .. output_z-1) + output_z<br>
         * This works only if no skip levels are specified.
         */
        @Override
        public RandomAccessibleInterval<FloatType> compute(
                        final RandomAccessibleInterval<T> input,
                        final RandomAccessibleInterval<FloatType> output) {

                if (input.numDimensions() != 2 || output.numDimensions() != 3) {
                        throw new RuntimeException(
                                        new IncompatibleTypeException(input,
                                                        "input has to be a 2D image, output a 3D image"));
                }

                if (output.dimension(2) < 2) {
                        throw new RuntimeException(
                                        new IncompatibleTypeException(
                                                        input,
                                                        "output requires at least 2 XY planes i.e.  {[0..sizeX], [0..sizeY], [0..a] | a >= 1}"));
                }

                final long shortSize = Math.min(input.dimension(0),
                                input.dimension(1));
                if (shortSize < getMinSize(output.dimension(2) - 1)) {
                        throw new RuntimeException(
                                        "image to small (to many wavelet levels)");
                }

                try {
                        return createCoefficients(input, output);
                } catch (final IncompatibleTypeException e) {
                        throw new RuntimeException(
                                        "Separable Symmetric Convolution failed",
                                        e);
                }
        }

        private RandomAccessibleInterval<FloatType> createCoefficients(
                        final RandomAccessibleInterval<T> input2D,
                        final RandomAccessibleInterval<FloatType> outputStack)
                        throws IncompatibleTypeException {

                final long[] min = new long[] { input2D.min(0), input2D.min(1), -1 };
                final long[] max = new long[] { input2D.max(0), input2D.max(1), -1 };

                RandomAccessible<FloatType> extendedInput = Views
                                .extendMirrorDouble(Converters.convert(input2D,
                                                new RealFloatConverter<T>(),
                                                new FloatType()));

                // create output.z-1 wavelets
                for (int i = 0; i < (outputStack.dimension(2) - 1); i++) {
                        // select slice from output
                        min[2] = (i + 1);
                        max[2] = (i + 1);
                        final FinalInterval outputSlice = new FinalInterval(min, max);
                        //

                        final double[][] halfKernels = createHalfKernel(i);
                        SeparableSymmetricConvolution.convolve(halfKernels,
                                        extendedInput,
                                        SubsetOperations.subsetview(
                                                        outputStack,
                                                        outputSlice), 1);

                        extendedInput = Views
                                        .extendMirrorDouble(SubsetOperations
                                                        .subsetview(outputStack,
                                                                        outputSlice));

                }

                // now subtract the appropriate levels

                final BinaryOperationAssignment<FloatType, FloatType, FloatType> substract = new BinaryOperationAssignment<FloatType, FloatType, FloatType>(
                                new RealSubtract<FloatType, FloatType, FloatType>());
                IterableInterval<FloatType> in1 = Views.iterable(Converters
                                .convert(input2D, new RealFloatConverter<T>(),
                                                new FloatType()));

                for (int i = 0; i < (outputStack.dimension(2) - 1); i++) {
                        if (!Arrays.asList(m_skipLevels).contains(
                                        new Integer(i))) {
                                // out
                                min[2] = i;
                                max[2] = i;
                                final FinalInterval out = new FinalInterval(min, max);
                                // in1
                                min[2] = (i + 1);
                                max[2] = (i + 1);
                                final FinalInterval sub = new FinalInterval(min, max);
                                //
                                substract.compute(
                                                in1,
                                                Views.iterable(SubsetOperations
                                                                .subsetview(outputStack,
                                                                                sub)),
                                                Views.iterable(SubsetOperations
                                                                .subsetview(outputStack,
                                                                                out)));
                                //
                        }
                        min[2] = (i + 1);
                        max[2] = (i + 1);
                        final FinalInterval tmp = new FinalInterval(min, max);
                        in1 = Views.iterable(SubsetOperations.subsetview(
                                        outputStack, tmp));
                }

                return outputStack;
        }

        private double[][] createHalfKernel(final int level) {
                final double[][] ret = new double[2][];

                // kernel constants
                final float[] numbers = new float[] { 3.0f / 8.0f, 1.0f / 4.0f,
                                1.0f / 16.0f };

                final long zeroTap = (long) Math.pow(2.0, level) - 1;
                final int length = (int) ((zeroTap * 4) + 4) / 2 + 1;

                ret[0] = new double[length];
                ret[1] = new double[length];

                int index = 0;
                for (final float number : numbers) {
                        ret[0][index] = number;
                        ret[1][index] = number;
                        index += (zeroTap + 1);
                }

                return ret;
        }

        private long getMinSize(final long levels) {
                return 5 + (long) (Math.pow(2, levels - 1)) * 4;
        }
}
