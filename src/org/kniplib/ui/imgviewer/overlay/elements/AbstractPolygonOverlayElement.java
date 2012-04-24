package org.kniplib.ui.imgviewer.overlay.elements;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.RealPoint;
import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.roi.PolygonRegionOfInterest;

import org.kniplib.ui.imgviewer.overlay.OverlayElement2D;
import org.kniplib.ui.imgviewer.overlay.OverlayElementStatus;

/**
 * 
 * @author dietzc, fschoenenberger, hornm
 */
public abstract class AbstractPolygonOverlayElement<L extends Comparable<L>>
                extends OverlayElement2D<L> implements Externalizable {

        protected static final int DRAWING_RADIUS = 2;

        protected boolean m_isClosed;

        protected Polygon m_poly;

        protected PolygonRegionOfInterest m_roi;

        protected abstract void renderPointOutline(Graphics2D g);

        protected abstract void renderPointInterior(Graphics2D g);

        public abstract void translate(int m_selectedIndex, long x, long y);

        public AbstractPolygonOverlayElement() {
                super();
        }

        public AbstractPolygonOverlayElement(long[] planePos,
                        int[] orientation, String... labels) {
                this(new Polygon(), planePos, orientation, labels);
        }

        public AbstractPolygonOverlayElement(Polygon poly, long[] planePos,
                        int[] orientation, String... labels) {
                super(planePos, orientation, labels);
                m_poly = poly;
                m_roi = new PolygonRegionOfInterest();
                for (int i = 0; i < m_poly.npoints; i++) {
                        m_roi.addVertex(i, new RealPoint(
                                        (double) m_poly.xpoints[i],
                                        (double) m_poly.ypoints[i]));
                }
        }

        public Polygon getPolygon() {
                return m_poly;
        }

        public void resetPolygon() {
                m_poly.reset();
        }

        public void close() {
                m_isClosed = true;
        }

        @Override
        public boolean add(long x, long y) {
                if (m_isClosed)
                        return false;

                m_poly.addPoint((int) x, (int) y);
                m_roi.addVertex(m_roi.getVertexCount(), new RealPoint(
                                (double) x, (double) y));

                return true;
        }

        @Override
        public Rectangle getBoundingBox() {
                return m_poly.getBounds();
        }

        @Override
        public void renderInterior(Graphics2D g) {
                if (m_isClosed) {
                        g.fill(m_poly);
                }

                if (getStatus() == OverlayElementStatus.ACTIVE
                                || getStatus() == OverlayElementStatus.DRAWING) {
                        renderPointInterior(g);
                }
        }

        @Override
        public void renderOutline(Graphics2D g) {

                if (getStatus() == OverlayElementStatus.ACTIVE
                                || getStatus() == OverlayElementStatus.DRAWING) {
                        renderPointOutline(g);
                }

                if (m_isClosed)
                        g.draw(m_poly);
                else
                        g.drawPolyline(m_poly.xpoints, m_poly.ypoints,
                                        m_poly.npoints);
        }

        public int getPointIndexByPosition(int x, int y, int pickingDelta) {

                for (int i = 0; i < m_poly.npoints; i++) {
                        if (m_poly.xpoints[i] - pickingDelta < x
                                        && x < m_poly.xpoints[i] + pickingDelta
                                        && m_poly.ypoints[i] - pickingDelta < y
                                        && y < m_poly.ypoints[i] + pickingDelta) {
                                return i;
                        }
                }
                return -1;
        }

        @Override
        public void translate(long x, long y) {
                for (int i = 0; i < m_poly.npoints; i++) {
                        m_poly.xpoints[i] += x;
                        m_poly.ypoints[i] += y;
                        m_roi.setVertexPosition(i, new RealPoint(
                                        (double) m_poly.xpoints[i],
                                        (double) m_poly.ypoints[i]));
                }
                m_poly.invalidate();
        }

        @Override
        public IterableRegionOfInterest getRegionOfInterest() {
                return m_roi;
        }

        @Override
        public boolean containsPoint(long x, long y) {
                return m_poly.contains((int) x, (int) y);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                super.writeExternal(out);
                out.writeObject(m_poly);
                out.writeBoolean(m_isClosed);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                        ClassNotFoundException {
                super.readExternal(in);
                m_poly = (Polygon) in.readObject();
                m_isClosed = in.readBoolean();

                m_roi = new PolygonRegionOfInterest();

                for (int n = 0; n < m_poly.npoints; n++) {
                        m_roi.addVertex(n, new RealPoint(
                                        (double) m_poly.xpoints[n],
                                        (double) m_poly.ypoints[n]));
                }
        }

}
