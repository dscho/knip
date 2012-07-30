package org.knime.knip.core.ui.imgviewer.events;

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

        private final double m_factor;

        private final int m_posX;

        private final int m_posY;

        private boolean m_isInside;

        private final MouseEvent m_e;

        public ImgViewerMouseEvent(MouseEvent e, double factor, int imgWidth,
                        int imgHeight) {

                m_factor = factor;

                m_e = e;
                setInside(isInsideImgView(imgWidth, imgHeight));

                m_posX = (int) Math.min(e.getX() / m_factor, imgWidth);
                m_posY = (int) Math.min(e.getY() / m_factor, imgHeight);

                m_id = e.getID();
                m_consumed = false;
                m_left = (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0
                                || e.getButton() == MouseEvent.BUTTON1;
                m_mid = (e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0
                                || e.getButton() == MouseEvent.BUTTON2;
                m_right = (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0
                                || e.getButton() == MouseEvent.BUTTON3;
                m_clickCount = e.getClickCount();
                m_isPopupTrigger = e.isPopupTrigger();
                m_isControlDown = e.isControlDown();
        }

        /*
         * Checks weather the mouse click appeared inside the image view pane or
         * not!
         */
        public boolean isInsideImgView(long dimA, long dimB) {
                // if ((e.getX() / m_factor > m_imgWidth || e.getX() / m_factor
                // < 0
                // || e.getY() / m_factor > m_imgHeight|| e.getY() / m_factor <
                // 0))
                // System.out.println("test");

                return !(m_e.getX() / m_factor >= dimA
                                || m_e.getX() / m_factor < 0
                                || m_e.getY() / m_factor >= dimB || m_e.getY()
                                / m_factor < 0);
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

        public void setInside(boolean m_isInside) {
                this.m_isInside = m_isInside;
        }
}
