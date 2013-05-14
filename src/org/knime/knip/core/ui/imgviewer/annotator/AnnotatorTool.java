package org.knime.knip.core.ui.imgviewer.annotator;

import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;

/**
 *
 * @author dietyc
 * @param <O>
 */
public abstract class AnnotatorTool<O extends OverlayElement2D<String>> {

    private final String m_name;

    private final String m_iconPath;

    private long[] m_dragPoint;

    private boolean m_stateChanged;

    private O m_currentOverlayElement;

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseDoubleClickLeft(ImgViewerMouseEvent e, O currentOverlayElement,
                                                PlaneSelectionEvent selection, Overlay<String> overlay,
                                                String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMousePressedLeft(ImgViewerMouseEvent e, O currentOverlayElement,
                                            PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseReleasedLeft(ImgViewerMouseEvent e, O currentOverlayElement,
                                             PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseDraggedLeft(ImgViewerMouseEvent e, O currentOverlayElement,
                                            PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseDoubleClickRight(ImgViewerMouseEvent e, O currentOverlayElement,
                                                 PlaneSelectionEvent selection, Overlay<String> overlay,
                                                 String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMousePressedRight(ImgViewerMouseEvent e, O currentOverlayElement,
                                             PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseReleasedRight(ImgViewerMouseEvent e, O currentOverlayElement,
                                              PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseDraggedRight(ImgViewerMouseEvent e, O currentOverlayElement,
                                             PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void
            onMouseDoubleClickMid(ImgViewerMouseEvent e, O currentOverlayElement, PlaneSelectionEvent selection,
                                  Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMousePressedMid(ImgViewerMouseEvent e, O currentOverlayElement,
                                           PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseReleasedMid(ImgViewerMouseEvent e, O currentOverlayElement,
                                            PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    /**
     * @param e
     * @param currentOverlayElement
     * @param selection
     * @param overlay
     * @param labels
     */
    public abstract void onMouseDraggedMid(ImgViewerMouseEvent e, O currentOverlayElement,
                                           PlaneSelectionEvent selection, Overlay<String> overlay, String... labels);

    public abstract void fireFocusLost(Overlay<String> overlay);

    /**
     * @param name
     * @param iconPath
     */
    public AnnotatorTool(final String name, final String iconPath) {
        m_name = name;
        m_iconPath = iconPath;
        m_stateChanged = false;
    }

    /**
     * @param e
     * @param selection
     * @param overlay
     * @param labels
     */
    public void onMouseDoubleClick(final ImgViewerMouseEvent e, final PlaneSelectionEvent selection,
                                   final Overlay<String> overlay, final String... labels) {
        if (!e.isInside()) {
            setCurrentOverlayElement(null, null);
            fireStateChanged();
        } else if (e.isLeftDown()) {
            onMouseDoubleClickLeft(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isRightDown()) {
            onMouseDoubleClickRight(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isMidDown()) {
            onMouseDoubleClickMid(e, m_currentOverlayElement, selection, overlay, labels);
        }

        tryToFireStateChanged(overlay);
    }

    /**
     * @param e
     * @param selection
     * @param overlay
     * @param labels
     */
    public void onMousePressed(final ImgViewerMouseEvent e, final PlaneSelectionEvent selection,
                               final Overlay<String> overlay, final String... labels) {
        m_dragPoint = selection.getPlanePos(e.getPosX(), e.getPosY());

        if (!e.isInside()) {
            setCurrentOverlayElement(null, null);
            fireStateChanged();
        } else if (e.isLeftDown()) {
            onMousePressedLeft(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isRightDown()) {
            onMousePressedRight(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isMidDown()) {
            onMousePressedMid(e, m_currentOverlayElement, selection, overlay, labels);
        }

        tryToFireStateChanged(overlay);
    }

    /**
     * @param e
     * @param selection
     * @param overlay
     * @param labels
     */
    public void onMouseReleased(final ImgViewerMouseEvent e, final PlaneSelectionEvent selection,
                                final Overlay<String> overlay, final String... labels) {

        if (!e.isInside()) {
            if ((m_currentOverlayElement != null)
                    && (m_currentOverlayElement.getStatus() != OverlayElementStatus.ACTIVE)) {
                m_currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                fireStateChanged();
            }
        } else if (e.isLeftDown()) {
            onMouseReleasedLeft(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isRightDown()) {
            onMouseReleasedRight(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isMidDown()) {
            onMouseReleasedMid(e, m_currentOverlayElement, selection, overlay, labels);
        }

        tryToFireStateChanged(overlay);

    }

    /**
     * @param e
     * @param selection
     * @param overlay
     * @param labels
     */
    public void onMouseDragged(final ImgViewerMouseEvent e, final PlaneSelectionEvent selection,
                               final Overlay<String> overlay, final String... labels) {
        if (!e.isInside()) {
            return;
        }

        if (e.isLeftDown()) {
            onMouseDraggedLeft(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isRightDown()) {
            onMouseDraggedRight(e, m_currentOverlayElement, selection, overlay, labels);
        } else if (e.isMidDown()) {
            onMouseDraggedMid(e, m_currentOverlayElement, selection, overlay, labels);
        }

        m_dragPoint = selection.getPlanePos(e.getPosX(), e.getPosY());

        tryToFireStateChanged(overlay);
    }

    /**
     * @param overlay
     */
    protected void tryToFireStateChanged(final Overlay<String> overlay) {
        if (m_stateChanged && (overlay != null)) {
            m_stateChanged = false;
            overlay.fireOverlayChanged();
        }
    }

    @SuppressWarnings("javadoc")
    public String getName() {
        return m_name;
    }

    /**
     * @return
     */
    public String getIconPath() {
        return m_iconPath;
    }

    @SuppressWarnings("javadoc")
    public final void setButtonIcon(final AbstractButton jb, final String path) {
        final URL icon =
                getClass().getClassLoader().getResource(getClass().getPackage().getName().replace('.', '/') + "/"
                                                                + path);
        jb.setHorizontalAlignment(SwingConstants.LEFT);
        if (icon != null) {
            jb.setIcon(new ImageIcon(icon));
        }
    }

    /**
     * @param status
     * @param os
     * @return
     */
    // Helpers
    protected boolean setCurrentOverlayElement(final OverlayElementStatus status, final O os) {
        if ((((m_currentOverlayElement == null) && (os == null)) || ((m_currentOverlayElement == os) && (m_currentOverlayElement
                .getStatus() == status)))) {
            return false;
        }

        if (os == null) {
            m_currentOverlayElement.setStatus(OverlayElementStatus.IDLE);
            m_currentOverlayElement = null;
        } else {
            os.setStatus(status == null ? os.getStatus() : status);

            if (m_currentOverlayElement != null) {
                m_currentOverlayElement.setStatus(OverlayElementStatus.IDLE);
            }
            m_currentOverlayElement = os;
        }

        return true;

    }

    @SuppressWarnings("javadoc")
    protected void fireStateChanged() {
        m_stateChanged = true;
    }

    @SuppressWarnings("javadoc")
    protected final long[] getDragPoint() {
        return m_dragPoint;
    }

    @SuppressWarnings("javadoc")
    protected final void setDragPoint(final long[] dragPoint) {
        m_dragPoint = dragPoint.clone();
    }

    @SuppressWarnings("javadoc")
    public void setLabelsCurrentElements(final Overlay<String> overlay, final String[] selectedLabels) {

        if (m_currentOverlayElement != null) {
            m_currentOverlayElement.setLabels(selectedLabels);
            overlay.fireOverlayChanged();
        }
    }

}
