package org.kniplib.ui.imgviewer.panels.providers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Set;

import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.type.numeric.RealType;

import org.kniplib.awt.AWTImageTools;
import org.kniplib.awt.renderer.GreyImgRenderer;
import org.kniplib.awt.renderer.LabelingRenderer;
import org.kniplib.awt.renderer.Transparency;
import org.kniplib.ui.event.EventListener;
import org.kniplib.ui.imgviewer.events.HilitedLabelsChgEvent;
import org.kniplib.ui.imgviewer.events.ImgAndLabelingChgEvent;
import org.kniplib.ui.imgviewer.events.ImgChgEvent;
import org.kniplib.ui.imgviewer.events.LabelPanelIsHiliteModeEvent;
import org.kniplib.ui.imgviewer.events.LabelPanelVisibleLabelsChgEvent;
import org.kniplib.ui.imgviewer.events.NormalizationParametersChgEvent;
import org.kniplib.ui.imgviewer.events.PlaneSelectionEvent;
import org.kniplib.ui.imgviewer.events.RendererSelectionChgEvent;
import org.kniplib.ui.imgviewer.events.TransparencyPanelValueChgEvent;

/**
 *
 *
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 */
public class BufferedImageLabelingOverlayProvider<T extends RealType<T>, L extends Comparable<L>>
                extends LabelingBufferedImageProvider<L> {

        /**
	 *
	 */
        private static final long serialVersionUID = 1L;

        private final GreyImgRenderer<T> m_imgRenderer;

        private Img<T> m_img;

        private Integer m_transparency;

        private NormalizationParametersChgEvent<T> m_normalizationParameters;

        private final GraphicsConfiguration m_config;

        private BufferedImage m_bufLab;

        private BufferedImage m_bufImg;

        private BufferedImage m_rgb;

        private Graphics m_rgbGraphics;

        private boolean m_labChanged;

        private boolean m_imgChanged;

        private Map<String, Set<String>> m_hilitedLabels;

        private String m_rowKey = null;

        private boolean m_isHiliteMode = false;

        public BufferedImageLabelingOverlayProvider(int cacheSize) {
                super(cacheSize);
                m_imgRenderer = new GreyImgRenderer<T>();
                m_transparency = 128;
                m_normalizationParameters = new NormalizationParametersChgEvent<T>(
                                0, false);
                m_config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                .getDefaultScreenDevice()
                                .getDefaultConfiguration();
        }

        @Override
        protected int generateHashCode() {
                int hash = super.generateHashCode();
                hash = hash * 31 + m_img.hashCode();
                hash = hash * 31 + m_transparency;
                hash = hash * 31 + m_normalizationParameters.hashCode();

                if (m_isHiliteMode)
                        hash = hash * 31 + 1;

                if (m_rowKey != null && m_hilitedLabels != null
                                && m_hilitedLabels.containsKey(m_rowKey)) {
                        hash = hash * 31 + m_rowKey.hashCode();
                        hash = hash
                                        * 31
                                        + m_hilitedLabels.get(m_rowKey)
                                                        .hashCode();
                }
                return hash;
        }

        @Override
        @EventListener
        public void onUpdate(LabelPanelVisibleLabelsChgEvent e) {
                m_labChanged = true;
                super.onUpdate(e);
        }

        @Override
        @EventListener
        public void onPlaneSelectionUpdate(PlaneSelectionEvent sel) {
                m_sel = sel;
                m_imgChanged = true;
                m_labChanged = true;
                super.onPlaneSelectionUpdate(sel);
        }

        @Override
        @EventListener
        public void onRendererUpdate(RendererSelectionChgEvent e) {
                m_labChanged = true;
                super.onRendererUpdate(e);
        }

        @Override
        protected Image createImage() {
                if (m_imgChanged) {
                        renderImage();
                        m_imgChanged = false;
                }

                if (m_labChanged) {
                        renderLabeling();
                        m_labChanged = false;
                }

                return renderTogether(m_bufImg, m_bufLab);
        }

        private BufferedImage renderImage() {
                double[] normParams = m_normalizationParameters
                                .getNormalizationParameters(m_img, m_sel);
                m_bufImg = m_imgRenderer.render(m_img,
                                m_sel.getPlaneDimIndex1(),
                                m_sel.getPlaneDimIndex2(), m_sel.getPlanePos(),
                                1.0, normParams[1], normParams[0]);
                return m_bufImg;
        }

        private BufferedImage renderTogether(BufferedImage img,
                        BufferedImage labeling) {
                m_rgb = m_config.createCompatibleImage(img.getWidth(),
                                img.getHeight(),
                                java.awt.Transparency.TRANSLUCENT);
                m_rgbGraphics = m_rgb.getGraphics();

                m_rgbGraphics.drawImage(img, 0, 0, null);
                m_rgbGraphics.drawImage(Transparency.makeColorTransparent(
                                labeling, Color.WHITE, m_transparency), 0, 0,
                                null);

                return m_rgb;
        }

        private BufferedImage renderLabeling() {
                if (m_hilitedLabels != null)
                        ((LabelingRenderer<L>) m_renderer)
                                        .setHilitedLabels(m_hilitedLabels
                                                        .get(m_rowKey));

                ((LabelingRenderer<L>) m_renderer)
                                .setHiliteMode(m_isHiliteMode);

                m_bufLab = ((LabelingRenderer<L>) m_renderer).render(m_src,
                                m_sel.getPlaneDimIndex1(),
                                m_sel.getPlaneDimIndex2(), m_sel.getPlanePos(),
                                1.0, m_activeLabels, m_operator);

                return m_bufLab;
        }

        @EventListener
        public void onUpdate(TransparencyPanelValueChgEvent e) {
                m_transparency = e.getTransparency();
                m_labChanged = true;
                renderAndCacheImg();
        }

        @EventListener
        public void onLabelingUpdated(final ImgAndLabelingChgEvent<T, L> e) {
                m_img = e.getInterval();
                m_rowKey = e.getName().getName();

                m_imgChanged = true;
                m_labChanged = true;
                super.onUpdated(new ImgChgEvent(e.getLabeling(), e.getName(), e
                                .getCalibratedSpace()));
        }

        @Override
        public void onUpdated(ImgChgEvent<Labeling<L>> e) {
                //
        }

        /**
         * {@link EventListener} for {@link NormalizationParametersChgEvent}
         * events The {@link NormalizationParametersChgEvent} of the
         * {@link AWTImageTools} will be updated
         *
         * @param normalizationParameters
         */
        @EventListener
        public void onUpdated(
                        NormalizationParametersChgEvent<T> normalizationParameters) {
                m_normalizationParameters = normalizationParameters;
                m_imgChanged = true;
                renderAndCacheImg();
        }

        @EventListener
        public void onUpdated(HilitedLabelsChgEvent e) {
                m_hilitedLabels = e.getHilitedLabels();
                m_labChanged = true;
                renderAndCacheImg();
        }

        @EventListener
        public void onUpdated(LabelPanelIsHiliteModeEvent e) {
                m_isHiliteMode = e.isHiliteMode();
                m_labChanged = true;
                renderAndCacheImg();
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                super.saveComponentConfiguration(out);
                out.writeInt(m_transparency);
                m_normalizationParameters.writeExternal(out);
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                super.loadComponentConfiguration(in);
                m_transparency = in.readInt();
                m_normalizationParameters = new NormalizationParametersChgEvent<T>();
                m_normalizationParameters.readExternal(in);
        }

}
