package org.knime.knip.core.awt.lookup;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.transfunc.TransferFunction;
import org.knime.knip.core.ui.transfunc.TransferFunctionBundle;
import org.knime.knip.core.ui.transfunc.TransferFunctionColor;

/**
 * A lookup table to convert any realvalues to ARGB values.
 *
 * @author muethingc
 *
 * @param <T>
 *                the Type this table should work on
 */
public class RealLookupTable<T extends RealType<T>> implements
                LookupTable<T, ARGBType> {

        private class Alpha implements TransferFunction {

                @Override
                public double getValueAt(double pos) {
                        return 1.0;
                }

                @Override
                public TransferFunction copy() {
                        return new Alpha();
                }

                @Override
                public void zoom(double lower, double upper) {
                        // just ignore
                }
        }

        // The default amount of bins in the lookup table
        private final static int ENTRIES = 255;

        private final ARGBType[] m_values;

        private final double m_minValue;

        private final double m_scale;

        /**
         * Create a new instance, using the default value of entries in the
         * lookup table.<br>
         *
         * @param min
         *                the minimum value of this table
         * @param max
         *                the largest value for this table
         * @param bundle
         *                the transfer function bundle used for creating the
         *                table
         */
        public RealLookupTable(final double min, final double max,
                        final TransferFunctionBundle bundle) {
                this(min, max, ENTRIES, bundle);
        }

        /**
         * Set up a new lookup table.<br>
         *
         * @param min
         *                the minimum value of this table
         * @param max
         *                the largest value for this table
         * @param entries
         *                the number of entries the lookup table should have
         * @param bundle
         *                the transfer function bundle used for creating the
         *                table
         */
        public RealLookupTable(final double min, final double max,
                        final int entries,
                        final TransferFunctionBundle bundle) {
                m_minValue = min;
                m_scale = (entries - 1) / (max - m_minValue);
                m_values = tableFromBundle(bundle, new ARGBType[entries]);
        }

        private ARGBType[] tableFromBundle(final TransferFunctionBundle bundle,
                        final ARGBType[] table) {
                assert (bundle != null);

                switch (bundle.getType()) {
                case GREY:
                        return tableFromGreyBundle(bundle, table);
                case GREYA:
                        return tableFromGreyABundle(bundle, table);
                case RGB:
                        return tableFromRGBBundle(bundle, table);
                case RGBA:
                        return tableFromRGBABundle(bundle, table);

                default:
                        throw new IllegalArgumentException(
                                        "Not yet implemented for "
                                                        + bundle.getType());
                }
        }

        private ARGBType[] tableFromGreyBundle(
                        final TransferFunctionBundle bundle,
                        final ARGBType[] table) {
                assert (bundle != null && bundle.getType() == TransferFunctionBundle.Type.GREY);

                TransferFunction grey = bundle.get(TransferFunctionColor.GREY);

                return fillTable(table, new Alpha(), grey, grey, grey);
        }

        private ARGBType[] tableFromGreyABundle(
                        final TransferFunctionBundle bundle,
                        final ARGBType[] table) {
                assert (bundle != null && bundle.getType() == TransferFunctionBundle.Type.GREYA);

                TransferFunction alpha = bundle
                                .get(TransferFunctionColor.ALPHA);
                TransferFunction grey = bundle.get(TransferFunctionColor.GREY);

                return fillTable(table, alpha, grey, grey, grey);
        }

        private ARGBType[] tableFromRGBBundle(
                        final TransferFunctionBundle bundle,
                        final ARGBType[] table) {
                assert (bundle != null && bundle.getType() == TransferFunctionBundle.Type.RGB);

                TransferFunction red = bundle.get(TransferFunctionColor.RED);
                TransferFunction green = bundle
                                .get(TransferFunctionColor.GREEN);
                TransferFunction blue = bundle.get(TransferFunctionColor.BLUE);

                return fillTable(table, new Alpha(), red, green, blue);
        }

        private ARGBType[] tableFromRGBABundle(
                        final TransferFunctionBundle bundle,
                        final ARGBType[] table) {
                assert (bundle != null && bundle.getType() == TransferFunctionBundle.Type.RGBA);

                TransferFunction alpha = bundle
                                .get(TransferFunctionColor.ALPHA);
                TransferFunction red = bundle.get(TransferFunctionColor.RED);
                TransferFunction green = bundle
                                .get(TransferFunctionColor.GREEN);
                TransferFunction blue = bundle.get(TransferFunctionColor.BLUE);

                return fillTable(table, alpha, red, green, blue);
        }

        private ARGBType[] fillTable(final ARGBType[] table,
                        final TransferFunction alpha,
                        final TransferFunction red,
                        final TransferFunction green,
                        final TransferFunction blue) {
                assert (table != null && table.length > 1);
                assert (alpha != null);
                assert (red != null);
                assert (green != null);
                assert (blue != null);

                double step = 1.0 / (table.length - 1);
                double pos = 0.0;

                for (int i = 0; i < table.length; i++) {
                        int a = ((int) (alpha.getValueAt(pos) * 255.0)) << 24;
                        int r = ((int) (red.getValueAt(pos) * 255.0)) << 16;
                        int g = ((int) (green.getValueAt(pos) * 255.0)) << 8;
                        int b = ((int) (blue.getValueAt(pos) * 255.0));

                        table[i] = new ARGBType((a | r | g | b));
                        pos += step;
                }

                return table;
        }

        /**
         * Lookup the value of a pixel.<br>
         *
         * @param pixel
         *                the value to lookup
         * @return the lookup value
         */
        public ARGBType lookup(final double pixel) {
                int index = (int) ((pixel - m_minValue) * m_scale);
                return m_values[index];
        }

        @Override
        public ARGBType lookup(T value) {
                return lookup(value.getRealDouble());
        }
}
