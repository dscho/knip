package org.knime.knip.core.ui.imgviewer.overlay.elements;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.roi.RectangleRegionOfInterest;

import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;

/**
 * 
 * @author dietzc, fschoenenberger, hornm,
 */
public class PointOverlayElement<L extends Comparable<L>> extends
                OverlayElement2D<L> {

        private static final int DRAWING_RADIUS = 4;

        private int m_y;

        private int m_x;

        private RectangleRegionOfInterest m_roi;

        public PointOverlayElement() {
                //
        }

        public PointOverlayElement(final int x, final int y, final long[] pos, final int[] orientation,
                        final String... labels) {
                super(pos, orientation, labels);
                m_x = x;
                m_y = y;
                m_roi = new RectangleRegionOfInterest(new double[] { x, y },
                                new double[] { 1, 1 });
        }

        @Override
        public void translate(final long deltaX, final long deltaY) {
                m_x += deltaX;
                m_y += deltaY;
        }

        @Override
        public void renderInterior(final Graphics2D g) {
                g.fillOval(m_x - DRAWING_RADIUS, m_y
                                - DRAWING_RADIUS, 2 * DRAWING_RADIUS,
                                2 * DRAWING_RADIUS);
        }

        @Override
        public void renderOutline(final Graphics2D g) {
                g.drawOval(m_x - DRAWING_RADIUS, m_y
                                - DRAWING_RADIUS, 2 * DRAWING_RADIUS,
                                2 * DRAWING_RADIUS);
        }

        @Override
        public boolean containsPoint(final long x, final long y) {
                return m_x == x && m_y == y;
        }

        @Override
        public IterableRegionOfInterest getRegionOfInterest() {
                return m_roi;
        }

        @Override
        public boolean add(final long x, final long y) {
                return false;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
                super.writeExternal(out);
                out.writeInt(m_x);
                out.writeInt(m_y);

        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException,
                        ClassNotFoundException {
                super.readExternal(in);
                m_x = in.readInt();
                m_y = in.readInt();
                m_roi = new RectangleRegionOfInterest(
                                new double[] { m_x, m_y },
                                new double[] { 1, 1 });

        }

        @Override
        public Rectangle getBoundingBox() {
                return new Rectangle(m_x, m_y, 1, 1);
        }

}
