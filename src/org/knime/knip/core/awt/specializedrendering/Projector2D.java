package org.knime.knip.core.awt.specializedrendering;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

@SuppressWarnings("javadoc")
public class Projector2D<A extends Type<A>, B extends Type<B>> extends Abstract2DProjector<A, B> {

    private final IterableInterval<B> m_target;

    private final Converter<A, B> m_converter;

    private final int m_dimX;

    private final int m_dimY;

    private final RandomAccessibleInterval<A> m_source;

    private static final int X = 0;

    private static final int Y = 1;

    /**
     * @param dimX
     * @param dimY
     * @param source
     * @param target
     * @param converter
     */
    public Projector2D(final int dimX, final int dimY, final RandomAccessibleInterval<A> source,
                       final IterableInterval<B> target, final Converter<A, B> converter) {
        super(source.numDimensions());
        this.m_dimX = dimX;
        this.m_dimY = dimY;
        this.m_target = target;
        this.m_source = source;
        this.m_converter = converter;
    }

    @Override
    public void map() {
        // fix interval for all dimensions
        for (int d = 0; d < position.length; ++d) {
            min[d] = max[d] = position[d];
        }

        min[m_dimX] = m_target.min(X);
        min[m_dimY] = m_target.min(Y);
        max[m_dimX] = m_target.max(X);
        max[m_dimY] = m_target.max(Y);
        final FinalInterval sourceInterval = new FinalInterval(min, max);
        final RandomAccessibleInterval<A> subset = SubsetOperations.subsetview(m_source, sourceInterval);

        final Cursor<B> targetCursor = m_target.cursor();
        final Cursor<A> sourceCursor = Views.iterable(subset).cursor();

        while (targetCursor.hasNext()) {
            targetCursor.fwd();
            sourceCursor.fwd();
            m_converter.convert(sourceCursor.get(), targetCursor.get());
        }
    }
}
