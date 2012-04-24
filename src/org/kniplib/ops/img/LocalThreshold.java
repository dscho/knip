/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 * 
 * History
 *   30 Dec 2010 (hornm): created
 */
package org.kniplib.ops.img;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.roi.RectangleRegionOfInterest;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.kniplib.algorithm.types.LocalThresholderType;
import org.kniplib.ops.iterable.Sum;

/**
 * Local Thresholder with adaptive calculation of the statistics. Several
 * methods, including a approach from Timo Simnacher (University of Konstanz)
 * included. Still missing:
 * 
 * Median, Sauvola, Sue Wue, Wolf & Jolion, Bernsen
 * 
 * Implemented: Niblack, Simnacher, Mean, Midgrey
 * 
 * @author dietzc, metznerj, simnachert (University of Konstanz)
 */
public class LocalThreshold<T extends RealType<T>, K extends IterableInterval<T> & RandomAccessibleInterval<T>, V extends IterableInterval<BitType> & RandomAccessibleInterval<BitType>>
                implements UnaryOperation<K, V> {

        /* Weight of the global/local mean according to the method */
        private double m_meanWeight;

        /* Weight of the standard deviation */
        private double m_standardDeviationWeight;

        /* The method used to calculate the local threshold */
        private LocalThresholderType m_type;

        /*
         * HELPERS TO SAVE MEMORY
         */

        /* Inital origin of the sliding window */
        private double[] m_roiOrigin;

        /* Extend of the sliding window */
        private double[] m_roiExtend;

        /* The actual size of the individual dimensions of the sliding window */
        private int m_windowSize;

        /* region of interest (sliding window) */
        private RectangleRegionOfInterest m_roi;

        /* Cursor of the resulting BitType Img */
        private Cursor<BitType> m_resCursor;

        /* Cursor over the source */
        private Cursor<T> m_srcCursor;

        /* Region of interest cursor */
        private Cursor<T> m_roiCursor;

        /* local standard deviation */
        private double m_localStandardDeviation;

        /* local mean */
        private double m_localMean;

        /* local sum */
        private int m_localSum;

        /* local sum of squares */
        private int m_localSumOfSquares;

        /* local minValue */
        private double m_localMax;

        /* local maxValue */
        private double m_localMin;

        /*
         * temporary helper list for adaptive calculation of min/max/sum/sumsq
         * etc.
         */
        private List<Double> m_p;

        /**
         * 
         * @param type
         *                method which will be used for local thresholding
         * @param meanWeight
         *                weighting of the mean value. used in simnacher
         *                (global) and niblack (local), otherwise ignored
         * @param stdDevWeight
         *                weighting of the local standard deviation. used in
         *                simnacher, otherwise ignored
         * @param windowSize
         *                size of the window. Muss be even.
         * 
         */
        public LocalThreshold(LocalThresholderType type, double meanWeight,
                        double stdDevWeight, int windowSize) {
                m_meanWeight = meanWeight;
                m_standardDeviationWeight = stdDevWeight;
                m_type = type;
                m_windowSize = 2 * windowSize + 1;
                m_p = new ArrayList<Double>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V compute(K src, V res) {

                final int numPixLocalWindow = (int) Math.pow(m_windowSize,
                                src.numDimensions());

                final double sum = new Sum<T>().compute(src).getRealDouble();
                final long numTotalPix = src.size();
                final double m_globalMean = sum / numTotalPix;

                if (m_roi == null
                                || m_roiOrigin.length != m_roi.numDimensions()) {
                        m_roiOrigin = new double[src.numDimensions()];
                        m_roiExtend = new double[src.numDimensions()];

                        for (int d = 0; d < m_roiOrigin.length; d++) {
                                m_roiOrigin[d] = -((m_windowSize - 1) / 2);
                                m_roiExtend[d] = (m_windowSize);
                        }
                        m_roi = new RectangleRegionOfInterest(m_roiOrigin,
                                        m_roiExtend);
                }

                m_resCursor = res.cursor();
                m_srcCursor = src.cursor();
                m_srcCursor.reset();

                m_roiCursor = m_roi.getIterableIntervalOverROI(
                                Views.extendMirrorDouble(src)).cursor();

                // TODO: Arraylist -> Array?

                m_p.clear();
                m_localSum = 0;
                m_localSumOfSquares = 0;
                m_localMax = Double.MIN_VALUE;
                m_localMin = Double.MAX_VALUE;

                while (m_srcCursor.hasNext()) {
                        m_srcCursor.fwd();
                        m_resCursor.fwd();
                        final double val = m_srcCursor.get().getRealDouble();

                        for (int d = 0; d < m_roiOrigin.length; d++) {
                                m_roiOrigin[d] = m_srcCursor.getIntPosition(d)
                                                - ((m_windowSize - 1) / 2);
                        }

                        m_roi.setOrigin(m_roiOrigin);
                        m_roiCursor.reset();

                        m_localSum = 0;
                        m_localSumOfSquares = 0;
                        m_localMax = val;
                        m_localMin = val;

                        // TODO optimize speed, see code below (very slow at the
                        // moment)
                        while (m_roiCursor.hasNext()) {
                                final double o = m_roiCursor.next()
                                                .getRealDouble();
                                m_localSum += o;
                                m_localSumOfSquares += o * o;
                                m_localMax = Math.max(m_localMax, o);
                                m_localMin = Math.min(m_localMin, o);
                        }
                        /*
                         * if (m_srcCursor.getIntPosition(0) != 0) {
                         * 
                         * for (int j = ii; j < numPixLocalWindow; j +=
                         * m_windowSize) {
                         * 
                         * // TODO: clear local max/min (this is WRONG at the
                         * moment)
                         * 
                         * // TODO: Workaround jmpFwd for (int i = 0; i <
                         * m_windowSize; i++) { m_roiCursor.fwd(); } final
                         * double o = m_roiCursor.get().getRealDouble();
                         * m_localMax = Math.max(m_localMax, o); m_localMin =
                         * Math.min(m_localMin, o); m_localSum += o -
                         * m_p.get(j); m_localSumOfSquares += o * o - m_p.get(j)
                         * * m_p.get(j); m_p.set(j, o); } ii = (ii + 1) %
                         * m_windowSize; } else {
                         * 
                         * m_p.clear(); m_localSum = 0; m_localSumOfSquares = 0;
                         * 
                         * while (m_roiCursor.hasNext()) { final double o =
                         * m_roiCursor.next().getRealDouble(); long[] pos = new
                         * long[m_roiCursor.numDimensions()];
                         * m_roiCursor.localize(pos); m_p.add(o); m_localSum +=
                         * o; m_localSumOfSquares += o * o; m_localMax =
                         * Math.max(m_localMax, o); m_localMin =
                         * Math.min(m_localMin, o); } ii = 0; }
                         */

                        m_localMean = m_localSum / numPixLocalWindow;
                        m_localStandardDeviation = Math
                                        .sqrt(m_localSumOfSquares
                                                        / numPixLocalWindow
                                                        - Math.pow(m_localMean,
                                                                        2));

                        switch (m_type) {
                        case MEAN:
                                m_resCursor.get().set(val > m_localMean);
                                break;
                        case MIDGREY:
                                m_resCursor.get()
                                                .set(val > (m_localMax - m_localMin) / 2);
                                break;
                        case NIBLACK:
                                m_resCursor.get()
                                                .set(val > (m_localMean + m_standardDeviationWeight
                                                                * m_localStandardDeviation));
                                break;
                        case SIMNACHER:
                                m_resCursor.get()
                                                .set(val > (m_standardDeviationWeight * m_localStandardDeviation)
                                                                && val > (m_meanWeight * m_globalMean));
                                break;
                        }
                }

                return res;
        }

        @Override
        public UnaryOperation<K, V> copy() {
                return new LocalThreshold<T, K, V>(m_type, m_meanWeight,
                                m_standardDeviationWeight, m_windowSize);
        }
}
