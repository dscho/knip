//package org.kniplib.ops.misc;
//
//import java.util.Iterator;
//
//import net.imglib2.Cursor;
//import net.imglib2.IterableInterval;
//import net.imglib2.IterableRealInterval;
//import net.imglib2.Localizable;
//import net.imglib2.Positionable;
//import net.imglib2.RandomAccess;
//import net.imglib2.RandomAccessibleInterval;
//import net.imglib2.RealPositionable;
//import net.imglib2.type.Type;
//
////TODO: What is this?
//public class IterableLine<T extends Type<T>> implements IterableInterval<T> {
//
//	private final RandomAccessibleInterval<T> m_rai;
//	private final long[] m_pos;
//	private final int m_dim;
//	private final long m_min;
//	private final long m_max;
//
//	public IterableLine(final RandomAccessibleInterval<T> rai,
//			final long[] pos, final int dim) {
//		m_rai = rai;
//		m_pos = pos;
//		m_dim = dim;
//		m_min = m_rai.min(m_dim);
//		m_max = m_rai.max(m_dim);
//	}
//
//	public IterableLine(final RandomAccessibleInterval<T> rai,
//			final Localizable loc, final int dim) {
//		m_rai = rai;
//		m_pos = new long[loc.numDimensions()];
//		loc.localize(m_pos);
//
//		m_dim = dim;
//		m_min = m_rai.min(m_dim);
//		m_max = m_rai.max(m_dim);
//	}
//
//	@Override
//	public Iterator<T> iterator() {
//		return new Iterator<T>() {
//
//			private final RandomAccess<T> m_ra = m_rai.randomAccess();
//			private long i = m_min;
//
//			{
//				m_ra.setPosition(m_pos);
//			}
//
//			@Override
//			public boolean hasNext() {
//				return i < m_max;
//			}
//
//			@Override
//			public T next() {
//				++i;
//				m_ra.setPosition(i, m_dim);
//				return m_ra.get();
//			}
//
//			@Override
//			public void remove() {
//				throw new Error();
//			}
//		};
//	}
//
//	@Override
//	public long size() {
//		return m_max - m_min;
//	}
//
//	@Override
//	public T firstElement() {
//		return iterator().next();
//	}
//
//	@Override
//	public boolean equalIterationOrder(IterableRealInterval<?> f) {
//		return false;
//	}
//
//	@Override
//	public double realMin(int d) {
//		return m_min;
//	}
//
//	@Override
//	public void realMin(double[] min) {
//		min[0] = m_min;
//	}
//
//	@Override
//	public void realMin(RealPositionable min) {
//		min.setPosition(m_min, 0);
//	}
//
//	@Override
//	public double realMax(int d) {
//		return m_max;
//	}
//
//	@Override
//	public void realMax(double[] max) {
//		max[0] = m_max;
//	}
//
//	@Override
//	public void realMax(RealPositionable max) {
//		max.setPosition(m_max, 0);
//	}
//
//	@Override
//	public int numDimensions() {
//		return 1;
//	}
//
//	@Override
//	public long min(int d) {
//		return m_min;
//	}
//
//	@Override
//	public void min(long[] min) {
//		min[0] = m_min;
//	}
//
//	@Override
//	public void min(Positionable min) {
//		min.setPosition(m_min, 0);
//	}
//
//	@Override
//	public long max(int d) {
//		return m_max;
//	}
//
//	@Override
//	public void max(long[] max) {
//		max[0] = m_max;
//	}
//
//	@Override
//	public void max(Positionable max) {
//		max.setPosition(m_max, 0);
//	}
//
//	@Override
//	public void dimensions(long[] dimensions) {
//		dimensions[0] = m_max;
//	}
//
//	@Override
//	public long dimension(int d) {
//		return m_max;
//	}
//
//	@Override
//	public Cursor<T> cursor() {
//		return null;
//	}
//
//	@Override
//	public Cursor<T> localizingCursor() {
//		return null;
//	}
//
//	@Override
//	public Object iterationOrder()
//	{
//		return null;
//	}
// }
