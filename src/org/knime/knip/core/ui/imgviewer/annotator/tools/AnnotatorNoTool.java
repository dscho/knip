package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorTool;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;

public class AnnotatorNoTool extends AnnotatorTool {

        public AnnotatorNoTool() {
                super("normal mouse", "handchen.png");
        }

        public AnnotatorNoTool(String name) {
                super(name, "handchen.png");
        }

        @Override
        public void onMouseDoubleClickLeft(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMousePressedLeft(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseReleasedLeft(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseDraggedLeft(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseDoubleClickRight(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMousePressedRight(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseReleasedRight(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseDraggedRight(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseDoubleClickMid(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMousePressedMid(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseReleasedMid(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void onMouseDraggedMid(ImgViewerMouseEvent e,
                        OverlayElement2D currentOverlayElement,
                        PlaneSelectionEvent selection, Overlay overlay,
                        String... labels) {
        }

        @Override
        public void fireFocusLost(Overlay overlay) {
        }


}
