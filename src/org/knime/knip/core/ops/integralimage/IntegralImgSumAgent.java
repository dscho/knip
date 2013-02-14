package org.knime.knip.core.ops.integralimage;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

/**
 * Helper object for {@link IntegralImgND} that executes efficient region sum
 * queries on the integral image.
 *
 * @author zinsmaie
 *
 * @param <T>
 */
public class IntegralImgSumAgent<T extends RealType<T>> {

        private final boolean[][] m_binaryRep;
        private final int[] m_signs;
        private final RandomAccess<T> m_iiRA;
        private final int m_dims;
        private final int m_points;

        /**
         * Initializes member variables that are needed for the nd sum
         * calculation.
         *
         * @param ii
         */
        public IntegralImgSumAgent(RandomAccessibleInterval<T> ii) {
                m_iiRA = ii.randomAccess();

                // initialize the binary representation of the control points
                // (choose value from leftUpper (false) or rightLower (true))
                // and the respective signs for the points

                m_dims = ii.numDimensions();
                m_points = (int) Math.pow(2, m_dims);

                m_binaryRep = new boolean[m_points][m_dims];
                m_signs = new int[m_points];

                for (int i = 0; i < m_points; i++) {
                        m_binaryRep[i] = getBinaryRep(i, m_dims);

                        int ones = 0;
                        for (int j = 0; j < m_dims; j++) {
                                if (m_binaryRep[i][j]) {
                                        ones++;
                                }
                        }

                        m_signs[i] = (int) Math.pow(-1, m_dims - ones);
                }
        }

        /**
         * get the sum of the area including leftUpper and rightLower corner.
         * The positions are defined with respect to the original input image of
         * the integral image. Therefore you can safely ignore the helper zeros
         * of the integral image.
         *
         * @param leftUpper
         * @param rightLower
         * @return
         */
        public double getSum(long[] leftUpper, long[] rightLower) {

                // implemented according to
                // http://en.wikipedia.org/wiki/Summed_area_table high
                // dimensional variant

                long[] position = new long[m_dims];
                double sum = 0;

                for (int i = 0; i < m_points; i++) {
                        for (int j = 0; j < m_dims; j++) {
                                if (m_binaryRep[i][j]) { // = 1
                                        // +1 because the integral image
                                        // contains a zero column
                                        position[j] = rightLower[j] + 1l;
                                } else { // = 0
                                         // no +1 because integrating from 3..5
                                         // inc. 3 & 5 means [5] - [2]
                                        position[j] = leftUpper[j];
                                }
                        }

                        m_iiRA.setPosition(position);
                        sum += m_signs[i] * m_iiRA.get().getRealDouble();
                }

                return sum;
        }

        // gives as {0,1}^d all binary combinations 0,0,..,0 ...
        // 1,1,...,1
        private boolean[] getBinaryRep(int i, int d) {
                char[] tmp = Long.toBinaryString(i).toCharArray();
                boolean[] p = new boolean[d];
                for (int pos = 0; pos < tmp.length; pos++) {
                        if (tmp[pos] == '1') {
                                p[tmp.length - (pos + 1)] = true;
                        }
                }

                return p;
        }
}
