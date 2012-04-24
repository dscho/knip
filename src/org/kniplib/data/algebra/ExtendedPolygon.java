/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.kniplib.data.algebra;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;

import org.kniplib.algorithm.BresenhamAlgorithm;
import org.kniplib.tools.PolygonTools;

/**
 * Adds more functionality to the java.awt.Polygon class.
 * 
 * 
 * @author hornm
 * 
 */

public class ExtendedPolygon extends Polygon implements Iterable<int[]> {

        /**
	 * 
	 */
        private static final long serialVersionUID = 4797555800668312421L;

        /**
         * The bitmap mask.
         */
        private Img<BitType> m_mask = null;

        private long[] m_center;

        /**
	 * 
	 */
        public ExtendedPolygon() {
                super();
        }

        /**
         * Wraps the given polygon.
         * 
         * @param poly
         *                polygon to wrap
         */
        public ExtendedPolygon(Polygon poly) {
                super(poly.xpoints, poly.ypoints, poly.npoints);
        }

        /**
         * 
         * @return the center of the polygons bounding box
         */
        public long[] getBoundingBoxCenter() {
                Rectangle r = getBounds();

                return new long[] { r.x + r.width / 2, r.y + r.height / 2 };

        }

        /**
         * The center of the polygon. If no center was set, the bounding box
         * center will be returned (not a copy!)
         * 
         * @return
         */

        public long[] getCenter() {
                if (m_center == null) {
                        return getBoundingBoxCenter();
                }
                return m_center;
        }

        /**
         * Sets the new center of the polygon. No checks are made, whether it
         * lies outside of the contour and, furthermore, NO copy is made!
         * 
         * @param p
         */

        public void setCenter(final long[] p) {
                m_center = p;
        }

        /**
         * Creates the bitmask.
         * 
         * @return
         */
        public Img<BitType> createOutline() {
                if (m_mask != null) {
                        return m_mask;
                }
                Rectangle r = getBounds();
                int w = r.width;
                int h = r.height;
                m_mask = new ArrayImgFactory<BitType>().create(
                                new int[] { w, h }, new BitType());
                Cursor<BitType> c = m_mask.localizingCursor();

                while (c.hasNext()) {
                        c.fwd();
                        if (contains(new Point(r.x + c.getIntPosition(0), r.y
                                        + c.getIntPosition(1)))) {
                                c.get().set(true);
                        }
                }

                return m_mask;

        }

        /**
         * Creates the bitmask.
         * 
         * @return
         */
        public Img<BitType> createBitmask() {
                if (m_mask != null) {
                        return m_mask;
                }
                Rectangle r = getBounds();
                int w = r.width;
                int h = r.height;
                m_mask = new ArrayImgFactory<BitType>().create(
                                new int[] { w, h }, new BitType());
                Cursor<BitType> c = m_mask.localizingCursor();

                while (c.hasNext()) {
                        c.fwd();
                        if (contains(new Point(r.x + c.getIntPosition(0), r.y
                                        + c.getIntPosition(1)))) {
                                c.get().set(true);
                        }
                }

                return m_mask;

        }

        /**
         * Return the number of included points in the contour
         * 
         * @return number of points
         */

        public int length() {
                return npoints;
        }

        /**
         * Return the point at the specified index.
         * 
         * @param index
         * @return
         */

        public int[] getPointAt(final int index) {
                if (index < 0 || index >= npoints) {
                        return null;
                } else {
                        return new int[] { xpoints[index], ypoints[index] };
                }
        }

        /**
         * Determines the normal vector of the point at the given index.
         * 
         * @param index
         * @return
         */
        public float[] getNormalVecAtPoint(final int index) {
                int i = index + length();
                Vector n;

                n = new Vector(getPointAt((i - 3) % length())).mapMultiply(-1);
                n = n.add(new Vector(getPointAt((i - 2) % length()))
                                .mapMultiply(-1));
                n = n.add(new Vector(getPointAt((i - 1) % length()))
                                .mapMultiply(-1));
                n = n.add(new Vector(getPointAt((i + 1) % length())));
                n = n.add(new Vector(getPointAt((i + 2) % length())));
                n = n.add(new Vector(getPointAt((i + 3) % length())));
                n = new Vector(new int[] { -n.getIntPosition(1),
                                n.getIntPosition(0) });
                float[] res = new float[n.numDimensions()];
                n.localize(res);
                return res;
        }

        /**
         * Calculates the angle of the normal vector of the point at the given
         * index.
         * 
         * @param index
         * @return
         */

