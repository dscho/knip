package org.knime.knip.core.ops.bittype;

import java.awt.Point;
import java.awt.Polygon;
import java.util.ArrayList;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.logic.BitType;

import org.knime.knip.core.algorithm.BresenhamAlgorithm;

/**
 *
 * Creates the convex hull from a point cloud in a binary image.
 *
 * @author hornm, University of Konstanz
 */
public class ConvexHull2D<K extends RandomAccessibleInterval<BitType> & IterableInterval<BitType>>
                implements UnaryOperation<K, K> {

        private final int m_dimX;

        private final int m_dimY;

        private final boolean m_fill;

        /**
         * @param dimX
         * @param dimY
         * @param fill
         *                wether to fill the resulting region
         */
        public ConvexHull2D(final int dimX, final int dimY, final boolean fill) {
                m_dimX = dimX;
                m_dimY = dimY;
                m_fill = fill;
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public K compute(final K in, final K r) {
                final Cursor<BitType> cur = in.localizingCursor();
                ArrayList<Point> points = new ArrayList<Point>();
                while (cur.hasNext()) {
                        cur.fwd();
                        if (cur.get().get()) {
                                points.add(new Point(
                                                cur.getIntPosition(m_dimX),
                                                cur.getIntPosition(m_dimY)));
                        }
                }
                points = quickHull(points);
                final Polygon poly = new Polygon();
                for (final Point p : points) {
                        poly.addPoint(p.x, p.y);
                }

                Cursor<BitType> resCur = r.localizingCursor();

                if (m_fill) {
                        while (resCur.hasNext()) {
                                resCur.fwd();
                                if (poly.contains(
                                                resCur.getIntPosition(m_dimX),
                                                resCur.getIntPosition(m_dimY))) {
                                        resCur.get().set(true);
                                }
                        }
                } else {

                        // m_imgManFalse.compute(in, r);

                        final RandomAccess<BitType> ra = r.randomAccess();

                        for (int i = 0; i < poly.npoints - 1; i++) {
                                drawLine(ra, poly, i, i + 1, m_dimX, m_dimY);
                        }
                        if (poly.npoints > 0) {
                                drawLine(ra, poly, poly.npoints - 1, 0, m_dimX,
                                                m_dimY);
                        }
                }
                return r;

                // // debug
                // BufferedImage buf = new BufferedImage((int)
                // r.dimension(m_dimX),
                // (int) r.dimension(m_dimY), BufferedImage.TYPE_BYTE_GRAY);
                // buf.getGraphics().drawPolygon(poly);
                // AWTImageTools.showInFrame(buf);
        }

        private static int[] p1 = new int[2];

        private static int[] p2 = new int[2];

        private static void drawLine(final RandomAccess<BitType> ra,
                        final Polygon poly, final int idx1, final int idx2,
                        final int dimX, final int dimY) {
                p1[0] = poly.xpoints[idx1];
                p1[1] = poly.ypoints[idx1];
                p2[0] = poly.xpoints[idx2];
                p2[1] = poly.ypoints[idx2];
                final int[][] points = BresenhamAlgorithm.rasterizeLine(p1, p2);
                for (final int[] p : points) {
                        ra.setPosition(p[0], dimX);
                        ra.setPosition(p[1], dimY);
                        ra.get().set(true);
                }
        }

        @SuppressWarnings("unchecked")
        private ArrayList<Point> quickHull(final ArrayList<Point> points) {
                final ArrayList<Point> convexHull = new ArrayList<Point>();
                if (points.size() < 3) {
                        return (ArrayList<Point>) points.clone();
                }
                // find extremals
                int minPoint = -1, maxPoint = -1;
                int minX = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                for (int i = 0; i < points.size(); i++) {
                        if (points.get(i).x < minX) {
                                minX = points.get(i).x;
                                minPoint = i;
                        }
                        if (points.get(i).x > maxX) {
                                maxX = points.get(i).x;
                                maxPoint = i;
                        }
                }
                final Point A = points.get(minPoint);
                final Point B = points.get(maxPoint);
                convexHull.add(A);
                convexHull.add(B);
                points.remove(A);
                points.remove(B);

                final ArrayList<Point> leftSet = new ArrayList<Point>();
                final ArrayList<Point> rightSet = new ArrayList<Point>();

                for (int i = 0; i < points.size(); i++) {
                        final Point p = points.get(i);
                        if (pointLocation(A, B, p) == -1) {
                                leftSet.add(p);
                        } else {
                                rightSet.add(p);
                        }
                }
                hullSet(A, B, rightSet, convexHull);
                hullSet(B, A, leftSet, convexHull);

                return convexHull;
        }

        /*
         * Computes the square of the distance of point C to the segment defined
         * by points AB
         */
        private static synchronized int distance(final Point A, final Point B,
                        final Point C) {
                final int ABx = B.x - A.x;
                final int ABy = B.y - A.y;
                int num = ABx * (A.y - C.y) - ABy * (A.x - C.x);
                if (num < 0) {
                        num = -num;
                }
                return num;
        }

        private void hullSet(final Point A, final Point B,
                        final ArrayList<Point> set, final ArrayList<Point> hull) {
                final int insertPosition = hull.indexOf(B);
                if (set.size() == 0) {
                        return;
                }
                if (set.size() == 1) {
                        final Point p = set.get(0);
                        set.remove(p);
                        hull.add(insertPosition, p);
                        return;
                }
                int dist = Integer.MIN_VALUE;
                int furthestPoint = -1;
                for (int i = 0; i < set.size(); i++) {
                        final Point p = set.get(i);
                        final int distance = distance(A, B, p);
                        if (distance > dist) {
                                dist = distance;
                                furthestPoint = i;
                        }
                }
                final Point P = set.get(furthestPoint);
                set.remove(furthestPoint);
                hull.add(insertPosition, P);

                // Determine who's to the left of AP
                final ArrayList<Point> leftSetAP = new ArrayList<Point>();
                for (int i = 0; i < set.size(); i++) {
                        final Point M = set.get(i);
                        if (pointLocation(A, P, M) == 1) {
                                leftSetAP.add(M);
                        }
                }

                // Determine who's to the left of PB
                final ArrayList<Point> leftSetPB = new ArrayList<Point>();
                for (int i = 0; i < set.size(); i++) {
                        final Point M = set.get(i);
                        if (pointLocation(P, B, M) == 1) {
                                leftSetPB.add(M);
                        }
                }
                hullSet(A, P, leftSetAP, hull);
                hullSet(P, B, leftSetPB, hull);

        }

        public static synchronized int pointLocation(final Point A,
                        final Point B, final Point P) {
                final int cp1 = (B.x - A.x) * (P.y - A.y) - (B.y - A.y)
                                * (P.x - A.x);
                return (cp1 > 0) ? 1 : -1;
        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new ConvexHull2D<K>(m_dimX, m_dimY, m_fill);
        }

}
