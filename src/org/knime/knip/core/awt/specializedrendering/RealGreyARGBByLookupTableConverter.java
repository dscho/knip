package org.knime.knip.core.awt.specializedrendering;

import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.lookup.LookupTable;

/**
 * Converts RealType values to ARGB using a Lookuptable.<br>
 *
 * @author muethingc
 */
public class RealGreyARGBByLookupTableConverter<T extends RealType<T>>
                implements Converter<T, ARGBType> {

        private LookupTable<T, ARGBType> m_table = null;

        /**
         * Create a new instance.<br>
         *
         * @param table
         *                the lookup table
         *
         * @throws NullPointerException
         *                 if table == null
         */
        public RealGreyARGBByLookupTableConverter(
                        final LookupTable<T, ARGBType> table) {
                setLookupTable(table);
        }

        /**
         * Set a new LookupTable which will be used from the next call to
         * lookup.
         *
         * @param table
         *                the new lookup table
         *
         * @throws NullPointerException
         *                 if table == null
         */
        public void setLookupTable(final LookupTable<T, ARGBType> table) {
                if (table == null)
                        throw new NullPointerException();
                m_table = table;
        }

        @Override
        public void convert(final T input, final ARGBType output) {

                output.set(m_table.lookup(input));

        }
}
