package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.FreeFormOverlayElement;

public class AnnotatorFreeFormTool extends
                AnnotationDrawingTool<FreeFormOverlayElement<String>> {

        public AnnotatorFreeFormTool() {
                super("Free Form", "tool-freeform.png");
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
                                selection.getDimIndices(), true, labels);

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

                currentOverlayElement.close();
                currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                overlay.fireOverlayChanged();

        }

        @Override
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        FreeFormOverlayElement<String> currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay<String> overlay,
                        String... labels) {

                if (currentOverlayElement.getStatus() == OverlayElementStatus.DRAWING) {
                        currentOverlayElement.add(e.getPosX(), e.getPosY());
                        overlay.fireOverlayChanged();
                }

        }
}
