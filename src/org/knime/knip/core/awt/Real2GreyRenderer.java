
package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.display.projectors.Projector2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.converter.RealGreyARGBConverter;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;
import org.knime.knip.core.awt.specializedrendering.FastNormalizingGreyRendering;

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
        public ScreenImage render(RandomAccessibleInterval<R> source, int dimX,
                        int dimY, long[] planePos) {

                // speed up standard cases e.g. array image...
                ScreenImage fastResult = FastNormalizingGreyRendering
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
        public void setNormalizationParameters(double factor, double min) {
                m_converter = new RealGreyARGBConverter<R>(factor, min);
                m_normalizationFactor = factor;
                m_min = min;
        }

        @Override
        public String toString() {
                return "Real Image Renderer";
        }

        @Override
        protected Abstract2DProjector<R, ARGBType> getProjector(int dimX,
                        int dimY, RandomAccessibleInterval<R> source,
                        ARGBScreenImage target) {

                return new Projector2D<R, ARGBType>(dimX, dimY, source, target,
                                m_converter);
        }
}
