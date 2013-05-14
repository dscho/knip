package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.SplineOverlayElement;

public class AnnotatorSplineTool extends
                AnnotationDrawingTool<SplineOverlayElement<String>> {

        public AnnotatorSplineTool() {
                super("Spline", "tool-spline.png");
        }

        @Override
        public void fireFocusLost(final Overlay<String> overlay) {
                if (setCurrentOverlayElement(null, null)) {
                        fireStateChanged();
                }
        }

        @Override
        public void onMouseDoubleClickLeft(final ImgViewerMouseEvent e,
                        final SplineOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {

                currentOverlayElement.close();
                currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
                fireStateChanged();

        }

        @SuppressWarnings("unchecked")
        @Override
        public void onMousePressedLeft(final ImgViewerMouseEvent e,
                        SplineOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
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
        public void onMouseReleasedLeft(final ImgViewerMouseEvent e,
                        final SplineOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
                // Nothing to do here
        }

        @Override
        public void onMouseDraggedLeft(final ImgViewerMouseEvent e,
                        final SplineOverlayElement<String> currentOverlayElement,
                        final PlaneSelectionEvent selection, final Overlay<String> overlay,
                        final String... labels) {
                // Nothing to do here
        }

}
