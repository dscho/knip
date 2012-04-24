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

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

import org.kniplib.data.statistics.QuantileHistogram;

/**
 * QuantileFilter
 * 
 * @author tcriess, University of Konstanz
 */
public class QuantileFilter<T extends RealType<T>, K extends IterableInterval<T> & RandomAccessibleInterval<T>>
                implements UnaryOperation<K, K> {

        public final static int MIN_DIMS = 2;

        public final static int MAX_DIMS = 2;

        private int m_radius = 3;

        private int m_quantile = 50;

        /**
         * 
         * @param radius
         * @param quantile
         */
        public QuantileFilter(int radius, int quantile) {
                m_radius = radius;
                m_quantile = quantile;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K compute(K src, K res) {

                /*
                 * ImgMap<UnsignedByteType, T> map = new
                 * ImgMap<UnsignedByteType, T>( new Convert<UnsignedByteType,
                 * T>(res.firstElement() .createVariable(),
                 * src.firstElement()));
                 * 
                 * res = map.compute(src);
                 */

                // res = srcIn;

                RandomAccess<T> resAccess = res.randomAccess();
                RandomAccess<T> srcAccess = src.randomAccess();

                int n = (int) src.dimension(0);
                int m = (int) src.dimension(1);

                int minx = 0;
                int maxx = (int) src.dimension(0);
                int miny = 0;
                int maxy = (int) src.dimension(1);

                // int maxx = Integer.MIN_VALUE;
                // int minx = Integer.MAX_VALUE;
                // int maxy = Integer.MIN_VALUE;
                // int miny = Integer.MAX_VALUE;

                int xrange = n;
                int yrange = m;

                int pixelrange = (int) (srcAccess.get().getMaxValue() - srcAccess
                                .get().getMinValue());

                // TODO: Binning of histogram
                // initialise column histograms and blockhistogram
                QuantileHistogram blockhistogram = new QuantileHistogram(
                                pixelrange);
                QuantileHistogram[] columnhistograms = new QuantileHistogram[xrange];
                for (int i = 0; i < xrange; i++) {
                        columnhistograms[i] = new QuantileHistogram(pixelrange);
                }

                int act_x_radius = 0, act_y_radius = 0;
                int x, y;
                int pixel;
                int actx, acty;

                // iterate through all rows
                for (int i = 0; i < yrange; i++) {
                        y = miny + i;

                        // compute the actual radius in y direction (respect the
                        // boundary!)
                        if (y - m_radius >= miny) {
                                if (y + m_radius <= maxy) {
                                        act_y_radius = m_radius;
                                } else {
                                        act_y_radius = Math.max(0, maxy - y);
                                }
                        } else {
                                if (2 * y <= maxy) {
                                        act_y_radius = y;
                                } else {
                                        act_y_radius = Math.max(0, maxy - y);
                                }
                        }

                        // clear the current blockhistogram (must be
                        // reconstructed at the
                        // boundaries anyway)
                        blockhistogram.clear();

                        // iterate through all columns
                        for (int j = 0; j < xrange; j++) {
                                x = minx + j;

                                // compute the actual radius in x direction
                                // (respect the
                                // boundary!)
                                if (x - m_radius >= minx) {
                                        if (x + m_radius <= maxx) {
                                                act_x_radius = m_radius;
                                        } else {
                                                act_x_radius = Math.max(0, maxx
                                                                - x);
                                        }
                                } else {
                                        if (2 * x <= maxx) {
                                                act_x_radius = x;
                                        } else {
                                                act_x_radius = Math.max(0, maxx
                                                                - x);
                                        }
                                }

                                srcAccess.setPosition(x, 0);
                                // cursor.setPosition(x, dimx);

                                // set the column histogram
                                if (i <= m_radius) {
                                        // special treatment for the first
                                        // radius rows
                                        acty = y + act_y_radius;

                                        // cursor.setPosition(acty, dimy);
                                        srcAccess.setPosition(acty, 1);
                                        pixel = (int) (srcAccess.get()
                                                        .getRealDouble() - srcAccess
                                                        .get().getMinValue());

                                        columnhistograms[j].addPixel(pixel);
                                        acty--;
                                        if (acty > 0) {
                                                srcAccess.setPosition(acty, 1);
                                                // cursor.setPosition(acty,
                                                // dimy);
                                                pixel = (int) (srcAccess
                                                                .get()
                                                                .getRealDouble() - srcAccess
                                                                .get()
                                                                .getMinValue());
                                                columnhistograms[j]
                                                                .addPixel(pixel);
                                        }
                                } else {
                                        if (i >= yrange - m_radius) {
                                                // special treatment for the
                                                // last radius rows
                                                acty = y - act_y_radius - 1;
                                                if (acty >= 0) {
                                                        srcAccess.setPosition(
                                                                        acty, 1);

                                                        // cursor.setPosition(acty,
                                                        // dimy);
                                                        pixel = (int) (srcAccess
                                                                        .get()
                                                                        .getRealDouble() - srcAccess
                                                                        .get()
                                                                        .getMinValue());
                                                        columnhistograms[j]
                                                                        .subPixel(pixel);
                                                        acty--;
                                                        if (acty >= 0) {
                                                                // cursor.setPosition(acty,
                                                                // dimy);
                                                                srcAccess.setPosition(
                                                                                acty,
                                                                                1);
                                                                pixel = (int) (srcAccess
                                                                                .get()
                                                                                .getRealDouble() - srcAccess
                                                                                .get()
                                                                                .getMinValue());
                                                                columnhistograms[j]
                                                                                .subPixel(pixel);
                                                        }
                                                }
                                        } else {
                                                if (y - m_radius - 1 >= miny
                                                                && y
                                                                                - m_radius
                                                                                - 1 <= maxy) {
                                                        // cursor.setPosition(y
                                                        // - m_radius - 1,
                                                        // dimy);
                                                        srcAccess.setPosition(
                                                                        y
                                                                                        - m_radius
                                                                                        - 1,
                                                                        1);
                                                        pixel = (int) (srcAccess
                                                                        .get()
                                                                        .getRealDouble() - srcAccess
                                                                        .get()
                                                                        .getMinValue());
                                                        columnhistograms[j]
                                                                        .subPixel(pixel);
                                                }
                                                if (y + m_radius >= miny
                                                                && y + m_radius <= maxy) {
                                                        // cursor.setPosition(y
                                                        // + m_radius, dimy);
                                                        srcAccess.setPosition(
                                                                        y
                                                                                        + m_radius,
                                                                        1);
                                                        pixel = (int) (srcAccess
                                                                        .get()
                                                                        .getRealDouble() - srcAccess
                                                                        .get()
                                                                        .getMinValue());
                                                        columnhistograms[j]
                                                                        .addPixel(pixel);
                                                }
                                        }
                                }
                        }

                        // iterate through all columns again
                        for (int j = 0; j < xrange; j++) {
                                x = minx + j;

                                // compute the actual radius in x direction
                                // (respect the
                                // boundary!)
                                if (x - m_radius >= minx) {
                                        if (x + m_radius <= maxx) {
                                                act_x_radius = m_radius;
                                        } else {
                                                act_x_radius = Math.max(0, maxx
                                                                - x);
                                        }
                                } else {
                                        if (2 * x <= maxx) {
                                                act_x_radius = x;
                                        } else {
                                                act_x_radius = Math.max(0, maxx
                                                                - x);
                                        }
                                }

                                // set the block histogram
                                if (j <= m_radius) {
                                        // special treatment for the first
                                        // radius columns
                                        actx = x + act_x_radius;
                                        if (actx >= minx && actx <= maxx) {
                                                blockhistogram.add(columnhistograms[actx
                                                                - minx]);
                                                actx--;
                                                if (actx >= minx
                                                                && actx <= maxx) {
                                                        blockhistogram.add(columnhistograms[actx
                                                                        - minx]);
                                                }
                                        }
                                } else {
                                        if (j >= xrange - m_radius) {
                                                // special treatment for the
                                                // last radius columns
                                                actx = x - act_x_radius - 1;
                                                if (actx >= minx
                                                                && actx <= maxx) {
                                                        blockhistogram.sub(columnhistograms[actx
                                                                        - minx]);
                                                        actx--;
                                                        if (actx >= minx
                                                                        && actx <= maxx) {
                                                                blockhistogram.sub(columnhistograms[actx
                                                                                - minx]);
                                                        }
                                                }
                                        } else {
                                                if (x - m_radius - 1 >= minx
                                                                && x
                                                                                - m_radius
                                                                                - 1 <= maxx) {
                                                        blockhistogram.sub(columnhistograms[x
                                                                        - minx
                                                                        - m_radius
                                                                        - 1]);
                                                }
                                                if (x + m_radius >= minx
                                                                && x + m_radius <= maxx) {
                                                        blockhistogram.add(columnhistograms[x
                                                                        - minx
                                                                        + m_radius]);
                                                }
                                        }
                                }

                                resAccess.setPosition(x, 0);
                                resAccess.setPosition(y, 1);

                                resAccess.get()
                                                .setReal(blockhistogram
                                                                .getQuantile(m_quantile));

                        }

                }

                return res;
        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new QuantileFilter<T, K>(m_radius, m_quantile);
        }
}
