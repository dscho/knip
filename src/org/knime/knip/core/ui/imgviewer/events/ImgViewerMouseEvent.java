package org.knime.knip.core.ui.imgviewer.events;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.knime.knip.core.ui.event.KNIPEvent;

/**
 *
 * @author dietzc, hornm, schoenenbergerf
 */
public abstract class ImgViewerMouseEvent implements KNIPEvent {

        private final int m_id;

        private boolean m_consumed;

        /**
         * Full nD position inside the image coordinate space.
         */
        private final boolean m_left;

        private final boolean m_mid;

        private final boolean m_right;

        private final int m_clickCount;

        private final boolean m_isPopupTrigger;

        private final boolean m_isControlDown;

        private final double[] m_factors;

        private final int m_posX;

        private final int m_posY;

        private boolean m_isInside;

        private final MouseEvent m_e;

        public ImgViewerMouseEvent(final MouseEvent e, final double[] factors,
                        final int imgWidth,
                        final int imgHeight) {

                m_factors = factors;

                m_e = e;
                setInside(isInsideImgView(imgWidth, imgHeight));

                m_posX = (int) Math.min(e.getX() / m_factors[0], imgWidth);
                m_posY = (int) Math.min(e.getY() / m_factors[1], imgHeight);

                m_id = e.getID();
                m_consumed = false;
                m_left = (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0
                                || e.getButton() == MouseEvent.BUTTON1;
                m_mid = (e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0
                                || e.getButton() == MouseEvent.BUTTON2;
                m_right = (e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0
                                || e.getButton() == MouseEvent.BUTTON3;
                m_clickCount = e.getClickCount();
                m_isPopupTrigger = e.isPopupTrigger();
                m_isControlDown = e.isControlDown();
        }

        /*
         * Checks weather the mouse click appeared inside the image view pane or
         * not!
         */
        public boolean isInsideImgView(final long dimA, final long dimB) {
                // if ((e.getX() / m_factor > m_imgWidth || e.getX() / m_factor
                // < 0
                // || e.getY() / m_factor > m_imgHeight|| e.getY() / m_factor <
                // 0))
                // System.out.println("test");

                return !(m_e.getX() / m_factors[0] >= dimA
                                || m_e.getX() / m_factors[0] < 0
                                || m_e.getY() / m_factors[1] >= dimB || m_e
                                .getY() / m_factors[1] < 0);
        }

        public boolean wasConsumed() {
                return m_consumed;
        }

        public void consume() {
                m_consumed = true;
        }

        public int getID() {
                return m_id;
        }

        public boolean isLeftDown() {
                return m_left;
        }

        public boolean isMidDown() {
                return m_mid;
        }

        public boolean isRightDown() {
                return m_right;
        }

        public int getClickCount() {
                return m_clickCount;
        }

        public boolean isPopupTrigger() {
                return m_isPopupTrigger;
        }

        public boolean isControlDown() {
                return m_isControlDown;
        }

        public int getPosX() {
                return m_posX;
        }

        public int getPosY() {
                return m_posY;
        }

        public boolean isInside() {
                return m_isInside;
        }

        public void setInside(final boolean m_isInside) {
                this.m_isInside = m_isInside;
        }
}
