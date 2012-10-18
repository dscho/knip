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
import java.awt.Stroke;

/**
 *
 *
 * @author Clemens Müthing (clemens.muething@uni-konstanz.de)
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
    
    // the positions of the first and last non zero value in the array
    private int m_posMin = 0;
    private int m_posMax = 0;
    
    
    private boolean m_normalize = false;

    // the virtual width of one pixel of the histogram
    private float m_pixelWidth = 0f;

    // the data to draw
    private int[] m_data;

    // Stores the current scale to draw in
    private Scale m_scale;

    /**
     * Set up a new histogram that will be drawn into using log scale.
     * 
     * @param data the data to draw
     */
    public HistogramPainter(final int[] data) {
        this(data, Scale.LOG);
    }

    /**
     * Sets up a new histogram that will initally not draw anthing and be set to
     * log scale.
     */
    public HistogramPainter() {
        this(null);
    }

    /**
     * Set up a new histogram that will be drawn into with the given scale.
     *  
     * @param data the data to draw
     * @param scale the scale to use
     */
    public HistogramPainter(final int[] data, final Scale scale) {
        setData(data);

        m_scale = scale;
    }
    /**
     * Set the histogram to display a new data set.
     * 
     * @param data
     *            the data
     */
    public final void setData(final int[] data) {
        m_data = data;

        if (m_data != null) {
            // find the new max and mins
            for (int i = 1; i < m_data.length; i++) {
                if (m_data[i] > m_max) {
                    m_max = m_data[i];
                }
                if (m_data[i] < m_min) {
                    m_min = m_data[i];
                }
            }

            m_maxLog = Math.log((double) m_max);
            m_minLog = Math.log((double) m_min);

            if (m_minLog == Double.NEGATIVE_INFINITY) {
                m_minLog = 0;
            }
            
            findPosMinMax();
        }
    }
    
    private void findPosMinMax() {
        
        assert (m_data != null);
        
        if (m_normalize) {
            
            // find the min Position
            for (int i = 0; i < m_data.length; i++) {
                if (m_data[i] != 0) {
                    m_posMin = i;
                    break;
                }
            }
            
            // find the max Position
            for (int i = m_data.length - 1; i >= 0; i--) {
                if (m_data[i] != 0) {
                    m_posMax = i;
                    break;
                }
            }
        } else {
            m_posMin = 0;
            m_posMax = m_data.length - 1;
        }
        
    }
    
    public double[] getNormalizationFractions() {
        double[] frac = new double[2];
        
        if (m_data != null) {
            frac[0] = (double) m_posMin / (double) m_data.length;
            frac[1] = (double) m_posMax / (double) m_data.length;
        } else {
            frac[0] = 0.0;
            frac[1] = 1.0;
        }
        
        return frac;
    }
    
    public final void setNormalize(final boolean value) {
        m_normalize = value;
        
        if (m_data != null) {
            findPosMinMax();
        }
    }

    /**
     * Calculate the sie of a pixel for the current size of the panel.
     */
    private float calcPixelSize(final int width, final int bins) {
        return (float) width / (float) (bins);
    }

    /**
     * Paint this histogram using the given Graphics2D object.
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     * @param g2 the Graphics2D object to use for drawing
     * @param width the width to draw to
     * @param height the height to draw to
     */
    public final void paintHistogram(final Graphics2D g2, final int width, final int height) {

        // paint the background
        g2.setColor(Color.white);
        g2.fillRect(0, 0, width, height);

        g2.setColor(Color.black);

        // onyl paint if some data is present
        if (m_data != null) {
            m_pixelWidth = calcPixelSize(width, m_posMax - m_posMin + 1);

            // Stroke bars = new BasicStroke( this.pixelWidth,
            // BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            Stroke bars = new BasicStroke(m_pixelWidth);
            g2.setStroke(bars);

            float pos = 0f;

            // use to check if really every picel has been drawn
            int lastFullPixel = 0;

            for (int i = m_posMin; i <= m_posMax; i++) {

                int p = (int) pos;

                if (p > lastFullPixel) {
                    lastFullPixel++;
                }
                
                int h = calculateBarDrawHeight(m_data[i], height);

                g2.drawLine(p, height, p, height - (int) h);

                pos += m_pixelWidth;

                // if we have skipped a pixel, redraw the same line
                if ((int) pos > lastFullPixel + 1) {
                    p++;
                    lastFullPixel++;

                    g2.drawLine(p, height, p, height - (int) h);
                }
            }

        } else {
            g2.setFont(new Font(g2.getFont().getFontName(), Font.BOLD, 20));
            g2.drawString("No data present", 20, height / 2);
        }
    }

    /**
     * Calculate the height to which the bar with the given value should be
     * drawn.
     * 
     * @param val the value
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
        }

        if (m_scale == Scale.LINEAR) {
            double frac = (val) / (double) m_max;
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
     *            the scale
     */
    public final void setScale(final Scale scale) {
        m_scale = scale;
    }

    /**
     * Check if this histogram has data present that can be painted.
     * 
     * @return true if data is present, false otherwise
     */
    public boolean hasData() {
        if (m_data != null) {
            return true;
        } else {
            return false;
        }
    }
}
