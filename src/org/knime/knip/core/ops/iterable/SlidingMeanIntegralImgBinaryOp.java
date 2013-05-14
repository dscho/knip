package org.knime.knip.core.ops.iterable;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.knime.knip.core.ops.integralimage.IntegralImgND;
import org.knime.knip.core.ops.integralimage.IntegralImgSumAgent;

public class SlidingMeanIntegralImgBinaryOp<T extends RealType<T>, V extends RealType<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                extends SlidingShapeOp<T, V, IN, OUT> {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private final IntegralImgND m_iiOp = new IntegralImgND(
                        new ArrayImgFactory());
        private final BinaryOperation<DoubleType, T, V> m_binaryOp;
        private final int m_span;

        public SlidingMeanIntegralImgBinaryOp(
                        final BinaryOperation<DoubleType, T, V> binaryOp,
                        final RectangleShape shape, final int span,
                        final OutOfBoundsFactory<T, IN> outOfBounds) {
                super(shape, outOfBounds);
                m_binaryOp = binaryOp;
                m_span = span;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingMeanIntegralImgBinaryOp<T, V, IN, OUT>(
                                m_binaryOp.copy(), (RectangleShape) shape,
                                m_span, outofbounds);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected OUT compute(final IterableInterval<Neighborhood<T>> neighborhoods,
                        final IN input, final OUT output) {

                final long[] min = new long[input.numDimensions()];
                final long[] max = new long[input.numDimensions()];

                for (int d = 0; d < input.numDimensions(); d++) {
                        min[d] = -m_span;
                        max[d] = (input.dimension(d) - 1) + m_span;
                }

                final Cursor<T> inCursor = Views.flatIterable(input).cursor();
                final Cursor<V> outCursor = output.cursor();

                // extend such that image is 2*span larger in each dimension
                // with corresponding outofbounds extension
                // Result: We have a IntegralImage

                final IntervalView<T> extended = Views.offset(Views.interval(
                                Views.extend(input, outofbounds),
                                new FinalInterval(min, max)), min);

                final RandomAccessibleInterval<IntType> ii = Operations.compute(
                                m_iiOp, extended);

                final DoubleType mean = new DoubleType();
                final long[] p1 = new long[input.numDimensions()];
                final long[] p2 = new long[input.numDimensions()];

                final IntegralImgSumAgent sumAgent = new IntegralImgSumAgent(ii);

                for (final Neighborhood<T> neighborhood : neighborhoods) {
                        inCursor.fwd();
                        outCursor.fwd();

                        for (int d = 0; d < p1.length; d++) {
                                final long p = inCursor.getLongPosition(d);
                                p1[d] = p; // -span
                                p2[d] = p + 2 * m_span; // +span
                        }

                        mean.setReal(sumAgent.getSum(p1, p2)
                                        / neighborhood.size());

                        m_binaryOp.compute(mean, inCursor.get(),
                                        outCursor.get());
                }

                return output;
        }

}
