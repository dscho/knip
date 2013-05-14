package org.knime.knip.core.ui.imgviewer.annotator.tools;

import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElementStatus;
import org.knime.knip.core.ui.imgviewer.overlay.elements.FreeFormOverlayElement;

public class AnnotatorFreeLineTool extends
AnnotationDrawingTool<FreeFormOverlayElement<String>> {

    public AnnotatorFreeLineTool() {
        super("Free Line", "tool-freeline.png");
    }

    @Override
    public void onMouseDoubleClickLeft(final ImgViewerMouseEvent e,
                                       final FreeFormOverlayElement<String> currentOverlayElement,
                                       final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                       final String... labels) {
        // Nothing to do here

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMousePressedLeft(final ImgViewerMouseEvent e,
                                   final FreeFormOverlayElement<String> currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                   final String... labels) {

        final FreeFormOverlayElement<String> element = new FreeFormOverlayElement<String>(
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
    public void onMouseReleasedLeft(final ImgViewerMouseEvent e,
                                    final FreeFormOverlayElement<String> currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                    final String... labels) {

        currentOverlayElement.setStatus(OverlayElementStatus.ACTIVE);
        fireStateChanged();
    }

    @Override
    public void onMouseDraggedLeft(final ImgViewerMouseEvent e,
                                   final FreeFormOverlayElement<String> currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay<String> overlay,
                                   final String... labels) {

        if (currentOverlayElement.getStatus() == OverlayElementStatus.DRAWING) {
            currentOverlayElement.add(e.getPosX(), e.getPosY());
            fireStateChanged();
        }
    }
}
