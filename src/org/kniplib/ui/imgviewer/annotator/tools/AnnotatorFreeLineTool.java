package org.kniplib.ui.imgviewer.annotator.tools;

import org.kniplib.ui.imgviewer.events.ImgViewerMouseEvent;
import org.kniplib.ui.imgviewer.events.PlaneSelectionEvent;
import org.kniplib.ui.imgviewer.overlay.Overlay;
import org.kniplib.ui.imgviewer.overlay.OverlayElementStatus;
import org.kniplib.ui.imgviewer.overlay.elements.FreeFormOverlayElement;

public class AnnotatorFreeLineTool extends
                AnnotationDrawingTool<FreeFormOverlayElement<String>> {

        public AnnotatorFreeLineTool() {
                super("Free Line", "tool-freeline.png");
        }

        @Override
        public void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        FreeFormOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {
                // Nothing to do here

        }

        @SuppressWarnings("unchecked")
        @Override
        public void onMousePressedLeft(ImgViewerMouseEvent e,
                        FreeFormOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                FreeFormOverlayElement<String> element = new FreeFormOverlayElement<String>(
                                selection.getPlanePos(e.getPosX(), e.getPosY()),
                                selection.getDimIndices(), false, labels);
                overlay.addElement(element);

                element.add(e.getPosX(), e.getPosY());

                if (setCurrentOverlayElement(OverlayElementStatus.DRAWING,
                                element)) {
                        fireStateChanged();
                }
        }

        @Override
        public void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        FreeFormOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                fireStateChanged();
        }

        @Override
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        FreeFormOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                if (currentOverlayElement.getStatus() == OverlayElementStatus.DRAWING) {
                        currentOverlayElement.add(e.getPosX(), e.getPosY());
                        fireStateChanged();
                }
        }
}
