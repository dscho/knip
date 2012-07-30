package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.display.projectors.DimProjector2D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.converter.RealColorARGBConverter;
import org.knime.knip.core.awt.parametersupport.RendererWithNormalization;

public class Real2ColorRenderer<R extends RealType<R>> extends
                ProjectingRenderer<R> implements RendererWithNormalization {

        private RealColorARGBConverter<R> m_converter;
        private final int m_projectedDimension;

        public Real2ColorRenderer(int projectedDimension) {
                m_projectedDimension = projectedDimension;
                m_converter = new RealColorARGBConverter<R>(1.0, 0.0);
        }

        @Override
        public String toString() {
                return "RGB Image Renderer (RGB-Dim:" + m_projectedDimension
                                + ")";
        }

        @Override
        public void setNormalizationParameters(double factor, double min) {
                m_converter = new RealColorARGBConverter<R>(factor, min);
        }

        @Override
        protected Abstract2DProjector<R, ARGBType> getProjector(int dimX,
                        int dimY, RandomAccessibleInterval<R> source,
                        ARGBScreenImage target) {

                return new DimProjector2D<R, ARGBType>(dimX, dimY, source,
                                target, m_converter, m_projectedDimension);
        }

}
