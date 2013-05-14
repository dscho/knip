package org.knime.knip.core.ops.iterable;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

public class SlidingShapeOpBinaryInside<T extends Type<T>, V extends Type<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
        extends SlidingShapeOp<T, V, IN, OUT> {

    private BinaryOperation<Iterator<T>, T, V> m_op;

    public SlidingShapeOpBinaryInside(final Shape neighborhood, final BinaryOperation<Iterator<T>, T, V> op,
                                      final OutOfBoundsFactory<T, IN> outofbounds) {
        super(neighborhood, outofbounds);
        this.m_op = op;
    }

    @Override
    public UnaryOperation<IN, OUT> copy() {
        return new SlidingShapeOpBinaryInside<T, V, IN, OUT>(m_shape, m_op != null ? m_op.copy() : null, m_outOfBounds);
    }

    @Override
    protected OUT compute(final IterableInterval<Neighborhood<T>> neighborhoods, final IN input, final OUT output) {

        final Cursor<T> inCursor = Views.iterable(SubsetOperations.subsetview(input, input)).cursor();
        final Cursor<V> outCursor = output.cursor();
        for (final Neighborhood<T> neighborhood : neighborhoods) {
            m_op.compute(neighborhood.cursor(), inCursor.next(), outCursor.next());
        }

        return output;

    }

    public void updateOperation(final BinaryOperation<Iterator<T>, T, V> op) {
        this.m_op = op;
    }

}
