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

    public AnnotatorNoTool(final String name) {
        super(name, "handchen.png");
    }

    @Override
    public void onMouseDoubleClickLeft(final ImgViewerMouseEvent e,
                                       final OverlayElement2D currentOverlayElement,
                                       final PlaneSelectionEvent selection, final Overlay overlay,
                                       final String... labels) {
    }

    @Override
    public void onMousePressedLeft(final ImgViewerMouseEvent e,
                                   final OverlayElement2D currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay overlay,
                                   final String... labels) {
    }

    @Override
    public void onMouseReleasedLeft(final ImgViewerMouseEvent e,
                                    final OverlayElement2D currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay overlay,
                                    final String... labels) {
    }

    @Override
    public void onMouseDraggedLeft(final ImgViewerMouseEvent e,
                                   final OverlayElement2D currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay overlay,
                                   final String... labels) {
    }

    @Override
    public void onMouseDoubleClickRight(final ImgViewerMouseEvent e,
                                        final OverlayElement2D currentOverlayElement,
                                        final PlaneSelectionEvent selection, final Overlay overlay,
                                        final String... labels) {
    }

    @Override
    public void onMousePressedRight(final ImgViewerMouseEvent e,
                                    final OverlayElement2D currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay overlay,
                                    final String... labels) {
    }

    @Override
    public void onMouseReleasedRight(final ImgViewerMouseEvent e,
                                     final OverlayElement2D currentOverlayElement,
                                     final PlaneSelectionEvent selection, final Overlay overlay,
                                     final String... labels) {
    }

    @Override
    public void onMouseDraggedRight(final ImgViewerMouseEvent e,
                                    final OverlayElement2D currentOverlayElement,
                                    final PlaneSelectionEvent selection, final Overlay overlay,
                                    final String... labels) {
    }

    @Override
    public void onMouseDoubleClickMid(final ImgViewerMouseEvent e,
                                      final OverlayElement2D currentOverlayElement,
                                      final PlaneSelectionEvent selection, final Overlay overlay,
                                      final String... labels) {
    }

    @Override
    public void onMousePressedMid(final ImgViewerMouseEvent e,
                                  final OverlayElement2D currentOverlayElement,
                                  final PlaneSelectionEvent selection, final Overlay overlay,
                                  final String... labels) {
    }

    @Override
    public void onMouseReleasedMid(final ImgViewerMouseEvent e,
                                   final OverlayElement2D currentOverlayElement,
                                   final PlaneSelectionEvent selection, final Overlay overlay,
                                   final String... labels) {
    }

    @Override
    public void onMouseDraggedMid(final ImgViewerMouseEvent e,
                                  final OverlayElement2D currentOverlayElement,
                                  final PlaneSelectionEvent selection, final Overlay overlay,
                                  final String... labels) {
    }

    @Override
    public void fireFocusLost(final Overlay overlay) {
    }


}
