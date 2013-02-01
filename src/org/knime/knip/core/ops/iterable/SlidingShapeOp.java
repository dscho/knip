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

        public SlidingShapeOp(Shape neighborhood,
                        OutOfBoundsFactory<T, IN> outofbounds) {
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


                return process(neighborhoods, input, output);
        }

        protected abstract OUT process(
                        IterableInterval<Neighborhood<T>> neighborhoods,
                        IN input, OUT output);
}
