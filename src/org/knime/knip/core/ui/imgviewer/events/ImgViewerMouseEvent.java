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

    private final int m_posX;

    private final int m_posY;

    private boolean m_isInside;

    private final MouseEvent m_e;

    private final double m_factorA;

    private final double m_factorB;

    /**
     * @param e
     * @param factors
     * @param imgWidth
     * @param imgHeight
     */
    public ImgViewerMouseEvent(final MouseEvent e, final double[] factors, final int imgWidth, final int imgHeight) {

        m_factorA = factors[0];
        m_factorB = factors[1];

        m_e = e;
        setInside(isInsideImgView(imgWidth, imgHeight));

        m_posX = (int)Math.min(e.getX() / m_factorA, imgWidth);
        m_posY = (int)Math.min(e.getY() / m_factorB, imgHeight);

        m_id = e.getID();
        m_consumed = false;
        m_left = ((e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0) || (e.getButton() == MouseEvent.BUTTON1);
        m_mid = ((e.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) || (e.getButton() == MouseEvent.BUTTON2);
        m_right = ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) || (e.getButton() == MouseEvent.BUTTON3);
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

        return !(((m_e.getX() / m_factorA) >= dimA) || ((m_e.getX() / m_factorA) < 0)
                || ((m_e.getY() / m_factorB) >= dimB) || ((m_e.getY() / m_factorB) < 0));
    }

    /**
     * @return
     */
    public boolean wasConsumed() {
        return m_consumed;
    }

    /**
     *
     */
    public void consume() {
        m_consumed = true;
    }

    /**
     * @return
     */
    public int getID() {
        return m_id;
    }

    /**
     * @return
     */
    public boolean isLeftDown() {
        return m_left;
    }

    /**
     * @return
     */
    public boolean isMidDown() {
        return m_mid;
    }

    /**
     * @return
     */
    public boolean isRightDown() {
        return m_right;
    }

    /**
     * @return
     */
    public int getClickCount() {
        return m_clickCount;
    }

    /**
     * @return
     */
    public boolean isPopupTrigger() {
        return m_isPopupTrigger;
    }

    /**
     * @return
     */
    public boolean isControlDown() {
        return m_isControlDown;
    }

    /**
     * @return
     */
    public int getPosX() {
        return m_posX;
    }

    /**
     * @return
     */
    public int getPosY() {
        return m_posY;
    }

    /**
     * @return
     */
    public boolean isInside() {
        return m_isInside;
    }

    /**
     * @param m_isInside
     */
    public void setInside(final boolean m_isInside) {
        this.m_isInside = m_isInside;
    }
}
