package org.knime.knip.core.ui.imgviewer;

import java.awt.Color;

/**
 * Utility class for creating unique colors for the selection process.
 *
 * @author muethingc, University of Konstanz
 */
public enum ColorDispenser {

        INSTANCE;

        // start at one cause BufferedImage.getRGB returns 0 on empty
        private int m_c = 1;

        /**
         * Get the next Color.
         */
        public Color next() {
                Color c = new Color(m_c);
                // increase by a large amount so that antialiasing
                // cannot interfere
                // with the identification
                m_c += 100;
                return c;
        }
}
