package org.knime.knip.core.ops.spotdetection.icybased;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/*
 * This OP uses the two classes B3SplineUDWT and WaveletConfigException from icy 
 */

/**
 * Op to create a a trous wavelet decomposition of a 2D image as stack on the z
 * axis.
 * 
 * @author zinsmaie
 * 
 * @param <T>
 */
public class ATrousWaveletCreatorIcyBased<T extends RealType<T>>
                implements
                UnaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<FloatType>> {

        private final Integer[] m_skipLevels;

        public ATrousWaveletCreatorIcyBased() {
                m_skipLevels = new Integer[] {};
        }

        /**
         * @param skipLevels
         *                at these indices the output will only contain
         *                temporary results and not the wavelet plane. This can
         *                be used to speed up the computations if not all
         *                wavelet planes are required.
         */
        public ATrousWaveletCreatorIcyBased(final Integer... skipLevels) {
                m_skipLevels = skipLevels;
        }

        @Override
        public UnaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<FloatType>> copy() {
                return new ATrousWaveletCreatorIcyBased<T>(m_skipLevels);
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

                return createCoefficients(input, output);
        }

        /**
         * uses B3SplineUDWT from icy to generate the wavelet coefficients.
         * 
         * @param input2D
         *                a 2d image slice
         * @return RandomAccessibleInterval with x,y dimension according to the
         *         input and i+1 slices in dimension unknown holding i wavelet
         *         coefficients and the residual level.
         */
        private RandomAccessibleInterval<FloatType> createCoefficients(
                        final RandomAccessibleInterval<T> input2D,
                        final RandomAccessibleInterval<FloatType> outputStack) {

                // make it 1D
                final int sizeX = (int) input2D.dimension(0);
                final int sizeY = (int) input2D.dimension(1);

                final float dataIn[] = new float[sizeX * sizeY];
                final Cursor<T> curso = Views.flatIterable(input2D).cursor();

                int k = 0;
                while (curso.hasNext()) {
                        dataIn[k] = curso.next().getRealFloat();
                        k++;
                }

                // decompose the image
                final B3SplineUDWT waveletTransform = new B3SplineUDWT();

                float[][] scales = null;
                try {
                        scales = waveletTransform.b3WaveletScales2D(dataIn,
                                        sizeX, sizeY,
                                        (int) (outputStack.dimension(2) - 1));
                } catch (final WaveletConfigException e1) {
                        e1.printStackTrace();
                }

                // calculate coefficients (wavelet scaleX - wavelet scaleY)
                final float[][] coefficients = waveletTransform
                                .b3WaveletCoefficients2D(
                                                scales,
                                                dataIn,
                                                (int) (outputStack.dimension(2) - 1),
                                                sizeX * sizeY);

                // write it back into an image
                final RandomAccess<FloatType> ra = outputStack.randomAccess();

                for (int i = 0; i < coefficients.length; i++) {
                        for (int j = 0; j < coefficients[0].length; j++) {

                                final int x = j % sizeX;
                                final int y = j / sizeX;

                                ra.setPosition(new int[] { x, y, i });
                                ra.get().setReal(coefficients[i][j]);
                        }
                }

                return outputStack;
        }

        private long getMinSize(final long levels) {
                return 5 + (long) (Math.pow(2, levels - 1)) * 4;
        }
}