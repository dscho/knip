package org.knime.knip.core.ops.iterable;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class SlidingShapeOp<T extends Type<T>, V extends Type<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                implements UnaryOperation<IN, OUT> {

        private final Shape shape;

        private UnaryOperation<Iterator<T>, V> op;

        private final OutOfBoundsFactory<T, IN> outofbounds;

        public SlidingShapeOp(Shape neighborhood,
                        UnaryOperation<Iterator<T>, V> op,
                        OutOfBoundsFactory<T, IN> outofbounds) {
                this.op = op;
                this.shape = neighborhood;
                this.outofbounds = outofbounds;
        }

        @Override
        public OUT compute(IN input, OUT output) {

                // Neighboor update
                IntervalView<T> interval = Views.interval(
                                Views.extend(input, outofbounds), input);

                IterableInterval<Neighborhood<T>> neighborhoods = shape
                                .neighborhoods(interval);

                // Create an iterable to check iteration order
                if (!neighborhoods.iterationOrder().equals(
                                output.iterationOrder()))
                        throw new IllegalArgumentException(
                                        "Iteration order doesn't fit in SlidingNeighborhoodOp");

                Cursor<V> outCursor = output.cursor();
                for (final Neighborhood<T> neighborhood : neighborhoods) {
                        op.compute(neighborhood.cursor(), outCursor.next());
                }

                return output;

        }

        public void updateOperation(UnaryOperation<Iterator<T>, V> op) {
                this.op = op;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingShapeOp<T, V, IN, OUT>(shape,
                                op != null ? op.copy() : null, outofbounds);
        }

}
