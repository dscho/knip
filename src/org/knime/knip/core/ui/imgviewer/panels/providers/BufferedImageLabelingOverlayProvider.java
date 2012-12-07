package org.knime.knip.core.ui.imgviewer.panels.providers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ScreenImage;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.AWTImageTools;
import org.knime.knip.core.awt.Real2GreyRenderer;
import org.knime.knip.core.awt.Transparency;
import org.knime.knip.core.awt.parametersupport.RendererWithHilite;
import org.knime.knip.core.awt.parametersupport.RendererWithLabels;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.events.HilitedLabelsChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgAndLabelingChgEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelColoringChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelOptionsChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelPanelIsHiliteModeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelPanelVisibleLabelsChgEvent;
import org.knime.knip.core.ui.imgviewer.events.NormalizationParametersChgEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.events.RendererSelectionChgEvent;
import org.knime.knip.core.ui.imgviewer.events.TransparencyPanelValueChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewClosedEvent;

/**
 *
 *
 * @author dietzc, hornm, schoenenbergerf, zinsmaierm University of Konstanz
 */
public class BufferedImageLabelingOverlayProvider<T extends RealType<T>, L extends Comparable<L>>
                extends LabelingBufferedImageProvider<L> {

        /**
          *
          */
        private static final long serialVersionUID = 1L;

        private Real2GreyRenderer<T> m_greyRenderer;

        private RandomAccessibleInterval<T> m_img;

        private Integer m_transparency;

        private NormalizationParametersChgEvent<T> m_normalizationParameters;

        private GraphicsConfiguration m_config;

        private BufferedImage m_bufLab;

        private BufferedImage m_bufImg;

        private BufferedImage m_rgb;

        private Set<String> m_hilitedLabels;

        private Graphics m_rgbGraphics;

        private boolean m_labChanged;

        private boolean m_imgChanged;

        private String m_rowKey = null;

        private boolean m_isHiliteMode = false;

        public BufferedImageLabelingOverlayProvider(int cacheSize) {
                super(cacheSize);

                m_greyRenderer = new Real2GreyRenderer<T>();
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

                if (m_rowKey != null && m_hilitedLabels != null) {
                        hash = hash * 31 + m_rowKey.hashCode();
                        hash = hash * 31 + m_hilitedLabels.hashCode();
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
                        m_bufImg = renderImage();
                        m_imgChanged = false;
                }

                if (m_labChanged) {
                        m_bufLab = renderLabeling();
                        m_labChanged = false;
                }

                return renderTogether(m_bufImg, m_bufLab);
        }

        @Override
        @EventListener
        public void onLabelColoringChangeEvent(LabelColoringChangeEvent e) {
                super.onLabelColoringChangeEvent(e);
                m_labChanged = true;
        }

        @Override
        @EventListener
        public void onLabelOptionsChangeEvent(LabelOptionsChangeEvent e) {
                super.onLabelOptionsChangeEvent(e);
                m_labChanged = true;
        }

        private BufferedImage renderImage() {
                double[] normParams = m_normalizationParameters
                                .getNormalizationParameters(m_img, m_sel);

                if (m_greyRenderer instanceof RendererWithNormalization) {
                        ((RendererWithNormalization) m_greyRenderer)
                                        .setNormalizationParameters(
                                                        normParams[0],
                                                        normParams[1]);
                }

                ScreenImage ret = m_greyRenderer.render(m_img,
                                m_sel.getPlaneDimIndex1(),
                                m_sel.getPlaneDimIndex2(), m_sel.getPlanePos());

                return loci.formats.gui.AWTImageTools.makeBuffered(ret.image());
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
                if (m_renderer instanceof RendererWithLabels) {
                        RendererWithLabels<L> r = (RendererWithLabels<L>) m_renderer;
                        r.setActiveLabels(m_activeLabels);
                        r.setOperator(m_operator);
                        r.setLabelMapping(m_labelMapping);
                        r.setRenderingWithLabelStrings(m_withLabelStrings);
                }

                if (m_renderer instanceof RendererWithHilite
                                && m_hilitedLabels != null) {
                        RendererWithHilite r = (RendererWithHilite) m_renderer;
                        r.setHilitedLabels(m_hilitedLabels);
                        r.setHiliteMode(m_isHiliteMode);
                }

                ScreenImage ret = m_renderer.render(m_src,
                                m_sel.getPlaneDimIndex1(),
                                m_sel.getPlaneDimIndex2(), m_sel.getPlanePos());

                return loci.formats.gui.AWTImageTools.makeBuffered(ret.image());
        }

        @EventListener
        public void onUpdate(TransparencyPanelValueChgEvent e) {
                m_transparency = e.getTransparency();
                m_labChanged = true;
        }

        @EventListener
        public void onLabelingUpdated(final ImgAndLabelingChgEvent<T, L> e) {
                m_img = e.getRandomAccessibleInterval();
                m_rowKey = e.getName().getName();

                m_imgChanged = true;
                m_labChanged = true;
                super.onUpdated(new IntervalWithMetadataChgEvent<LabelingType<L>>(
                                e
                                .getLabeling(), e.getName(), e.getSource(), e
                                .getCalibratedSpace()));
        }

        @Override
        public void onUpdated(IntervalWithMetadataChgEvent<LabelingType<L>> e) {
                //
        }

        @EventListener
        public void onClose(ViewClosedEvent event) {
                m_img = null;
                m_greyRenderer = null;
                m_src = null;
                m_normalizationParameters = null;
                m_config = null;
                m_bufLab = null;
                m_bufImg = null;

                m_rgb = null;

                m_hilitedLabels = null;

                m_rgbGraphics = null;
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
        }

        @EventListener
        public void onUpdated(HilitedLabelsChgEvent e) {
                m_hilitedLabels = e.getHilitedLabels();
                m_labChanged = true;
        }

        @EventListener
        public void onUpdated(LabelPanelIsHiliteModeEvent e) {
                m_isHiliteMode = e.isHiliteMode();
                m_labChanged = true;
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