        public double getAngleAtPoint(final int index) {
                float[] n = getNormalVecAtPoint(index);
                // orthogonal
                n = new float[] { -n[1], n[0] };
                double ang = Math.atan2(n[0], n[1]);
                return (Math.abs(ang) != ang ? 2 * Math.PI + ang : ang);
        }

        /**
         * Resamples the polygon with the given maximum number of points.
         * 
         * @param maxNumPoints
         * 
         * @param numPoints
         * @return the new resampled contour
         */
        public ExtendedPolygon resamplePolygon(final int maxNumPoints) {

                // collect all possible points
                List<int[]> allPoints = new java.util.Vector<int[]>();
                for (int i = 1; i < npoints; i++) {
                        int[] p1 = getPointAt(i - 1);
                        int[] p2 = getPointAt(i);
                        int[][] tmp = BresenhamAlgorithm.rasterizeLine(p1, p2);
                        for (int[] p : tmp) {
                                allPoints.add(p);
                        }
                }

                int[] p1 = getPointAt(length() - 1);
                int[] p2 = getPointAt(0);
                int[][] tmp = BresenhamAlgorithm.rasterizeLine(p1, p2);
                for (int[] p : tmp) {
                        allPoints.add(p);
                }

                double stepsize = (Math.max(1.0, (double) allPoints.size()
                                / (double) (maxNumPoints + 1)));
                ExtendedPolygon res = new ExtendedPolygon();

                for (double i = 0; i < allPoints.size() - 2 * stepsize; i += stepsize) {
                        int[] p = allPoints.get((int) Math.round(i));
                        res.addPoint(p[0], p[1]);
                }

                return res;

        }

        /**
         * 
         * An iterator over the points.
         * 
         * @return the iterator
         */

        @Override
        public Iterator<int[]> iterator() {
                return new ContourIterator();
        }

        protected class ContourIterator implements Iterator<int[]> {
                protected int i = -1;

                public boolean hasNext() {
                        return i < length() - 1;
                }

                public void remove() {
                        throw new UnsupportedOperationException();
                }

                public int[] next() {
                        if (!hasNext())
                                throw new NoSuchElementException();
                        i++;
                        return new int[] { xpoints[i], ypoints[i] };
                }

        }

        /**
         * Shows images for debugging purposes: the lines along the normal
         * vectors at each point of the contour, ...
         * 
         * @param srcImg
         */
        public <T extends RealType<T>> void showDebugImage(Img<T> srcImg) {

                Img<ByteType> res = new ArrayImgFactory<ByteType>().create(
                                srcImg, new ByteType());

                RandomAccess<ByteType> resRA = res.randomAccess();

                RandomAccess<T> srcCur = srcImg.randomAccess();

                int samplePoints = 100;
                int radius = 20;
                Img<T> cImg = srcImg.factory().create(
                                new int[] { radius * 2, samplePoints },
                                srcImg.firstElement().createVariable());
                RandomAccess<T> cImgRA = cImg.randomAccess();

                int[][] line;
                ExtendedPolygon tmp = resamplePolygon(samplePoints);
                for (int i = 0; i < tmp.length(); i++) {
                        line = PolygonTools.getLineAt(tmp.getPointAt(i),
                                        tmp.getNormalVecAtPoint(i), radius);
                        int j = 0;
                        for (int[] p : line) {
                                resRA.setPosition(p[0], 0);
                                resRA.setPosition(p[1], 1);
                                resRA.get().set((byte) 50);

                                srcCur.setPosition(p[0], 0);
                                srcCur.setPosition(p[1], 1);

                                cImgRA.setPosition(j, 0);
                                cImgRA.setPosition(i, 1);

                                cImgRA.get().setReal(
                                                srcCur.get().getRealDouble());
                                j++;

                        }
                        resRA.setPosition(tmp.getPointAt(i)[0], 0);
                        resRA.setPosition(tmp.getPointAt(i)[1], 1);
                        resRA.get().set((byte) 128);

                }
                // loci.formats.gui.AWTImageTools.showInFrame(cImg, "contour",
                // 4);
                // AWTImageTools.showInFrame(res, "contours", 2);
        }

        /**
         * @return
         */
        public void getCenterOfGravityAndUpdate(int[] currentPos) {

                int x = 0;
                int y = 0;

                double total = 0;
                for (int[] p : this) {
                        x += p[0];
                        y += p[1];

                        total++;
                }

                currentPos[0] = (int) Math.round(x / (total));
                currentPos[1] = (int) Math.round(y / (total));

        }

}
