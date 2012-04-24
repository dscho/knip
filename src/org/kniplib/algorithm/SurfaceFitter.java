package org.kniplib.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.optimization.fitting.WeightedObservedPoint;
import org.kniplib.data.fit.ParametricBivariateRealFunction;
import org.kniplib.data.fit.WeightedObservedPoint2D;

/**
 * Fitter for parametric univariate real functions y = f(x).
 * <p>
 * When a univariate real function y = f(x) does depend on some unknown
 * parameters p<sub>0</sub>, p<sub>1</sub> ... p<sub>n-1</sub>, this class can
 * be used to find these parameters. It does this by <em>fitting</em> the curve
 * so it remains very close to a set of observed points (x<sub>0</sub>,
 * y<sub>0</sub>), (x<sub>1</sub>, y<sub>1</sub>) ... (x<sub>k-1</sub>,
 * y<sub>k-1</sub>). This fitting is done by finding the parameters values that
 * minimizes the objective function
 * &sum;(y<sub>i</sub>-f(x<sub>i</sub>))<sup>2</sup>. This is really a least
 * squares problem.
 * </p>
 * 
 * @version $Id: CurveFitter.java 1179928 2011-10-07 03:20:39Z psteitz $
 * @since 2.0
 */
public class SurfaceFitter {
        /** Optimizer to use for the fitting. */
        private final DifferentiableMultivariateVectorialOptimizer optimizer;

        /** Observed points. */
        private final List<WeightedObservedPoint2D> observations;

        /**
         * Simple constructor.
         * 
         * @param optimizer
         *                optimizer to use for the fitting
         */
        public SurfaceFitter(
                        final DifferentiableMultivariateVectorialOptimizer optimizer) {
                this.optimizer = optimizer;
                observations = new ArrayList<WeightedObservedPoint2D>();
        }

        /**
         * Add an observed (x,y) point to the sample with unit weight.
         * <p>
         * Calling this method is equivalent to call
         * {@code addObservedPoint(1.0, x, y)}.
         * </p>
         * 
         * @param x
         *                abscissa of the point
         * @param y
         *                observed value of the point at x, after fitting we
         *                should have f(x) as close as possible to this value
         * @see #addObservedPoint(double, double, double)
         * @see #addObservedPoint(WeightedObservedPoint)
         * @see #getObservations()
         */
        public void addObservedPoint(double x, double y, double v) {
                addObservedPoint(1.0, x, y, v);
        }

        /**
         * Add an observed weighted (x,y) point to the sample.
         * 
         * @param weight
         *                weight of the observed point in the fit
         * @param x
         *                abscissa of the point
         * @param y
         *                observed value of the point at x, after fitting we
         *                should have f(x) as close as possible to this value
         * @see #addObservedPoint(double, double)
         * @see #addObservedPoint(WeightedObservedPoint)
         * @see #getObservations()
         */
        public void addObservedPoint(double weight, double x, double y, double v) {
                observations.add(new WeightedObservedPoint2D(weight, x, y, v));
        }

        /**
         * Add an observed weighted (x,y) point to the sample.
         * 
         * @param observed
         *                observed point to add
         * @see #addObservedPoint(double, double)
         * @see #addObservedPoint(double, double, double)
         * @see #getObservations()
         */
        public void addObservedPoint(WeightedObservedPoint2D observed) {
                observations.add(observed);
        }

        /**
         * Get the observed points.
         * 
         * @return observed points
         * @see #addObservedPoint(double, double)
         * @see #addObservedPoint(double, double, double)
         * @see #addObservedPoint(WeightedObservedPoint)
         */
        public WeightedObservedPoint2D[] getObservations() {
                return observations
                                .toArray(new WeightedObservedPoint2D[observations
                                                .size()]);
        }

        /**
         * Remove all observations.
         */
        public void clearObservations() {
                observations.clear();
        }

        /**
         * Fit a curve. This method compute the coefficients of the curve that
         * best fit the sample of observed points previously given through calls
         * to the {@link #addObservedPoint(WeightedObservedPoint)
         * addObservedPoint} method.
         * 
         * @param f
         *                parametric function to fit.
         * @param initialGuess
         *                first guess of the function parameters.
         * @return the fitted parameters.
         * @throws IllegalArgumentException
         */
        public double[] fit(final ParametricBivariateRealFunction f,
                        final double[] initialGuess)
                        throws IllegalArgumentException {
                return fit(Integer.MAX_VALUE, f, initialGuess);
        }

        /**
         * Fit a curve. This method compute the coefficients of the curve that
         * best fit the sample of observed points previously given through calls
         * to the {@link #addObservedPoint(WeightedObservedPoint)
         * addObservedPoint} method.
         * 
         * @param f
         *                parametric function to fit.
         * @param initialGuess
         *                first guess of the function parameters.
         * @param maxEval
         *                Maximum number of function evaluations.
         * @return the fitted parameters.
         * @throws IllegalArgumentException
         * @since 3.0
         */
        public double[] fit(int maxEval,
                        final ParametricBivariateRealFunction f,
                        final double[] initialGuess)
                        throws IllegalArgumentException {
                // prepare least squares problem
                double[] target = new double[observations.size()];
                double[] weights = new double[observations.size()];
                int i = 0;
                for (WeightedObservedPoint2D point : observations) {
                        target[i] = point.getV();
                        weights[i] = point.getWeight();
                        ++i;
                }

                /*
                 * // perform the fit VectorialPointValuePair optimum =
                 * optimizer .optimize(new TheoreticalValuesFunction(f), target,
                 * weights, initialGuess);
                 */
                // perform the fit
                VectorialPointValuePair optimum = optimizer.optimize(maxEval,
                                new TheoreticalValuesFunction(f), target,
                                weights, initialGuess);

                // extract the coefficients
                return optimum.getPointRef();
        }

        /** Vectorial function computing function theoretical values. */
        private class TheoreticalValuesFunction implements
                        DifferentiableMultivariateVectorialFunction {
                /** Function to fit. */
                private final ParametricBivariateRealFunction f;

                /**
                 * Simple constructor.
                 * 
                 * @param f
                 *                function to fit.
                 */
                public TheoreticalValuesFunction(
                                final ParametricBivariateRealFunction f) {
                        this.f = f;
                }

                /** {@inheritDoc} */
                public MultivariateMatrixFunction jacobian() {
                        return new MultivariateMatrixFunction() {
                                public double[][] value(double[] point) {
                                        final double[][] jacobian = new double[observations
                                                        .size()][];

                                        int i = 0;
                                        for (WeightedObservedPoint2D observed : observations) {
                                                jacobian[i++] = f
                                                                .gradient(observed
                                                                                .getX(),
                                                                                observed.getY(),
                                                                                point);
                                        }

                                        return jacobian;
                                }
                        };
                }

                /** {@inheritDoc} */
                public double[] value(double[] point) {
                        // compute the residuals
                        final double[] values = new double[observations.size()];
                        int i = 0;
                        for (WeightedObservedPoint2D observed : observations) {
                                values[i++] = f.value(observed.getX(),
                                                observed.getY(), point);
                        }

                        return values;
                }
        }
}
