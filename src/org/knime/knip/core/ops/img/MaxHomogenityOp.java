package org.knime.knip.core.ops.img;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.ops.operation.iterable.unary.Variance;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.roi.PolygonRegionOfInterest;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

//TODO: Use circle instead of rectangle??
//TODO: Input: RandomAccessibleInterval Output: IterableInterval
public class MaxHomogenityOp<T extends RealType<T>, I extends RandomAccessibleInterval<T>>
                implements UnaryOperation<I, I> {

        private final long[] m_span;
        private final double m_lambda;
        private final OutOfBoundsFactory<T, I> m_outofbounds;

        public MaxHomogenityOp(final double lambda, final long[] span,
                        final OutOfBoundsFactory<T, I> outofbounds) {
                m_span = span;
                m_lambda = lambda;
                m_outofbounds = outofbounds;

        }

        @Override
        public I compute(final I input, final I output) {

                final IterableInterval<T> inputIterable = Views.iterable(input);
                final PolygonRegionOfInterest[] rois = createROIs(inputIterable
                                .firstElement().createVariable(), m_span);

                final double[] displacement = new double[input.numDimensions()];
                final double[] position = new double[input.numDimensions()];

                final Cursor<T> cursor = inputIterable.cursor();
                final Cursor<T> outCursor = Views.iterable(output).cursor();
                while (cursor.hasNext()) {
                        cursor.fwd();
                        outCursor.fwd();
                        cursor.localize(position);

                        final double[] means = new double[rois.length];
                        final double[] stddevs = new double[rois.length];
                        double minStdDev = Double.MAX_VALUE;

                        for (int d = 0; d < displacement.length; d++) {
                                displacement[d] = position[d] - displacement[d];
                        }

                        // Can be done more nicely? dont know
                        int r = 0;
                        for (final PolygonRegionOfInterest roi : rois) {
                                roi.move(displacement);
                                // CODE START
                                final Cursor<T> roiCursor = roi
                                                .getIterableIntervalOverROI(
                                                                Views.extend(input,
                                                                                m_outofbounds))
                                                .cursor();

                                means[r] = new Mean<T, DoubleType>().compute(
                                                roiCursor, new DoubleType())
                                                .getRealDouble();

                                roiCursor.reset();
                                stddevs[r] = Math
                                                .sqrt(new Variance<T, DoubleType>()
                                                                .compute(roiCursor,
                                                                                new DoubleType())
                                                                .getRealDouble());

                                minStdDev = Math.min(stddevs[r], minStdDev);

                                r++;
                                // CODE END
                        }

                        // Calc
                        double sum = 0;
                        double sum2 = 0;
                        for (int d = 0; d < stddevs.length; d++) {
                                stddevs[d] = minStdDev / stddevs[d];

                                if (Double.isNaN(stddevs[d])) {
                                        stddevs[d] = 1;
                                }

                                final double tmp = Math.pow(stddevs[d], m_lambda);
                                sum += tmp;
                                sum2 += tmp * means[d];
                        }

                        for (int d = 0; d < displacement.length; d++) {
                                displacement[d] = position[d];
                        }

                        outCursor.get().setReal(sum2 / sum);

                }

                return output;
        }

        private PolygonRegionOfInterest[] createROIs(final T empty, final long[] span) {

                // TODO: Only 2d case implemented and this is not well done can
                // be
                // automatized (either line or bresenham change ... can be
                // calculated
                // for n-dimensions) (nd)
                final int numRois = 8;

                final PolygonRegionOfInterest[] rois = new PolygonRegionOfInterest[numRois];
                int t = 0;

                final Point origin = new Point(new long[span.length]);

                // T0
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { span[0], 0 }));

                rois[t].addVertex(2, new Point(new long[] { span[0], span[1] }));
                t++;

                // T1
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { span[0], 0 }));

                rois[t].addVertex(2,
                                new Point(new long[] { span[0], -span[1] }));
                t++;

                // T2
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1,
                                new Point(new long[] { span[0], -span[1] }));
                rois[t].addVertex(2, new Point(new long[] { 0, -span[1] }));

                t++;

                // T3
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { 0, -span[1] }));
                rois[t].addVertex(2, new Point(
                                new long[] { -span[0], -span[1] }));

                t++;

                // T4
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { -span[0], 0 }));
                rois[t].addVertex(2, new Point(
                                new long[] { -span[0], -span[1] }));

                t++;

                // T5
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { -span[0], 0 }));
                rois[t].addVertex(2,
                                new Point(new long[] { -span[0], span[1] }));

                t++;

                // T6
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { 0, span[1] }));
                rois[t].addVertex(2,
                                new Point(new long[] { -span[0], span[1] }));

                t++;

                // T7
                rois[t] = new PolygonRegionOfInterest();
                rois[t].addVertex(0, origin);

                rois[t].addVertex(1, new Point(new long[] { 0, span[1] }));
                rois[t].addVertex(2, new Point(new long[] { span[0], span[1] }));

                t++;

                return rois;
        }

        @Override
        public UnaryOperation<I, I> copy() {
                return new MaxHomogenityOp<T, I>(m_lambda, m_span.clone(),
                                m_outofbounds);
        }

}
