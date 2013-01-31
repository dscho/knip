package org.knime.knip.core.awt.converter;

import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractArrayColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

/**
 * Takes a real and converts it into a pixel of an ARGB image using a color
 * table. If no color table is set using {@link #setColorTable(ColorTable16)} or
 * {@link #setColorTable(ColorTable8)} a linear ramp grey color table is used as
 * default.
 * 
 * @author zinsmaie
 * 
 * @param <R>
 */
public class RealTableColorARGBConverter<R extends RealType<R>> implements
                Converter<R, ARGBType> {

        private final double m_localMin;
        private final double m_normalizationFactor;
        private AbstractArrayColorTable<?> m_table;
        private int m_rangeFactor;

        public RealTableColorARGBConverter(double normalizationFactor,
                        double localMin) {

                m_localMin = localMin;
                m_normalizationFactor = normalizationFactor;

                m_table = new ColorTable8();
                m_rangeFactor = 255;
        }

        public void setColorTable(ColorTable8 table) {
                m_rangeFactor = 255;
                m_table = table;
        }

        public void setColorTable(ColorTable16 table) {
                m_rangeFactor = 65535;
                m_table = table;
        }

        @Override
        public void convert(final R input, final ARGBType output) {

                int intVal;
                double val;

                if (m_normalizationFactor == 1) {
                        val = ((input.getRealDouble() - input.getMinValue()) / (input
                                        .getMaxValue() - input.getMinValue()));

                } else {
                        val = ((input.getRealDouble() - m_localMin)
                                        / (input.getMaxValue() - input
                                                        .getMinValue()) * m_normalizationFactor);

                }

                intVal = (int) Math.round(val * m_rangeFactor);

                if (intVal < 0)
                        intVal = 0;
                else if (intVal > m_rangeFactor)
                        intVal = m_rangeFactor;

                output.set(m_table.argb(intVal));
        }
}
