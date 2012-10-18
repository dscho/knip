package org.knime.knip.core.ui.imgviewer;

import java.awt.Color;

/**
 * Use this to wrap colors inside a HashMap.<br>
 * 
 * We need to overwrite to methods, as both are checked by the HashMap.
 *
 * @author muethingc, University of Konstanz
 */
public final class ColorWrapper {
        private final Color m_c;

        public ColorWrapper(final Color c) {
                m_c = c;
        }

        @Override
        public int hashCode() {
                return m_c.getRed() + 255 * m_c.getGreen() + 255 * 255
                                * m_c.getBlue() + 255 * 255 * 255 * m_c.getAlpha();
        }

        @Override
        public boolean equals(final Object w) {
                if ( ! (w instanceof ColorWrapper) ) {
                        return false;
                } else {
                        return hashCode() == w.hashCode() ? true : false;
                }
        }

        /**
         * Get a copy of the stored color.<br>
         *
         * @return a copy of the color
         */
        public Color getColor() {
                return new Color(m_c.getRed(), m_c.getGreen(), m_c.getBlue(), m_c.getAlpha());
        }
}
