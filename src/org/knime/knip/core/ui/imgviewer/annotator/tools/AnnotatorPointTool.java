package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.PointOverlayElement;

public class AnnotatorPointTool extends
                AnnotationDrawingTool<PointOverlayElement<String>> {

        public AnnotatorPointTool() {
                super("Point", "tool-point.png");
        }

        @Override
        public void onMouseDoubleClickLeft(final ImgViewerMouseEvent e,
                        final PointOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
                // Nothing to do here
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onMousePressedLeft(final ImgViewerMouseEvent e,
                        final PointOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
                final PointOverlayElement<String> element = new PointOverlayElement<String>(
                                e.getPosX(),
                                e.getPosY(),
                                selection.getPlanePos(e.getPosX(), e.getPosY()),
                                selection.getDimIndices(), labels);

                overlay.addElement(element);

                if (setCurrentOverlayElement(OverlayElementStatus.ACTIVE,
                                element)) {
                        fireStateChanged();
                }
        }

        @Override
        public void onMouseReleasedLeft(final ImgViewerMouseEvent e,
                        final PointOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseDraggedLeft(final ImgViewerMouseEvent e,
                        final PointOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
                // Nothing to do here
        }
}
