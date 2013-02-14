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
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import org.knime.knip.core.ops.integralimage.IntegralImageSumAgent;
import org.knime.knip.core.ops.integralimage.IntegralImgND;

public class SlidingMeanIntegralImgBinaryOp<T extends RealType<T>, V extends RealType<V> & NativeType<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                extends SlidingShapeOp<T, V, IN, OUT> {

        private final IntegralImgND<T, IntType> m_iiOp;
        private final BinaryOperation<DoubleType, T, V> m_binaryOp;
        private final int m_span;

        public SlidingMeanIntegralImgBinaryOp(
                        BinaryOperation<DoubleType, T, V> binaryOp,
                        RectangleShape shape, int span,
                        OutOfBoundsFactory<T, IN> outOfBounds) {
                super(shape, outOfBounds);
                m_binaryOp = binaryOp;
                m_iiOp = new IntegralImgND<T, IntType>(
                                new ArrayImgFactory<IntType>());
                m_span = span;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingMeanIntegralImgBinaryOp<T, V, IN, OUT>(
                                m_binaryOp.copy(), (RectangleShape) shape,
                                m_span, outofbounds);
        }

        @Override
        protected OUT compute(IterableInterval<Neighborhood<T>> neighborhoods,
                        IN input, OUT output) {

                long[] min = new long[input.numDimensions()];
                long[] max = new long[input.numDimensions()];

                for (int d = 0; d < input.numDimensions(); d++) {
                        min[d] = -m_span;
                        max[d] = (input.dimension(d) - 1) + m_span;
                }

                Cursor<T> inCursor = Views.iterable(input).cursor();
                Cursor<V> outCursor = output.cursor();

                // extend such that image is 2*span larger in each dimension
                // with corresponding outofbounds extension
                // Result: We have a IntegralImage

                IntervalView<T> extended = Views.offset(Views.interval(
                                Views.extend(input, outofbounds),
                                new FinalInterval(min, max)), min);



                RandomAccessibleInterval<IntType> ii = Operations.compute(
                                m_iiOp, extended);


                DoubleType mean = new DoubleType();
                long[] p1 = new long[input.numDimensions()];
                long[] p2 = new long[input.numDimensions()];

                IntegralImageSumAgent<IntType> sumAgent = new IntegralImageSumAgent<IntType>(
                                ii);

                for (final Neighborhood<T> neighborhood : neighborhoods) {
                        inCursor.fwd();
                        outCursor.fwd();

                        for(int d = 0; d < p1.length; d++){
                                long p =  inCursor.getLongPosition(d);
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
