package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorTool;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;

public abstract class AnnotationDrawingTool<O extends OverlayElement2D<String>>
                extends AnnotatorTool<O> {

        public AnnotationDrawingTool(final String name, final String iconPath) {
                super(name, iconPath);
        }

        @Override
        public void onMouseDoubleClickRight(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMousePressedRight(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseReleasedRight(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                for (final OverlayElement2D<String> element : overlay
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
        public void onMouseDraggedRight(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMouseDoubleClickMid(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMousePressedMid(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMouseDraggedMid(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here

        }

        @Override
        public void onMouseReleasedMid(final ImgViewerMouseEvent e,
                        final O currentOverlayElement, final PlaneSelectionEvent selection,
                        final Overlay<String> overlay, final String... labels) {
                // Nothing to do here
        }

        @Override
        public void fireFocusLost(final Overlay<String> overlay) {
                if (setCurrentOverlayElement(null, null)) {
                        fireStateChanged();
                }

                tryToFireStateChanged(overlay);
        }
}
