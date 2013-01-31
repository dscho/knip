package org.knime.knip.core.awt.converter;

import net.imglib2.converter.Converter;
import net.imglib2.display.AbstractArrayColorTable;
import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public class RealTableColorARGBConverter<R extends RealType<R>> implements
                Converter<R, ARGBType> {

        private final double m_localMin;
        private final double m_normalizationFactor;
        private AbstractArrayColorTable<?> m_table;
        private boolean m_table8;

        public RealTableColorARGBConverter(double normalizationFactor,
                        double localMin) {

                m_localMin = localMin;
                m_normalizationFactor = normalizationFactor;
        }

        public void setColorTable(ColorTable8 table) {
                m_table8 = true;
                m_table = table;
        }

        public void setColorTable(ColorTable16 table) {
                m_table8 = false;
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

                if (m_table8) {
                        intVal = (int) Math.round(val * 255.0);

                        if (intVal < 0)
                                intVal = 0;
                        else if (intVal > 255)
                                intVal = 255;
                } else {
                        intVal = (int) Math.round(val * 65535);

                        if (intVal < 0)
                                intVal = 0;
                        else if (intVal > 65535)
                                intVal = 65535;
                }

                output.set(m_table.argb(intVal));
        }
}
