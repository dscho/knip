/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003, 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 *
 *
 * @author muethingc
 */
public class HistogramPainter {

        /**
         * The scale the histogram is drawn with.
         */
        public enum Scale {
                LOG("log"), LINEAR("linear");

                private final String m_name;

                private Scale(final String name) {
                        m_name = name;
                }

                @Override
                public String toString() {
                        return m_name;
                }
        }

        // used for determining how to draw the data
        private int m_max = Integer.MIN_VALUE;
        private int m_min = Integer.MAX_VALUE;
        private double m_maxLog;
        private double m_minLog;

        // the data to draw
        private HistogramWithNormalization m_histogram = null;

        // Stores the current scale to draw in
        private Scale m_scale;

        // color used for painting the histogram
        private final Color m_color = Color.BLACK;

        /**
         * Set up a new histogram that will be painted using log scale.
         *
         * @param hist
         *                the hist to draw
         */
        public HistogramPainter(final HistogramWithNormalization hist) {
                this(hist, Scale.LOG);
        }

        /**
         * Sets up a new histogram that will initally not draw anthing and be
         * set to log scale.
         */
        public HistogramPainter() {
                this(null);
        }

        /**
         * Set up a new histogram that will be drawn into with the given scale.
         *
         * @param hist
         *                the hist to draw
         * @param scale
         *                the scale to use
         */
        public HistogramPainter(final HistogramWithNormalization hist,
                        final Scale scale) {
                setHistogram(hist);
                m_scale = scale;
        }

        /**
         * Set the histogram to display a new data set.
         *
         * @param hist
         */
        public final void setHistogram(final HistogramWithNormalization hist) {
                m_histogram = hist;

                if (m_histogram == null) {
                        return;
                }

                findMinMax();
        }

        private final void findMinMax() {
                assert m_histogram != null;

                for (Integer v : m_histogram) {
                        m_max = v > m_max ? v : m_max;
                        m_min = v < m_min ? v : m_min;
                }

                m_maxLog = Math.log((double) m_max);
                m_minLog = Math.log((double) m_min);

                if (m_minLog == Double.NEGATIVE_INFINITY) {
                        m_minLog = 0;
                }
        }

        private float calcPixelSize(final int width, final int bins) {
                return (float) width / (float) (bins);
        }

        /**
         * Paint this histogram using the given Graphics2D object.
         *
         * @see javax.swing.JComponent#paintComponent(Graphics)
         * @param g2
         *                the Graphics2D object to use for drawing
         */
        public final void paint(final Graphics2D g2) {

                Rectangle paintArea = g2.getClipBounds();
                int width = (int) paintArea.getWidth();
                int height = (int) paintArea.getHeight();

                // paint the background
                g2.setColor(Color.white);
                g2.fillRect(0, 0, width, height);

                if (m_histogram != null) {
                        paintHistogram(g2);
                } else {
                        g2.setFont(new Font(g2.getFont().getFontName(),
                                        Font.BOLD, 20));
                        g2.drawString("No data present", 20, height / 2);
                }
        }

        private void paintHistogram(final Graphics2D g2) {
                assert m_histogram != null;

                Rectangle paintArea = g2.getClipBounds();
                int width = (int) paintArea.getWidth();
                int height = (int) paintArea.getHeight();

                g2.setColor(m_color);
                float pixelWidth = calcPixelSize(width, m_histogram.size());

                g2.setStroke(new BasicStroke(1));

                float pos = 0f;

                for (Integer i : m_histogram) {
                        int p = (int) pos;
                        pos += pixelWidth;

                        int h = height - calculateBarDrawHeight(i, height);

                        while (p < (int) pos) {
                                g2.drawLine(p, height, p, h);
                                p++;
                        }

                }
        }

        /**
         * Calculate the height to which the bar with the given value should be
         * drawn.
         *
         * @param val
         *                the value
         * @return the height to draw to in int
         */
        private int calculateBarDrawHeight(final double val, final int height) {

                double h = 0;

                if (m_scale == Scale.LOG) {

                        double max = m_maxLog - m_minLog;
                        double log = Math.log(val);

                        if (log == Double.NEGATIVE_INFINITY) {
                                log = 0;
                        }

                        // Normalize to log scale
                        double l = (log - m_minLog) / max;
                        h = l * height;
                } else {
                        double frac = val / (double) m_max;
                        h = frac * height;
                }

                return (int) h;
        }

        /**
         * Sets the scale used to display the histogram.
         *
         * Note: To acutally see the changes, the calling class has to issue a
         * repaint() itself.
         *
         * @param scale
         *                the scale
         */
        public final void setScale(final Scale scale) {
                m_scale = scale;
        }
}
