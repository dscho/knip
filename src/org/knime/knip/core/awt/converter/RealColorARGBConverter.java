package org.knime.knip.core.awt.converter;

import net.imglib2.converter.Converter;
import net.imglib2.display.projectors.ProjectedDimSampler;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;


public class RealColorARGBConverter<R extends RealType<R>> implements
                Converter<ProjectedDimSampler<R>, ARGBType> {

        private final double m_localMin;
        private final double m_normalizationFactor;

        public RealColorARGBConverter(final double normalizationFactor,
                        final double localMin) {

                m_localMin = localMin;
                m_normalizationFactor = normalizationFactor;

        }

        @Override
        public void convert(final ProjectedDimSampler<R> input,
                        final ARGBType output) {

                int i = 0;
                final int[] rgb = new int[3];

                while (input.hasNext() && i < 3) {

                        final double val = input.get().getRealDouble();
                        double value = ((val - m_localMin) * m_normalizationFactor);

                        // normalize to be between 0 and 1
                        value = value
                                        / (input.get().getMaxValue() - input
                                                        .get().getMinValue());

                        if (value < 0) {
                                value = 0;
                        } else if (value > 1) {
                                value = 1;
                        }

                        rgb[i] = (int) Math.round(value * 255);
                        i++;
                        input.fwd();
                }

                final int argb = 0xff000000 | (rgb[0] << 16 | rgb[1] << 8 | rgb[2]);
                output.set(argb);
        }

}
