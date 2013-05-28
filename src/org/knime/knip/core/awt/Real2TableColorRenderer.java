package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.converter.RealTableColorARGBConverter;
import org.knime.knip.core.awt.parametersupport.RendererWithColorTable;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;
import org.knime.knip.core.awt.specializedrendering.Projector2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders the real values of a X,Y slice as ScreenImage. The position in the colorDim defines which of the provided
 * color tables is used.
 * 
 * @author zinsmaie
 * 
 * @param <R>
 */
public class Real2TableColorRenderer<R extends RealType<R>> extends ProjectingRenderer<R> implements
        RendererWithNormalization, RendererWithColorTable {

    public final static Logger LOGGER = LoggerFactory.getLogger(Real2TableColorRenderer.class);

    // default
    private RealTableColorARGBConverter<R> m_converter;

    private final int m_colorDim;

    private ColorTable[] m_colorTables;

    public Real2TableColorRenderer(final int colorDim) {
        m_colorDim = colorDim;
        m_converter = new RealTableColorARGBConverter<R>(1.0, 0.0);
    }

    @Override
    public ScreenImage render(final RandomAccessibleInterval<R> source, final int dimX, final int dimY,
                              final long[] planePos) {

        // default implementation
        final ColorTable ct = m_colorTables[(int)planePos[m_colorDim]];

        if (ct instanceof ColorTable8) {
            m_converter.setColorTable((ColorTable8)ct);
        } else if (ct instanceof ColorTable16) {
            m_converter.setColorTable((ColorTable16)ct);
        } else {
            // fall back linear 8 gray ramp
            LOGGER.warn("Unsupported color table format. Using linear grey ramp.");
            m_converter.setColorTable(new ColorTable8());
        }

        return super.render(source, dimX, dimY, planePos);
    }

    @Override
    public void setNormalizationParameters(final double factor, final double min) {
        m_converter = new RealTableColorARGBConverter<R>(factor, min);
    }

    @Override
    public String toString() {
        return "ColorTable based Image Renderer (dim:" + m_colorDim + ")";
    }

    @Override
    protected Abstract2DProjector<R, ARGBType> getProjector(final int dimX, final int dimY,
                                                            final RandomAccessibleInterval<R> source,
                                                            final ARGBScreenImage target) {

        return new Projector2D<R, ARGBType>(dimX, dimY, source, target, m_converter);
    }

    @Override
    public void setColorTables(final ColorTable[] tables) {
        m_colorTables = tables.clone();
    }

}
