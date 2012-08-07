package org.knime.knip.core.awt.specializedrendering;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.img.subset.SubsetViews;
import net.imglib2.type.Type;
import net.imglib2.view.IterableRandomAccessibleInterval;

public class Projector2D<A extends Type<A>, B extends Type<B>> extends
		Abstract2DProjector<A, B> {

	final Converter<A, B> converter;
	final protected IterableInterval<B> target;
	final int numDimensions;
	private final int dimX;
	private final int dimY;

	final int X = 0;
	final int Y = 1;
	private final RandomAccessibleInterval<A> source;

	public Projector2D(final int dimX, final int dimY,
			final RandomAccessibleInterval<A> source,
			final IterableInterval<B> target, final Converter<A, B> converter) {
		super(source.numDimensions());
		this.dimX = dimX;
		this.dimY = dimY;
		this.target = target;
		this.source = source;
		this.converter = converter;
		this.numDimensions = source.numDimensions();
	}

	@Override
	public void map() {
		// fix interval for all dimensions
		for (int d = 0; d < position.length; ++d)
			min[d] = max[d] = position[d];

		min[dimX] = target.min(X);
		min[dimY] = target.min(Y);
		max[dimX] = target.max(X);
		max[dimY] = target.max(Y);
		final FinalInterval sourceInterval = new FinalInterval(min, max);

		IterableRandomAccessibleInterval<A> iterableSubsetView = SubsetViews
				.iterableSubsetView(source, sourceInterval);

		final Cursor<B> targetCursor = target.cursor();
		final Cursor<A> sourceCursor = iterableSubsetView.cursor();


		while (targetCursor.hasNext()) {
			targetCursor.fwd();
			sourceCursor.fwd();
                        converter.convert(sourceCursor.get(),
                                        targetCursor.get());
		}
	}
}
