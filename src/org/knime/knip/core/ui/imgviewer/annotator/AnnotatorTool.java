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

public abstract class AnnotatorTool<O extends OverlayElement2D<String>> {

        private final String m_name;

        private final String m_iconPath;

        private long[] m_dragPoint;

        private boolean m_stateChanged;

        private O m_currentOverlayElement;

        public abstract void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMousePressedLeft(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseDoubleClickRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMousePressedRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseReleasedRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseDraggedRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseDoubleClickMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMousePressedMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseReleasedMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void onMouseDraggedMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels);

        public abstract void fireFocusLost(Overlay<String> overlay);

        public AnnotatorTool(String name, String iconPath) {
                m_name = name;
                m_iconPath = iconPath;
                m_stateChanged = false;
        }

        public void onMouseDoubleClick(ImgViewerMouseEvent e,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                if (!e.isInside()) {
                        setCurrentOverlayElement(null, null);
                        fireStateChanged();
                } else if (e.isLeftDown()) {
                        onMouseDoubleClickLeft(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isRightDown()) {
                        onMouseDoubleClickRight(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isMidDown()) {
                        onMouseDoubleClickMid(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                }

                tryToFireStateChanged(overlay);
        }

        public void onMousePressed(ImgViewerMouseEvent e,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                m_dragPoint = selection.getPlanePos(e.getPosX(), e.getPosY());

                if (!e.isInside()) {
                        setCurrentOverlayElement(null, null);
                        fireStateChanged();
                } else if (e.isLeftDown()) {
                        onMousePressedLeft(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isRightDown()) {
                        onMousePressedRight(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isMidDown()) {
                        onMousePressedMid(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                }

                tryToFireStateChanged(overlay);
        }

        public void onMouseReleased(ImgViewerMouseEvent e,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                if (!e.isInside()) {
                        if (m_currentOverlayElement != null
                                        && m_currentOverlayElement.getStatus() != OverlayElementStatus.ACTIVE) {
                                m_currentOverlayElement
                                                .setStatus(OverlayElementStatus.ACTIVE);
                                fireStateChanged();
                        }
                } else if (e.isLeftDown()) {
                        onMouseReleasedLeft(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isRightDown()) {
                        onMouseReleasedRight(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isMidDown()) {
                        onMouseReleasedMid(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                }

                tryToFireStateChanged(overlay);

        }

        public void onMouseDragged(ImgViewerMouseEvent e,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                if (!e.isInside()) {
                        return;
                }

                if (e.isLeftDown()) {
                        onMouseDraggedLeft(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isRightDown()) {
                        onMouseDraggedRight(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                } else if (e.isMidDown()) {
                        onMouseDraggedMid(e, m_currentOverlayElement,
                                        selection, overlay, labels);
                }

                m_dragPoint = selection.getPlanePos(e.getPosX(), e.getPosY());

                tryToFireStateChanged(overlay);
        }

        protected void tryToFireStateChanged(Overlay<String> overlay) {
                if (m_stateChanged && overlay != null) {
                        m_stateChanged = false;
                        overlay.fireOverlayChanged();
                }
        }

        public String getName() {
                return m_name;
        }

        public String getIconPath() {
                return m_iconPath;
        }

        public final void setButtonIcon(final AbstractButton jb,
                        final String path) {
                URL icon = getClass().getClassLoader().getResource(
                                getClass().getPackage().getName()
                                                .replace('.', '/')
                                                + "/" + path);
                jb.setHorizontalAlignment(SwingConstants.LEFT);
                if (icon != null) {
                        jb.setIcon(new ImageIcon(icon));
                }
        }

        // Helpers
        protected boolean setCurrentOverlayElement(OverlayElementStatus status,
                        O os) {
                if ((m_currentOverlayElement == null && os == null || (m_currentOverlayElement == os && m_currentOverlayElement
                                .getStatus() == status))) {
                        return false;
                }

                if (os == null) {
                        m_currentOverlayElement
                                        .setStatus(OverlayElementStatus.IDLE);
                        m_currentOverlayElement = null;
                } else {
                        os.setStatus(status == null ? os.getStatus() : status);

                        if (m_currentOverlayElement != null) {
                                m_currentOverlayElement
                                                .setStatus(OverlayElementStatus.IDLE);
                        }
                        m_currentOverlayElement = os;
                }

                return true;

        }

        protected void fireStateChanged() {
                m_stateChanged = true;
        }

        protected final long[] getDragPoint() {
                return m_dragPoint;
        }

        protected final void setDragPoint(long[] dragPoint) {
                m_dragPoint = dragPoint;
        }

        public void setLabelsCurrentElements(Overlay<String> overlay,
                        String[] selectedLabels) {

                if (m_currentOverlayElement != null) {
                        m_currentOverlayElement.setLabels(selectedLabels);
                        overlay.fireOverlayChanged();
                }
        }

}
