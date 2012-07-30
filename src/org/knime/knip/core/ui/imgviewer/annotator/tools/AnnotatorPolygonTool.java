package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.PolygonOverlayElement;

public class AnnotatorPolygonTool extends
                AnnotationDrawingTool<PolygonOverlayElement<String>> {

        public AnnotatorPolygonTool() {
                super("Polygon", "tool-poly.png");
        }

        @Override
        public void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        PolygonOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                currentOverlayElement.close();
                currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                overlay.fireOverlayChanged();
        }

        @Override
        public void onMousePressedLeft(ImgViewerMouseEvent e,
                        PolygonOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

        }

        @SuppressWarnings("unchecked")
        @Override
        public void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        PolygonOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                if (currentOverlayElement == null
                                || (currentOverlayElement.getStatus() != OverlayElementStatus.DRAWING)) {
                        currentOverlayElement = new PolygonOverlayElement<String>(
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
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        PolygonOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here
        }
}
