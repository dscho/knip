package org.kniplib.data.fit;

import java.io.Serializable;

import org.kniplib.algorithm.SurfaceFitter;

/**
 * This class is a simple container for weighted observed point in
 * {@link SurfaceFitter curve fitting}.
 * <p>
 * Instances of this class are guaranteed to be immutable.
 * </p>
 * 
 * @version $Id: WeightedObservedPoint.java 1131229 2011-06-03 20:49:25Z luc $
 * @since 2.0
 */
public class WeightedObservedPoint2D implements Serializable {

        /** Serializable version id. */
        private static final long serialVersionUID = 1L;

        /** Weight of the measurement in the fitting process. */
        private final double weight;

        /** Abscissa of the point. */
        private final double x;

        /** Ordinate of the point. */
        private final double y;

        /** Observed value at x,y */
        private final double v;

        /**
         * Simple constructor.
         * 
         * @param weight
         *                weight of the measurement in the fitting process
         * @param x
         *                abscissa of the measurement
         * @param y
         *                ordinate of the measurement
         * @param v
         *                value
         */
        public WeightedObservedPoint2D(final double weight, final double x,
                        final double y, final double v) {
                this.weight = weight;
                this.x = x;
                this.y = y;
                this.v = v;
        }

        /**
         * Get the weight of the measurement in the fitting process.
         * 
         * @return weight of the measurement in the fitting process
         */
        public double getWeight() {
                return weight;
        }

        /**
         * Get the abscissa of the point.
         * 
         * @return abscissa of the point
         */
        public double getX() {
                return x;
        }

        /**
         * Get the ordinate of the point.
         * 
         * @return ordinate of the point
         */
        public double getY() {
                return y;
        }

        /**
         * Get the observed value of the function at x,y.
         * 
         * @return observed value of the function at x,y
         */
        public double getV() {
                return v;
        }

}
