package org.kniplib.ui.imgviewer.annotator.tools;

import org.kniplib.ui.imgviewer.events.ImgViewerMouseEvent;
import org.kniplib.ui.imgviewer.events.PlaneSelectionEvent;
import org.kniplib.ui.imgviewer.overlay.Overlay;
import org.kniplib.ui.imgviewer.overlay.OverlayElementStatus;
import org.kniplib.ui.imgviewer.overlay.elements.SplineOverlayElement;

public class AnnotatorSplineTool extends
                AnnotationDrawingTool<SplineOverlayElement<String>> {

        public AnnotatorSplineTool() {
                super("Spline", "tool-spline.png");
        }

        @Override
        public void fireFocusLost(Overlay<String> overlay) {
                if (setCurrentOverlayElement(null, null)) {
                        fireStateChanged();
                }
        }

        @Override
        public void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        SplineOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                currentOverlayElement.close();
                currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                fireStateChanged();

        }

        @SuppressWarnings("unchecked")
        @Override
        public void onMousePressedLeft(ImgViewerMouseEvent e,
                        SplineOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                if (currentOverlayElement == null
                                || (currentOverlayElement.getStatus() != OverlayElementStatus.DRAWING)) {
                        currentOverlayElement = new SplineOverlayElement<String>(
                                        selection.getPlanePos(e.getPosX(),
                                                        e.getPosY()),
                                        selection.getDimIndices(), labels);
                        overlay.addElement(currentOverlayElement);
                        setCurrentOverlayElement(OverlayElementStatus.DRAWING,
                                        currentOverlayElement);
                }

                currentOverlayElement.add(e.getPosX(), e.getPosY());
                fireStateChanged();
        }

        @Override
        public void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        SplineOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        SplineOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }

}
