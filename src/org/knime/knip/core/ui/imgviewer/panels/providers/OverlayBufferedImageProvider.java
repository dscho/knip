package org.knime.knip.core.ui.imgviewer.panels.providers;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ScreenImage;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.AWTImageTools;
import org.knime.knip.core.awt.ImageRenderer;
import org.knime.knip.core.awt.Real2GreyRenderer;
import org.knime.knip.core.awt.RendererFactory;
import org.knime.knip.core.awt.parametersupport.RendererWithColorTable;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorImgAndOverlayChgEvent;
import org.knime.knip.core.ui.imgviewer.events.AWTImageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.NormalizationParametersChgEvent;
import org.knime.knip.core.ui.imgviewer.events.OverlayChgEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.events.TransparencyPanelValueChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewClosedEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;

/**
 * Creates a awt image from an image, plane selection, normalization parameters, ..., and an overlay. Propagates
 * {@link AWTImageChgEvent}.
 *
 * @author hornm, University of Konstanz
 */
public class OverlayBufferedImageProvider<T extends RealType<T>, L extends Comparable<L>> extends AWTImageProvider<T> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Overlay<L> m_overlay;

    private int m_transparency = 128;

    private BufferedImage m_tmpRes;

    private BufferedImage m_tmpCanvas;

    private Graphics2D m_tmpCanvasGraphics;

    private NormalizationParametersChgEvent m_normalizationParameters;

    private final GraphicsConfiguration m_config;

    private ColorTable[] m_colorTables;

    public OverlayBufferedImageProvider() {
        super(0);
        m_renderer = new Real2GreyRenderer<T>();
        m_normalizationParameters = new NormalizationParametersChgEvent(-1, false);
        m_config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    }

    @Override
    protected int generateHashCode() {
        int hash = super.generateHashCode();

        if (m_overlay != null) {
            hash = (hash * 31) + m_overlay.hashCode();
        }

        return hash;
    }

    @Override
    protected Image createImage() {
        //converted version is guaranteed to be ? extends RealType => getNormalization and render should work
        //TODO find a way to relax type constraints from R extends RealType  to RealType
        RandomAccessibleInterval convertedSrc = convertIfDouble(m_src);

        final double[] normParams = m_normalizationParameters.getNormalizationParameters(convertedSrc, m_sel);

        if (m_renderer instanceof RendererWithNormalization) {
            ((RendererWithNormalization)m_renderer).setNormalizationParameters(normParams[0], normParams[1]);
        }

        if (m_renderer instanceof RendererWithColorTable) {
            ((RendererWithColorTable)m_renderer).setColorTables(m_colorTables);
        }

        final ScreenImage res =
                m_renderer.render(convertedSrc, m_sel.getPlaneDimIndex1(), m_sel.getPlaneDimIndex2(),
                                                      m_sel.getPlanePos());

        m_tmpRes = loci.formats.gui.AWTImageTools.makeBuffered(res.image());

        return writeOverlay(m_tmpRes);

    }

    private BufferedImage writeOverlay(final BufferedImage img) {

        if (m_overlay == null) {
            return img;
        }

        if ((m_tmpCanvas == null) || (m_tmpCanvas.getWidth() != img.getWidth())
                || (m_tmpCanvas.getHeight() != img.getHeight())) {
            m_tmpCanvas =
                    m_config.createCompatibleImage((int)m_src.dimension(m_sel.getPlaneDimIndex1()),
                                                   (int)m_src.dimension(m_sel.getPlaneDimIndex2()),
                                                   Transparency.TRANSLUCENT);
            m_tmpCanvasGraphics = m_tmpCanvas.createGraphics();
        }

        m_tmpCanvasGraphics.drawImage(img, 0, 0, null);

        m_overlay.renderBufferedImage(m_tmpCanvasGraphics, m_sel.getDimIndices(), m_sel.getPlanePos(), m_transparency);

        return m_tmpCanvas;
    }

    @EventListener
    public void onUpdated(final OverlayChgEvent e) {
        m_overlay = e.getOverlay();
        m_eventService.publish(new AWTImageChgEvent(writeOverlay(m_tmpRes)));
    }

    @Override
    public void onUpdated(final IntervalWithMetadataChgEvent<T> e) {
        // Do nothing
    }

    @EventListener
    public void onUpdated(final AnnotatorImgAndOverlayChgEvent e) {
        m_src = e.getImg();
        m_overlay = e.getOverlay();

        if ((m_sel == null) || (m_sel.numDimensions() != m_src.numDimensions())) {
            m_sel = new PlaneSelectionEvent(0, 1, new long[m_src.numDimensions()]);
        }
        for (int d = 0; d < m_sel.numDimensions(); d++) {
            if (m_sel.getPlanePosAt(d) >= m_src.dimension(d)) {
                m_sel = new PlaneSelectionEvent(0, 1, new long[m_src.numDimensions()]);
                break;
            }
        }

        final ImageRenderer<T>[] renderers = RendererFactory.createSuitableRenderer(m_src);
        if (m_renderer != null) {
            boolean contained = false;
            for (final ImageRenderer<T> renderer : renderers) {
                if (m_renderer.toString().equals(renderer.toString())) {
                    m_renderer = renderer;
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                m_renderer = renderers[0];
            }
        } else {
            m_renderer = renderers[0];
        }
    }

    @EventListener
    public void onUpdate(final TransparencyPanelValueChgEvent e) {
        if (m_src != null) {
            m_transparency = e.getTransparency();
            m_eventService.publish(new AWTImageChgEvent(writeOverlay(m_tmpRes)));
        }
    }

    /**
     * {@link EventListener} for {@link NormalizationParametersChgEvent} events The
     * {@link NormalizationParametersChgEvent} of the {@link AWTImageTools} will be updated
     *
     * @param normalizationParameters
     */
    @EventListener
    public void onUpdated(final NormalizationParametersChgEvent normalizationParameters) {
        if (m_src != null) {
            m_normalizationParameters = normalizationParameters;
        }
    }

    @EventListener
    public void onImageUpdated(final ImgWithMetadataChgEvent<T> e) {
        final int size = e.getImgMetaData().getColorTableCount();
        m_colorTables = new ColorTable[size];

        for (int i = 0; i < size; i++) {
            m_colorTables[i] = e.getImgMetaData().getColorTable(i);
        }
    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        super.saveComponentConfiguration(out);
        m_overlay.writeExternal(out);
        m_normalizationParameters.writeExternal(out);
        out.writeInt(m_transparency);
    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.loadComponentConfiguration(in);

        m_overlay = new Overlay<L>();
        m_overlay.readExternal(in);
        m_normalizationParameters = new NormalizationParametersChgEvent();
        m_normalizationParameters.readExternal(in);
        m_transparency = in.readInt();

    }

    @Override
    public void reset() {
        super.reset();
        m_overlay = null;
        m_src = null;
    }

    @EventListener
    public void onClose(final ViewClosedEvent event) {
        m_src = null;
        m_overlay = null;
    }
}
