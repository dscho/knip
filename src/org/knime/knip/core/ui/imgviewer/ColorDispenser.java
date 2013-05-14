package org.knime.knip.core.ui.imgviewer;

import java.awt.Color;

/**
 * Utility class for creating unique colors for the selection process.
 *
 * @author muethingc, University of Konstanz
 */
public enum ColorDispenser {

        INSTANCE;

        /**
         * The color that should be used for painting backgrounds when using the
         * ColorDispenser. This Color is guaranteed to not be returned on a call
         * to {@link next()}.
         */
        public static final int BACKGROUND_RGB = 0;

        /**
         * The color that should be used for painting backgrounds when using the
         * ColorDispenser. This Color is guaranteed to not be returned on a call
         * to {@link next()}.
         */
        public static final Color BACKGROUND_COLOR = new Color(BACKGROUND_RGB);

        // start at one cause BufferedImage.getRGB returns 0 on empty private
        private int m_c = 1;

        /**
         * Get the next Color.
         */
        public Color next() {
                final Color c = new Color(m_c);
                // increase by a large amount so that antialiasing
                // cannot interfere
                // with the identification
                m_c += 100;

                m_c = m_c == BACKGROUND_RGB ? m_c + 100 : m_c;

                return c;
        }
}
