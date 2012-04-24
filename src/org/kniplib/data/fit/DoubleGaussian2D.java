package org.kniplib.data.fit;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

public class DoubleGaussian2D<T extends RealType<T>> extends
                Fitter2DFunction<T> {
        private final int m_noParameters = 12;

        /**
         * For now, use the parameters A, x0, y0, s_x, s_y (in that order)
         */
        @Override
        public double value(double x, double y, double... parameters) {
                double s1_A = parameters[0];
                double s1_a = parameters[1];
                double s1_b = parameters[2];
                double s1_c = parameters[3];
                double s1_x0 = parameters[4];
                double s1_y0 = parameters[5];

                double s2_A = parameters[6];
                double s2_a = parameters[7];
                double s2_b = parameters[8];
                double s2_c = parameters[9];
                double s2_x0 = parameters[10];
                double s2_y0 = parameters[11];

                return s1_A
                                * Math.exp(-(s1_a * (x - s1_x0) * (x - s1_x0)
                                                + 2 * s1_b * (x - s1_x0)
                                                * (y - s1_y0) + s1_c
                                                * (y - s1_y0) * (y - s1_y0)))
                                + s2_A
                                * Math.exp(-(s2_a * (x - s2_x0) * (x - s2_x0)
                                                + 2 * s2_b * (x - s2_x0)
                                                * (y - s2_y0) + s2_c
                                                * (y - s2_y0) * (y - s2_y0)));
        }

        @Override
        public double[] gradient(double x, double y, double... parameters) {
                double s1_A = parameters[0];
                double s1_a = parameters[1];
                double s1_b = parameters[2];
                double s1_c = parameters[3];
                double s1_x0 = parameters[4];
                double s1_y0 = parameters[5];

                double s2_A = parameters[6];
                double s2_a = parameters[7];
                double s2_b = parameters[8];
                double s2_c = parameters[9];
                double s2_x0 = parameters[10];
                double s2_y0 = parameters[11];

                double[] ret = new double[12];

                double e1 = Math.exp(-(s1_a * (x - s1_x0) * (x - s1_x0) + 2
                                * s1_b * (x - s1_x0) * (y - s1_y0) + s1_c
                                * (y - s1_y0) * (y - s1_y0)));
                double e2 = Math.exp(-(s2_a * (x - s2_x0) * (x - s2_x0) + 2
                                * s2_b * (x - s2_x0) * (y - s2_y0) + s2_c
                                * (y - s2_y0) * (y - s2_y0)));

                // d/dA
                ret[0] = e1;
                // d/da
                ret[1] = -(x - s1_x0) * (x - s1_x0) * s1_A * e1;
                // d/db
                ret[2] = -2 * (x - s1_x0) * (y - s1_y0) * s1_A * e1;
                // d/dc
                ret[3] = -(y - s1_y0) * (y - s1_y0) * s1_A * e1;
                // d/dx0
                ret[4] = 2 * (s1_a * (x - s1_x0) + s1_b * (y - s1_y0)) * s1_A
                                * e1;
                // d/dy0
                ret[5] = 2 * (s1_c * (y - s1_y0) + s1_b * (x - s1_x0)) * s1_A
                                * e1;

                // d/dA
                ret[6] = e2;
                // d/da
                ret[7] = -(x - s2_x0) * (x - s2_x0) * s2_A * e2;
                // d/db
                ret[8] = -2 * (x - s2_x0) * (y - s2_y0) * s2_A * e2;
                // d/dc
                ret[9] = -(y - s2_y0) * (y - s2_y0) * s2_A * e2;
                // d/dx0
                ret[10] = 2 * (s2_a * (x - s2_x0) + s2_b * (y - s2_y0)) * s2_A
                                * e2;
                // d/dy0
                ret[11] = 2 * (s2_c * (y - s2_y0) + s2_b * (x - s2_x0)) * s2_A
                                * e2;

                return ret;
        }

        @Override
        public double[] init(IterableInterval<T> ii) {
                // TODO always use the first two dimensions...
                long x = (ii.max(0) + ii.min(0)) / 2;
                long y = (ii.max(1) + ii.min(1)) / 2;

                double[] pars = new double[m_noParameters];

                pars[0] = ii.firstElement().getMaxValue();
                pars[1] = 0.5;
                pars[2] = 0.0;
                pars[3] = 0.5;
                pars[4] = x;
                pars[5] = y;
                pars[6] = ii.firstElement().getMaxValue();
                pars[7] = 0.5;
                pars[8] = 0.0;
                pars[9] = 0.5;
                pars[10] = x;
                pars[11] = y;

                return pars;
        }

        @Override
        int noParameters() {
                return m_noParameters;
        }

}
