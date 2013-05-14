package org.knime.knip.core.awt.converter;

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public class RealGreyARGBConverter<R extends RealType<R>> implements Converter<R, ARGBType> {

    private final double m_localMin;

    private final double m_normalizationFactor;

    public RealGreyARGBConverter(final double normalizationFactor, final double localMin) {
        m_localMin = localMin;
        m_normalizationFactor = normalizationFactor;
    }

    @Override
    public void convert(final R input, final ARGBType output) {

        int b;
        double val;

        if (m_normalizationFactor == 1) {
            val = ((input.getRealDouble() - input.getMinValue()) / (input.getMaxValue() - input.getMinValue()));

        } else {
            val =
                    (((input.getRealDouble() - m_localMin) / (input.getMaxValue() - input.getMinValue())) * m_normalizationFactor);

        }

        b = (int)Math.round(val * 255.0);

        if (b < 0) {
            b = 0;
        } else if (b > 255) {
            b = 255;
        }

        output.set(0xff000000 | (((b << 8) | b) << 8) | b);
    }
}
