package org.knime.knip.core.ui.imgviewer.panels.providers;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.display.ColorTable;
import net.imglib2.display.ScreenImage;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.AWTImageTools;
import org.knime.knip.core.awt.lookup.LookupTable;
import org.knime.knip.core.awt.parametersupport.RendererWithColorTable;
import org.knime.knip.core.awt.parametersupport.RendererWithLookupTable;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.events.AWTImageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.NormalizationParametersChgEvent;
import org.knime.knip.core.ui.imgviewer.panels.transfunc.BundleChgEvent;
import org.knime.knip.core.ui.imgviewer.panels.transfunc.LookupTableChgEvent;

/**
 * Converts an {@link Img} to a {@link BufferedImage}.
 *
 * It creates an image from a plane selection, image, image renderer, and normalization parameters. Propagates
 * {@link AWTImageChgEvent}.
 *
 * @author dietzc, hornm, schonenbergerf University of Konstanz
 *
 * @param <T> the {@link Type} of the {@link Img} converted to a {@link BufferedImage}
 * @param <I> the {@link Img} converted to a {@link BufferedImage}
 */
public class BufferedImageProvider<T extends RealType<T>> extends AWTImageProvider<T> {

    /**
     * A simple class that can be injected in the converter so that we will always get some result.
     */
    private class SimpleTable implements LookupTable<T, ARGBType> {

        @Override
        public final ARGBType lookup(final T value) {
            return new ARGBType(1);
        }
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private NormalizationParametersChgEvent<T> m_normalizationParameters;

    private LookupTable<T, ARGBType> m_lookupTable = new SimpleTable();

    private ColorTable[] m_colorTables = new ColorTable[]{};

    /**
     * @param cacheSize The size of the cache beeing used in {@link AWTImageProvider}
     */
    public BufferedImageProvider(final int cacheSize) {
        super(cacheSize);
        m_normalizationParameters = new NormalizationParametersChgEvent<T>(0, false);
    }

    /**
     * Render an image of
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Image createImage() {
        final double[] normParams = m_normalizationParameters.getNormalizationParameters(m_src, m_sel);

        if (m_renderer instanceof RendererWithNormalization) {
            ((RendererWithNormalization)m_renderer).setNormalizationParameters(normParams[0], normParams[1]);
        }

        if (m_renderer instanceof RendererWithLookupTable) {
            ((RendererWithLookupTable<T, ARGBType>)m_renderer).setLookupTable(m_lookupTable);
        }

        if (m_renderer instanceof RendererWithColorTable) {
            ((RendererWithColorTable)m_renderer).setColorTables(m_colorTables);
        }

        final ScreenImage ret =
                m_renderer.render(m_src, m_sel.getPlaneDimIndex1(), m_sel.getPlaneDimIndex2(), m_sel.getPlanePos());

        return loci.formats.gui.AWTImageTools.makeBuffered(ret.image());
    }

    /**
     * {@link EventListener} for {@link NormalizationParametersChgEvent} events The
     * {@link NormalizationParametersChgEvent} of the {@link AWTImageTools} will be updated
     *
     * @param normalizationParameters
     */
    @EventListener
    public void onUpdated(final NormalizationParametersChgEvent<T> normalizationParameters) {
        m_normalizationParameters = normalizationParameters;
    }

    /**
     *
     * {@link EventListener} for {@link BundleChgEvent}. A new lookup table will be constructed using the given transfer
     * function bundle.
     *
     * @param event
     */
    @EventListener
    public void onLookupTableChgEvent(final LookupTableChgEvent<T, ARGBType> event) {
        m_lookupTable = event.getTable();

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
    protected int generateHashCode() {

        return (super.generateHashCode() * 31) + m_normalizationParameters.hashCode();

    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        super.saveComponentConfiguration(out);
        m_normalizationParameters.writeExternal(out);

    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.loadComponentConfiguration(in);
        m_normalizationParameters = new NormalizationParametersChgEvent<T>();
        m_normalizationParameters.readExternal(in);
    }
}
