package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.RectangleOverlayElement;

public class AnnotatorRectangleTool extends
                AnnotationDrawingTool<RectangleOverlayElement<String>> {

        private long[] m_startPoint;

        public AnnotatorRectangleTool() {
                super("Rectangle", "tool-rect.png");
        }

        @Override
        public void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        RectangleOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onMousePressedLeft(ImgViewerMouseEvent e,
                        RectangleOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                m_startPoint = getDragPoint();
                RectangleOverlayElement<String> element = new RectangleOverlayElement<String>(
                                selection.getPlanePos(e.getPosX(), e.getPosY()),
                                selection.getDimIndices(), labels);
                overlay.addElement(element);
                element.setRectangle(
                                m_startPoint[selection.getPlaneDimIndex1()],
                                m_startPoint[selection.getPlaneDimIndex2()],
                                m_startPoint[selection.getPlaneDimIndex1()],
                                m_startPoint[selection.getPlaneDimIndex2()]);
                setCurrentOverlayElement(OverlayElementStatus.DRAWING, element);
                fireStateChanged();

        }

        @Override
        public void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        RectangleOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                fireStateChanged();

        }

        @Override
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        RectangleOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                if (currentOverlayElement.getStatus() == OverlayElementStatus.DRAWING) {
                        currentOverlayElement.setRectangle(
                                        m_startPoint[selection
                                                        .getPlaneDimIndex1()],
                                        m_startPoint[selection
                                                        .getPlaneDimIndex2()],
                                        e.getPosX(), e.getPosY());

                        fireStateChanged();
                }
        }
}
