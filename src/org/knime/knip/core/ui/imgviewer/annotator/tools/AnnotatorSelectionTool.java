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

public class AnnotatorSelectionTool extends AnnotatorTool<OverlayElement2D<String>> {

    private final List<OverlayElement2D<String>> m_elements;

    private int m_selectedIndex = -1;

    public AnnotatorSelectionTool() {
        super("Selection", "tool-select.png");
        m_elements = new ArrayList<OverlayElement2D<String>>();

    }

    private void clearSelectedElements() {
        for (final OverlayElement2D<String> element : m_elements) {
            element.setStatus(OverlayElementStatus.IDLE);
        }
        m_elements.clear();
    }

    @Override
    public void fireFocusLost(final Overlay<String> overlay) {
        m_selectedIndex = -1;
        if (setCurrentOverlayElement(null, null)) {
            fireStateChanged();
        }

        tryToFireStateChanged(overlay);
    }

    @Override
    public void setLabelsCurrentElements(final Overlay<String> overlay, final String[] selectedLabels) {
        if (!m_elements.isEmpty()) {
            for (final OverlayElement2D<String> element : m_elements) {
                element.setLabels(selectedLabels);
            }
            overlay.fireOverlayChanged();
        }
    }

    @Override
    public void onMouseDoubleClickLeft(final ImgViewerMouseEvent e,
                                       final OverlayElement2D<String> currentOverlayElement,
                                       final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                       final String... labels) {
        // Nothing to do here

    }

    @Override
    public void onMousePressedLeft(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                   final String... labels) {
        final List<OverlayElement2D<String>> elements =
                overlay.getElementsByPosition(selection.getPlanePos(e.getPosX(), e.getPosY()),
                                              selection.getDimIndices());

        if (!elements.isEmpty()) {

            if (!e.isControlDown()) {

                if (elements.get(0) != currentOverlayElement) {
                    clearSelectedElements();

                    m_elements.add(elements.get(0));

                    if (setCurrentOverlayElement(OverlayElementStatus.ACTIVE, m_elements.get(0))) {
                        fireStateChanged();
                    }
                } else if (currentOverlayElement instanceof AbstractPolygonOverlayElement) {
                    m_selectedIndex =
                            ((AbstractPolygonOverlayElement<String>)currentOverlayElement).getPointIndexByPosition(e
                                    .getPosX(), e.getPosY(), 3);
                }

            } else {
                m_elements.add(elements.get(0));
                elements.get(0).setStatus(OverlayElementStatus.ACTIVE);
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
    public void onMouseReleasedLeft(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                    final String... labels) {
        // Nothing to do here
    }

    @Override
    public void onMouseDraggedLeft(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                   final String... labels) {

        if (!m_elements.isEmpty()) {
            final long[] pos = selection.getPlanePos(e.getPosX(), e.getPosY()).clone();

            for (int d = 0; d < pos.length; d++) {
                pos[d] -= getDragPoint()[d];
            }

            if (currentOverlayElement != null) {
                if ((m_selectedIndex == -1)) {
                    currentOverlayElement.translate(pos);
                } else {
                    ((AbstractPolygonOverlayElement<String>)currentOverlayElement)
                            .translate(m_selectedIndex, pos[selection.getPlaneDimIndex1()],
                                       pos[selection.getPlaneDimIndex2()]);
                }
            }
            fireStateChanged();
        }

    }

    @Override
    public void onMouseDoubleClickRight(final ImgViewerMouseEvent e,
                                        final OverlayElement2D<String> currentOverlayElement,
                                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                        final String... labels) {
        m_selectedIndex = -1;
    }

    @Override
    public void onMousePressedRight(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                    final String... labels) {
        m_selectedIndex = -1;

    }

    @Override
    public void onMouseReleasedRight(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                     final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                     final String... labels) {
        m_selectedIndex = -1;
        overlay.removeAll(m_elements);
        if (setCurrentOverlayElement(OverlayElementStatus.IDLE, null)) {
            fireStateChanged();
        }

    }

    @Override
    public void onMouseDraggedRight(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                    final String... labels) {
        m_selectedIndex = -1;
    }

    @Override
    public void onMouseDoubleClickMid(final ImgViewerMouseEvent e,
                                      final OverlayElement2D<String> currentOverlayElement,
                                      final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                      final String... labels) {
        // Nothing to do here
    }

    @Override
    public void onMousePressedMid(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                  final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                  final String... labels) {
        // Nothing to do here
    }

    @Override
    public void onMouseReleasedMid(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                   final String... labels) {

    }

    @Override
    public void onMouseDraggedMid(final ImgViewerMouseEvent e, final OverlayElement2D<String> currentOverlayElement,
                                  final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                  final String... labels) {
        // Nothing to do here
    }
}
