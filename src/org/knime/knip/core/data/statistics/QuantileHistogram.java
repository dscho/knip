/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.knime.knip.core.data.statistics;

/**
 * 
 * 
 * @author tcriess, University of Konstanz
 */
public class QuantileHistogram {
        /* */
        private static int m_maxValue;

        /* */
        private int[] m_histogram;

        /* */
        private int m_count;

        /**
         * Constructor.
         * 
         * @param maxValue
         *                The maximum pixel value
         */
        public QuantileHistogram(final int maxValue) {
                m_maxValue = maxValue;
                m_histogram = new int[maxValue];
                clear();
        }

        /**
         * Clear the histogram.
         */
        public void clear() {
                for (int i = 0; i < m_maxValue; i++) {
                        m_histogram[i] = 0;
                }
                m_count = 0;
        }

        /**
         * Add a histogram.
         * 
         * @param h
         *                The histogram to add
         */
        public void add(final QuantileHistogram h) {
                int[] histogram = h.getArray();
                if (histogram.length != m_histogram.length) {
                        return;
                }
                for (int i = 0; i < histogram.length; i++) {
                        m_histogram[i] += histogram[i];
                        m_count += histogram[i];
                }
                return;
        }

        /**
         * Subtract a histogram.
         * 
         * @param h
         *                The histogram to subtract
         */
        public void sub(final QuantileHistogram h) {
                int[] histogram = h.getArray();
                if (histogram.length != m_histogram.length) {
                        return;
                }
                for (int i = 0; i < histogram.length; i++) {
                        m_histogram[i] -= histogram[i];
                        m_count -= histogram[i];
                }
                return;
        }

        /**
         * Add a pixel value.
         * 
         * @param pixel
         *                Pixel value to add
         */
        public void addPixel(final int pixel) {
                if (pixel < m_maxValue) {
                        m_histogram[pixel]++;
                        m_count++;
                }
        }

        /**
         * Substract a pixel value.
         * 
         * @param pixel
         *                Pixel value to subtract
         */
        public void subPixel(final int pixel) {
                if (pixel < m_maxValue) {
                        m_histogram[pixel]--;
                        m_count--;
                }
        }

        /**
         * Returns the given quantile value.
         * 
         * @param quantile
         *                The quantile that should be returned (in %, min. 1)
         * @return The quantile value
         */
        public int getQuantile(final int quantile) {
                int actcount = 0;
                int i;
                int stop = Math.max(
                                (int) ((double) m_count * (double) quantile / 100.0),
                                1);
                for (i = 0; i < m_histogram.length && actcount < stop; i++) {
                        actcount += m_histogram[i];
                }
                if (i > 0) {
                        i--;
                }
                return i;
        }

        /**
         * Get the histogram as an array.
         * 
         * @return The histogram array
         */
        public int[] getArray() {
                return m_histogram;
        }
}
