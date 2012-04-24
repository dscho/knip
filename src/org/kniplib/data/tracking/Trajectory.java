package org.kniplib.data.tracking;

//package org.yaialib.data.tracking;
//
//import java.util.Arrays;
//import java.util.Iterator;
//import java.util.List;
//
//import net.imglib2.FinalInterval;
//import net.imglib2.Interval;
//import net.imglib2.Location;
//import net.imglib2.roi.IterableRegionOfInterest;
//
///**
// * 
// * @author dietzc, schoenen
// */
//public class Trajectory<L extends Comparable<L>, II extends IntegerType<II>> implements
//		Iterable<TrajectoryPoint> {
//	private final L m_label;
//	private final L m_predecessor;
//	private final List<TrajectoryPoint> m_trac;
//	private final Interval m_boundingbox;
//
//	public Trajectory(final L label, final List<TrajectoryPoint> trac) {
//		this(label, null, trac);
//	}
//
//	public Trajectory(final L label, final L predecessor,
//			final List<TrajectoryPoint> trac) {
//		m_label = label;
//		m_predecessor = predecessor;
//		m_trac = trac;
//		final int nDim = trac.get(0).numDimensions();
//		final long[] min = new long[nDim];
//		final long[] max = new long[nDim];
//		Arrays.fill(min, Long.MAX_VALUE);
//		Arrays.fill(max, Long.MIN_VALUE);
//		for (TrajectoryPoint l : m_trac) {
//			IterableRegionOfInterest roi = l.getIterableRegionOfInterest();
//			for (int i = 0; i < nDim; i++) {
//				long s, e = s = l.getLongPosition(i);
//				if (roi != null) {
//					s = (long) Math.floor(roi.realMin(i));
//					e = (long) Math.ceil(roi.realMax(i));
//				}
//				if (s < min[i])
//					min[i] = s;
//				if (e > max[i])
//					max[i] = e;
//			}
//		}
//		m_boundingbox = new FinalInterval(min, max);
//	}
//
//	public final int numDimensions() {
//		return m_trac.get(0).numDimensions();
//	}
//
//	public final L label() {
//		return m_label;
//	}
//
//	public final L predecessor() {
//		return m_predecessor;
//	}
//
//	public final Interval interval() {
//		return m_boundingbox;
//	}
//
//	public final Location first() {
//		return m_trac.get(0);
//	}
//
//	public final Location last() {
//		return m_trac.get(m_trac.size() - 1);
//	}
//
//	public final int size() {
//		return m_trac.size();
//	}
//
//	@Override
//	public Iterator<TrajectoryPoint> iterator() {
//		return new Iterator<TrajectoryPoint>() {
//			private final Iterator<TrajectoryPoint> m_i = m_trac.iterator();
//
//			@Override
//			public boolean hasNext() {
//				return m_i.hasNext();
//			}
//
//			@Override
//			public TrajectoryPoint next() {
//				return m_i.next();
//			}
//
//			@Override
//			public void remove() {
//				throw new UnsupportedOperationException();
//			}
//		};
//	}
// }
