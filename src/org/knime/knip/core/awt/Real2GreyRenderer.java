
package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.converter.RealGreyARGBConverter;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;
import org.knime.knip.core.awt.specializedrendering.FastNormalizingGreyRendering;
import org.knime.knip.core.awt.specializedrendering.Projector2D;

public class Real2GreyRenderer<R extends RealType<R>> extends
ProjectingRenderer<R> implements RendererWithNormalization {

    // speed up
    double m_normalizationFactor;
    double m_min;
    // default
    private RealGreyARGBConverter<R> m_converter;

    public Real2GreyRenderer() {
        m_converter = new RealGreyARGBConverter<R>(1.0, 0.0);
        m_normalizationFactor = 1.0;
        m_min = 0.0;
    }

    @Override
    public ScreenImage render(final RandomAccessibleInterval<R> source, final int dimX,
                              final int dimY, final long[] planePos) {

        // speed up standard cases e.g. array image...
        final ScreenImage fastResult = FastNormalizingGreyRendering
                .tryRendering(source, dimX, dimY, planePos,
                              m_normalizationFactor, m_min);

        if (fastResult != null) {
            return fastResult;
        } else {
            // default implementation
            return super.render(source, dimX, dimY, planePos);
        }
    }

    @Override
    public void setNormalizationParameters(final double factor, final double min) {
        m_converter = new RealGreyARGBConverter<R>(factor, min);
        m_normalizationFactor = factor;
        m_min = min;
    }

    @Override
    public String toString() {
        return "Real Image Renderer";
    }

    @Override
    protected Abstract2DProjector<R, ARGBType> getProjector(final int dimX,
                                                            final int dimY, final RandomAccessibleInterval<R> source,
                                                            final ARGBScreenImage target) {

        return new Projector2D<R, ARGBType>(dimX, dimY, source, target,
                m_converter);
    }
}
