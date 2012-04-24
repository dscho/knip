package org.kniplib.data.fit;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

public class Gaussian2D<T extends RealType<T>> extends Fitter2DFunction<T> {
        private final int m_noParameters = 6;

        /**
         * For now, use the parameters A, x0, y0, s_x, s_y (in that order)
         */
        @Override
        public double value(double x, double y, double... parameters) {
                double A = parameters[0];
                double a = parameters[1];
                double b = parameters[2];
                double c = parameters[3];
                double x0 = parameters[4];
                double y0 = parameters[5];
                return A
                                * Math.exp(-(a * (x - x0) * (x - x0) + 2 * b
                                                * (x - x0) * (y - y0) + c
                                                * (y - y0) * (y - y0)));
        }

        @Override
        public double[] gradient(double x, double y, double... parameters) {
                double A = parameters[0];
                double a = parameters[1];
                double b = parameters[2];
                double c = parameters[3];
                double x0 = parameters[4];
                double y0 = parameters[5];
                double[] ret = new double[6];

                double e = Math.exp(-(a * (x - x0) * (x - x0) + 2 * b
                                * (x - x0) * (y - y0) + c * (y - y0) * (y - y0)));

                // d/dA
                ret[0] = e;
                // d/da
                ret[1] = -(x - x0) * (x - x0) * A * e;
                // d/db
                ret[2] = -2 * (x - x0) * (y - y0) * A * e;
                // d/dc
                ret[3] = -(y - y0) * (y - y0) * A * e;
                // d/dx0
                ret[4] = 2 * (a * (x - x0) + b * (y - y0)) * A * e;
                // d/dy0
                ret[5] = 2 * (c * (y - y0) + b * (x - x0)) * A * e;

                return ret;
        }

        @Override
        public double[] init(IterableInterval<T> ii) {
                // TODO always use the first two dimensions...
                long x = (ii.max(0) + ii.min(0)) / 2; // .dimension(0);
                long y = (ii.max(1) + ii.min(1)) / 2; // dimension(1);

                double[] pars = new double[m_noParameters];

                pars[0] = ii.firstElement().getMaxValue();
                pars[1] = 0.5;
                pars[2] = 0.0;
                pars[3] = 0.5;
                pars[4] = x;
                pars[5] = y;

                return pars;
        }

        @Override
        int noParameters() {
                return m_noParameters;
        }

}
