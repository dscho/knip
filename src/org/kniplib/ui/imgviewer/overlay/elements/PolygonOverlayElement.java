package org.kniplib.ui.imgviewer.overlay.elements;

import java.awt.Graphics2D;
import java.awt.Polygon;

/**
 * 
 * @author dietzc, hornm, schoenenbergerf
 */
public class PolygonOverlayElement<L extends Comparable<L>> extends
                AbstractPolygonOverlayElement<L> {

        public PolygonOverlayElement() {
                super();
        }

        public PolygonOverlayElement(long[] planePos, int[] orientation,
                        final String... labels) {
                this(new Polygon(), planePos, orientation, labels);
        }

        public PolygonOverlayElement(Polygon poly, long[] planePos,
                        int[] orientation, String... labels) {
                super(poly, planePos, orientation, labels);
        }

        @Override
        public void translate(int m_selectedIndex, long x, long y) {
                m_poly.xpoints[m_selectedIndex] += x;
                m_poly.ypoints[m_selectedIndex] += y;
                m_poly.invalidate();
        }

        @Override
        protected void renderPointInterior(Graphics2D g) {
                for (int i = 0; i < m_poly.npoints; i++) {
                        g.fillOval(m_poly.xpoints[i] - DRAWING_RADIUS,
                                        m_poly.ypoints[i] - DRAWING_RADIUS,
                                        2 * DRAWING_RADIUS, 2 * DRAWING_RADIUS);
                }

        }

        @Override
        protected void renderPointOutline(Graphics2D g) {
                for (int i = 0; i < m_poly.npoints; i++) {
                        g.drawOval(m_poly.xpoints[i] - DRAWING_RADIUS,
                                        m_poly.ypoints[i] - DRAWING_RADIUS,
                                        2 * DRAWING_RADIUS, 2 * DRAWING_RADIUS);
                }
        }

}
