package org.knime.knip.core.ops.iterable;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public abstract class SlidingShapeOp<T extends Type<T>, V extends Type<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                implements UnaryOperation<IN, OUT> {

        protected final Shape shape;

        protected final OutOfBoundsFactory<T, IN> outofbounds;

        public SlidingShapeOp(final Shape shape, final OutOfBoundsFactory<T, IN> outofbounds) {
                this.shape = shape;
                this.outofbounds = outofbounds;
        }

        @Override
        public OUT compute(final IN input, final OUT output) {

                // Neighboor update
                final IntervalView<T> interval = Views.interval(
                                Views.extend(input, outofbounds), input);

                final IterableInterval<Neighborhood<T>> neighborhoods = shape
                                .neighborhoods(interval);

                // Create an iterable to check iteration order
                if (!neighborhoods.iterationOrder().equals(
                                output.iterationOrder())) {
                        throw new IllegalArgumentException(
                                        "Iteration order doesn't fit in SlidingNeighborhoodOp");
                }

                return compute(neighborhoods, input, output);
        }

        protected abstract OUT compute(
                        IterableInterval<Neighborhood<T>> neighborhoods,
                        IN input, OUT output);
}
