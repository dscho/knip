package org.kniplib.ui.imgviewer;

import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ui.imgviewer.annotator.AnnotatorFilePanel;
import org.kniplib.ui.imgviewer.annotator.AnnotatorLabelPanel;
import org.kniplib.ui.imgviewer.annotator.AnnotatorManager;
import org.kniplib.ui.imgviewer.annotator.AnnotatorToolbar;
import org.kniplib.ui.imgviewer.panels.ImgNormalizationPanel;
import org.kniplib.ui.imgviewer.panels.MinimapPanel;
import org.kniplib.ui.imgviewer.panels.PlaneSelectionPanel;
import org.kniplib.ui.imgviewer.panels.TransparencyPanel;
import org.kniplib.ui.imgviewer.panels.infobars.HistogramViewInfoPanel;
import org.kniplib.ui.imgviewer.panels.infobars.ImgViewInfoPanel;
import org.kniplib.ui.imgviewer.panels.infobars.LabelingViewInfoPanel;
import org.kniplib.ui.imgviewer.panels.providers.BufferedImageProvider;
import org.kniplib.ui.imgviewer.panels.providers.HistogramBufferedImageProvider;
import org.kniplib.ui.imgviewer.panels.providers.LabelingBufferedImageProvider;
import org.kniplib.ui.imgviewer.panels.providers.OverlayBufferedImageProvider;

public class ViewerFactory {

        private ViewerFactory() {
                //
        }

        /**
         * Creates a ImgViewer for {@link Img}s with a Minimap, Plane Selection,
         * Renderer Selection, Image Normalization and Image Properties Panel
         * 
         * @return
         */
        public static <T extends RealType<T>> ImgViewer<T, Img<T>> createImgViewer(
                        int cacheSize) {

                ImgViewer<T, Img<T>> viewer = new ImgViewer<T, Img<T>>();

                BufferedImageProvider<T, Img<T>> realProvider = new BufferedImageProvider<T, Img<T>>(
                                cacheSize);
                realProvider.setEventService(viewer.getEventService());

                viewer.addViewerComponent(new ImgViewInfoPanel<T>());
                viewer.addViewerComponent(new ImgCanvas<T, Img<T>>());

                viewer.addViewerComponent(ViewerComponents.MINIMAP
                                .createInstance());
                viewer.addViewerComponent(ViewerComponents.PLANE_SELECTION
                                .createInstance());
                viewer.addViewerComponent(ViewerComponents.IMAGE_ENHANCE
                                .createInstance());
                viewer.addViewerComponent(ViewerComponents.RENDERER_SELECTION
                                .createInstance());
                viewer.addViewerComponent(ViewerComponents.IMAGE_PROPERTIES
                                .createInstance());

                return viewer;

        }

        public static <L extends Comparable<L>> ImgViewer<LabelingType<L>, Labeling<L>> createLabelingViewer(
                        int cacheSize) {
                ImgViewer<LabelingType<L>, Labeling<L>> viewer = new ImgViewer<LabelingType<L>, Labeling<L>>();

                new LabelingBufferedImageProvider<L>(cacheSize)
                                .setEventService(viewer.getEventService());

                viewer.addViewerComponent(new LabelingViewInfoPanel<L>());

                viewer.addViewerComponent(new ImgCanvas<LabelingType<L>, Labeling<L>>());

                viewer.addViewerComponent(ViewerComponents.MINIMAP
                                .createInstance());

                viewer.addViewerComponent(ViewerComponents.PLANE_SELECTION
                                .createInstance());

                viewer.addViewerComponent(ViewerComponents.RENDERER_SELECTION
                                .createInstance());

                viewer.addViewerComponent(ViewerComponents.IMAGE_PROPERTIES
                                .createInstance());

                viewer.addViewerComponent(ViewerComponents.LABEL_FILTER
                                .createInstance());

                return viewer;
        }

        /**
         * Creates a ImgViewer showing the histogram of the given image.
         * 
         * @param <T>
         * @return
         */
        public static <T extends RealType<T>> ImgViewer<T, Img<T>> createHistViewer(
                        int cacheSize) {
                ImgViewer<T, Img<T>> viewer = new ImgViewer<T, Img<T>>();

                viewer.addViewerComponent(new HistogramBufferedImageProvider<T, Img<T>>(
                                cacheSize, 512));
                viewer.addViewerComponent(new HistogramViewInfoPanel<T, Img<T>>());
                viewer.addViewerComponent(new ImgCanvas<T, Img<T>>());
                viewer.addViewerComponent(ViewerComponents.MINIMAP
                                .createInstance());
                viewer.addViewerComponent(ViewerComponents.PLANE_SELECTION
                                .createInstance());
                viewer.addViewerComponent(ViewerComponents.IMAGE_PROPERTIES
                                .createInstance());

                return viewer;
        }

        /**
         * 
         * @return
         */
        public static <T extends RealType<T>, L extends Comparable<L>> ImgViewer<T, Img<T>> createAnnotator() {
                ImgViewer<T, Img<T>> viewer = new ImgViewer<T, Img<T>>();

                viewer.addViewerComponent(new OverlayBufferedImageProvider<T, L>());
                viewer.addViewerComponent(new AnnotatorManager<T, ImgPlus<T>>());

                viewer.addViewerComponent(new AnnotatorFilePanel<T>());
                viewer.addViewerComponent(AnnotatorToolbar
                                .createStandardToolbar());
                viewer.addViewerComponent(new AnnotatorLabelPanel());
                viewer.addViewerComponent(new MinimapPanel());
                viewer.addViewerComponent(new ImgNormalizationPanel<T, Img<T>>());
                viewer.addViewerComponent(new PlaneSelectionPanel<T, Img<T>>());
                viewer.addViewerComponent(new TransparencyPanel());
                viewer.addViewerComponent(new ImgViewInfoPanel<T>());
                viewer.addViewerComponent(new ImgCanvas<T, Img<T>>());

                return viewer;
        }
}
