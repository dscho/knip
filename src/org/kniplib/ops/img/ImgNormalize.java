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
 *   8 Dec 2010 (hornm): created
 */
package org.kniplib.ops.img;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import org.kniplib.data.statistics.KNIPLibHistogram;
import org.kniplib.ops.iterable.MakeHistogram;

/**
 * 
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 * @param <T>
 *                The type of the {@link Img} which will be normalized. Must be
 *                {@link RealType}
 */
public class ImgNormalize<T extends RealType<T>, I extends IterableInterval<T>>
                implements UnaryOperation<I, I> {

        private double m_saturation;

        private double[] m_minmax = new double[] { 1, 0 };

        private int m_numBins = -1;

        private MakeHistogram<T> m_histOp = null;

        private KNIPLibHistogram m_hist = null;

        /**
         * Normalizes an image.
         */
        public ImgNormalize() {
                this(0);
        }

        /**
         * @param saturation
         *                the percentage of pixels in the lower and upper domain
         *                to be ignored in the normalization
         */
        public ImgNormalize(double saturation) {
                m_saturation = saturation;
        }

        /**
         * @param min
         * @param max
         */
        public ImgNormalize(double min, double max) {
                m_minmax[0] = min;
                m_minmax[1] = max;
        }

        /**
         * 
         * @param saturation
         *                the percentage of pixels in the lower and upper domain
         *                to be
         * @param numBins
         *                number of bins in the histogram to determine the
         *                saturation, if T is not {@link IntegerType}, 256 bins
         *                are chosen automatically
         */
        public ImgNormalize(ImgFactory<T> fac, double saturation, int numBins) {
                this(saturation);
                m_numBins = numBins;
        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        @Override
        public I compute(I in, I res) {
                double[] minmax;
                if (m_minmax[0] > m_minmax[1]) {
                        minmax = getMinAndMax(in, m_saturation, in.size(),
                                        m_numBins);
                } else {
                        minmax = m_minmax;
                }
                manipulate(in, res, minmax);
                return res;
        }

        /**
         * Determines the minimum and factory for scaling according to the given
         * saturation
         * 
         * @param <T>
         * @param interval
         * @param saturation
         *                the percentage of pixels in the lower and upper domain
         *                to be ignored in the normalization
         * @return with the normalization factor at position 0, minimum of the
         *         image at position 1
         */
        public double[] getNormalizationProperties(I interval, double saturation) {

                double[] minMax = getMinAndMax(interval, saturation,
                                interval.size(), -1);
                return new double[] {
                                1
                                                / (minMax[1] - minMax[0])
                                                * (interval.firstElement()
                                                                .getMaxValue() - interval
                                                                .firstElement()
                                                                .getMinValue()),
                                minMax[0] };
        }

        private double[] getMinAndMax(I interval, double saturation,
                        long numPixels, int numBins) {
                T val = interval.firstElement().createVariable();

                if (saturation == 0) {
                        // calc min max without saturation
                        T maxVal = val.createVariable();
                        maxVal.setReal(val.getMinValue());
                        T minVal = val.createVariable();
                        minVal.setReal(val.getMaxValue());

                        Cursor<T> c = interval.cursor();

                        while (c.hasNext()) {
                                c.fwd();
                                if (c.get().compareTo(maxVal) > 0) {
                                        maxVal.set(c.get());
                                }
                                if (c.get().compareTo(minVal) < 0) {
                                        minVal.set(c.get());
                                }
                        }
                        return new double[] { minVal.getRealDouble(),
                                        maxVal.getRealDouble() };
                }
                // initialize histogram
                int bins;
                if (!(val instanceof IntegerType)) {
                        bins = Short.MAX_VALUE * 2;
                } else if (numBins < 0) {
                        bins = (int) (val.getMaxValue() - val.getMinValue() + 1);
                } else {
                        bins = numBins;
                }

                if (m_histOp == null) {
                        m_histOp = new MakeHistogram<T>(bins);
                        m_hist = m_histOp.createEmptyOutput(interval);
                } else {
                        if (!(m_hist.max() == val.getMaxValue()
                                        && m_hist.min() == val.getMinValue() && m_hist
                                                .numBins() == bins)) {
                                m_histOp = new MakeHistogram<T>(bins);
                                m_hist = m_histOp.createEmptyOutput(interval);
                        } else {
                                Arrays.fill(m_hist.hist(), 0);
                        }
                }
                m_histOp.compute(interval, m_hist);

                return calcMinMaxWithSaturation(interval, saturation,
                                numPixels, m_hist.hist());

        }

        private double[] calcMinMaxWithSaturation(I interval,
                        final double saturation, final long numPixels,
                        int[] hist) {
                T val = interval.firstElement().createVariable();
                int histMin = 0, histMax;
                int threshold = (int) (numPixels * saturation / 200.0);

                // find min
                int pCount = 0;
                for (int i = 0; i < hist.length; i++) {
                        pCount += hist[i];
                        if (pCount > threshold) {
                                histMin = i;
                                break;
                        }
                }

                // find max
                pCount = 0;
                histMax = hist.length - 1;
                for (int i = hist.length - 1; i >= 0; i--) {
                        pCount += hist[i];
                        if (pCount > threshold) {
                                histMax = i;
                                break;
                        }
                }
                return new double[] { histMin + val.getMinValue(),
                                histMax + val.getMinValue() };
        }

        private void manipulate(I in, I out, double[] minMax) {

                T type = in.firstElement().createVariable();
                T minVal = type.createVariable();
                T maxVal = type.createVariable();
                minVal.setReal(minVal.getMinValue());
                maxVal.setReal(maxVal.getMaxValue());

                double factor = 1 / (minMax[1] - minMax[0])
                                * ((type.getMaxValue() - type.getMinValue()));
                double minNormVal = type.getMinValue();

                Cursor<T> inCursor = in.cursor();
                Cursor<T> outCursor = out.cursor();
                while (inCursor.hasNext()) {
                        inCursor.fwd();
                        outCursor.fwd();
                        if (inCursor.get().getRealDouble() <= minMax[0]) {
                                outCursor.get().set(minVal);
                        } else if (inCursor.get().getRealDouble() >= minMax[1]) {
                                outCursor.get().set(maxVal);
                        } else {
                                outCursor.get()
                                                .setReal((inCursor
                                                                .get()
                                                                .getRealDouble() - minMax[0])
                                                                * factor
                                                                + minNormVal);
                        }
                }
        }

        public UnaryOperation<I, I> copy() {
                return new ImgNormalize<T, I>(m_saturation, m_numBins);
        }

}
