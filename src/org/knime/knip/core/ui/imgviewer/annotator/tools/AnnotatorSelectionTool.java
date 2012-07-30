package org.knime.knip.core.ui.imgviewer.annotator.tools;

import java.util.ArrayList;
import java.util.List;

import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorTool;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.AbstractPolygonOverlayElement;

public class AnnotatorSelectionTool extends
                AnnotatorTool<OverlayElement2D<String>> {

        private final List<OverlayElement2D<String>> m_elements;

        private int m_selectedIndex = -1;

        public AnnotatorSelectionTool() {
                super("Selection", "tool-select.png");
                m_elements = new ArrayList<OverlayElement2D<String>>();

        }

        private void clearSelectedElements() {
                for (OverlayElement2D<String> element : m_elements) {
                        element.setStatus(OverlayElementStatus.IDLE);
                }
                m_elements.clear();
        }

        @Override
        public void fireFocusLost(Overlay<String> overlay) {
                m_selectedIndex = -1;
                if (setCurrentOverlayElement(null, null)) {
                        fireStateChanged();
                }

                tryToFireStateChanged(overlay);
        }

        @Override
        public void setLabelsCurrentElements(Overlay<String> overlay,
                        String[] selectedLabels) {
                if (!m_elements.isEmpty()) {
                        for (OverlayElement2D<String> element : m_elements) {
                                element.setLabels(selectedLabels);
                        }
                        overlay.fireOverlayChanged();
                }
        }

        @Override
        public void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMousePressedLeft(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                List<OverlayElement2D<String>> elements = overlay
                                .getElementsByPosition(
                                                selection.getPlanePos(
                                                                e.getPosX(),
                                                                e.getPosY()),
                                                selection.getDimIndices());

                if (!elements.isEmpty()) {

                        if (!e.isControlDown()) {

                                if (elements.get(0) != currentOverlayElement) {
                                        clearSelectedElements();

                                        m_elements.add(elements.get(0));

                                        if (setCurrentOverlayElement(
                                                        OverlayElementStatus.ACTIVE,
                                                        m_elements.get(0))) {
                                                fireStateChanged();
                                        }
                                } else if (currentOverlayElement instanceof AbstractPolygonOverlayElement) {
                                        m_selectedIndex = ((AbstractPolygonOverlayElement<String>) currentOverlayElement)
                                                        .getPointIndexByPosition(
                                                                        e.getPosX(),
                                                                        e.getPosY(),
                                                                        3);
                                }

                        } else {
                                m_elements.add(elements.get(0));
                                elements.get(0).setStatus(
                                                OverlayElementStatus.ACTIVE);
                                fireStateChanged();
                        }

                } else {
                        clearSelectedElements();
                        if (setCurrentOverlayElement(null, null)) {
                                fireStateChanged();
                        }
                }

        }

        @Override
        public void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                if (!m_elements.isEmpty()) {
                        long[] pos = selection.getPlanePos(e.getPosX(),
                                        e.getPosY()).clone();

                        for (int d = 0; d < pos.length; d++) {
                                pos[d] -= getDragPoint()[d];
                        }

                        if (m_selectedIndex == -1
                                        && currentOverlayElement != null) {
                                currentOverlayElement.translate(pos);
                        } else {
                                ((AbstractPolygonOverlayElement<String>) currentOverlayElement)
                                                .translate(m_selectedIndex,
                                                                pos[selection.getPlaneDimIndex1()],
                                                                pos[selection.getPlaneDimIndex2()]);
                        }
                        fireStateChanged();
                }

        }

        @Override
        public void onMouseDoubleClickRight(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                m_selectedIndex = -1;
        }

        @Override
        public void onMousePressedRight(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                m_selectedIndex = -1;

        }

        @Override
        public void onMouseReleasedRight(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                m_selectedIndex = -1;
                overlay.removeAll(m_elements);
                if (setCurrentOverlayElement(OverlayElementStatus.IDLE, null))
                        fireStateChanged();

        }

        @Override
        public void onMouseDraggedRight(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                m_selectedIndex = -1;
        }

        @Override
        public void onMouseDoubleClickMid(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMousePressedMid(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseReleasedMid(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

        }

        @Override
        public void onMouseDraggedMid(ImgViewerMouseEvent e,
                        OverlayElement2D<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }
}
