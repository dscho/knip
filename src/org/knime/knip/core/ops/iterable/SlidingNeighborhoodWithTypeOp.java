package org.knime.knip.core.ops.iterable;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.AbstractNeighborhood;
import net.imglib2.ops.BinaryOperation;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

public class SlidingNeighborhoodWithTypeOp<T extends Type<T>, V extends Type<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                implements UnaryOperation<IN, OUT> {

        private AbstractNeighborhood<T, IN> neighborhood;

        private BinaryOperation<Iterator<T>, T, V> op;

        public SlidingNeighborhoodWithTypeOp(
                        AbstractNeighborhood<T, IN> neighborhood, int span) {
                this(neighborhood, null);
        }

        public SlidingNeighborhoodWithTypeOp(
                        AbstractNeighborhood<T, IN> neighborhood,
                        BinaryOperation<Iterator<T>, T, V> op) {
                this.op = op;
                this.neighborhood = neighborhood;
        }

        @Override
        public OUT compute(IN input, OUT output) {

                IterableInterval<T> iterable = Views.iterable(input);

                if (!iterable.iterationOrder().equals(output.iterationOrder()))
                        throw new IllegalArgumentException(
                                        "Iteration order doesn't fit in SlidingNeighborhoodOp");

                // Neighboor update
                neighborhood.updateSource(input);

                // Neighborhood update
                Cursor<T> neighborhoodCursor = neighborhood.cursor();

                // Cursors are created.
                Cursor<V> resCursor = output.cursor();
                Cursor<T> inCursor = iterable.cursor();

                // Sliding
                while (resCursor.hasNext()) {
                        // fwd res
                        resCursor.fwd();
                        inCursor.fwd();

                        // NeighborhoodCursor is reseted
                        neighborhood.setPosition(resCursor);

                        // reset the cursor
                        neighborhoodCursor.reset();

                        // Neighborhood is iterable at the moment as center was
                        // updated
                        op.compute(neighborhoodCursor, inCursor.get(),
                                        resCursor.get());

                }

                return output;

        }

        public void updateOperation(BinaryOperation<Iterator<T>, T, V> op) {
                this.op = op;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingNeighborhoodWithTypeOp<T, V, IN, OUT>(
                                neighborhood.copy(), op != null ? op.copy()
                                                : null);
        }
}
