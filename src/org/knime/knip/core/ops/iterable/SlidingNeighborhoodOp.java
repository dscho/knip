package org.knime.knip.core.ops.iterable;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.AbstractNeighborhood;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

public class SlidingNeighborhoodOp<T extends Type<T>, V extends Type<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                implements UnaryOperation<IN, OUT> {

        private AbstractNeighborhood<T, IN> neighborhood;

        private UnaryOperation<Iterator<T>, V> op;

        public SlidingNeighborhoodOp(AbstractNeighborhood<T, IN> neighborhood) {
                this(neighborhood, null);
        }

        public SlidingNeighborhoodOp(AbstractNeighborhood<T, IN> neighborhood,
                        UnaryOperation<Iterator<T>, V> op) {
                this.op = op;
                this.neighborhood = neighborhood;
        }

        @Override
        public OUT compute(IN input, OUT output) {

                // Create an iterable to check iteration order
                if (!Views.iterable(input).iterationOrder()
                                .equals(output.iterationOrder()))
                        throw new IllegalArgumentException(
                                        "Iteration order doesn't fit in SlidingNeighborhoodOp");

                // Neighboor update
                neighborhood.updateSource(input);

                // Cursor
                Cursor<T> neighborhoodCursor = neighborhood.cursor();

                // Cursors are created.
                Cursor<V> resCursor = output.cursor();

                // Sliding
                while (resCursor.hasNext()) {
                        // fwd res
                        resCursor.fwd();

                        // NeighborhoodCursor is reseted
                        neighborhood.setPosition(resCursor);
                        neighborhoodCursor.reset();

                        // Neighborhood is iterable at the moment as center was
                        // updated
                        op.compute(neighborhoodCursor, resCursor.get());

                }

                return output;

        }

        public void updateOperation(UnaryOperation<Iterator<T>, V> op) {
                this.op = op;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingNeighborhoodOp<T, V, IN, OUT>(
                                neighborhood.copy(), op != null ? op.copy()
                                                : null);
        }

}
