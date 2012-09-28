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
                                * m_c.getBlue();
        }

        @Override
        public boolean equals(final Object w) {
                return hashCode() == w.hashCode() ? true : false;
        }
}