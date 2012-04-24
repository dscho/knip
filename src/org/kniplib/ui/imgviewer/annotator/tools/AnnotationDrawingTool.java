package org.kniplib.ui.imgviewer.annotator.tools;

import org.kniplib.ui.imgviewer.annotator.AnnotatorTool;
import org.kniplib.ui.imgviewer.events.ImgViewerMouseEvent;
import org.kniplib.ui.imgviewer.events.PlaneSelectionEvent;
import org.kniplib.ui.imgviewer.overlay.Overlay;
import org.kniplib.ui.imgviewer.overlay.OverlayElement2D;

public abstract class AnnotationDrawingTool<O extends OverlayElement2D<String>>
                extends AnnotatorTool<O> {

        public AnnotationDrawingTool(String name, String iconPath) {
                super(name, iconPath);
        }

        @Override
        public void onMouseDoubleClickRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMousePressedRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseReleasedRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                for (OverlayElement2D<String> element : overlay
                                .getElementsByPosition(
                                                selection.getPlanePos(
                                                                e.getPosX(),
                                                                e.getPosY()),
                                                selection.getDimIndices())) {
                        if (overlay.removeElement(element)) {
                                fireStateChanged();
                        }
                        break;
                }
        }

        @Override
        public void onMouseDraggedRight(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMouseDoubleClickMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMousePressedMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMouseDraggedMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMouseReleasedMid(ImgViewerMouseEvent e,
                        O currentOverlayElement, PlaneSelectionEvent selection,
                        Overlay<String> overlay, String... labels) {
                // Nothing to do here
        }

        @Override
        public void fireFocusLost(Overlay<String> overlay) {
                if (setCurrentOverlayElement(null, null)) {
                        fireStateChanged();
                }

                tryToFireStateChanged(overlay);
        }
}
