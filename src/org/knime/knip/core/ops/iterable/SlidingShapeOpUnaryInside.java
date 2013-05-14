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

public class SlidingShapeOpUnaryInside<T extends Type<T>, V extends Type<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
extends SlidingShapeOp<T, V, IN, OUT> {

    private UnaryOperation<Iterator<T>, V> op;

    public SlidingShapeOpUnaryInside(final Shape neighborhood, final UnaryOperation<Iterator<T>, V> op,
                                     final OutOfBoundsFactory<T, IN> outofbounds) {
        super(neighborhood, outofbounds);
        this.op = op;
    }

    @Override
    protected OUT compute(final IterableInterval<Neighborhood<T>> neighborhoods, final IN input, final OUT output) {
        final Cursor<V> outCursor = output.cursor();
        for (final Neighborhood<T> neighborhood : neighborhoods) {
            op.compute(neighborhood.cursor(), outCursor.next());
        }

        return output;
    }

    public void updateOperation(final UnaryOperation<Iterator<T>, V> op) {
        this.op = op;
    }

    @Override
    public UnaryOperation<IN, OUT> copy() {
        return new SlidingShapeOpUnaryInside<T, V, IN, OUT>(shape, op != null ? op.copy() : null, outofbounds);
    }

}
