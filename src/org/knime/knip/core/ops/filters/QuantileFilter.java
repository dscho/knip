package org.knime.knip.core.ops.filters;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.data.statistics.QuantileHistogram;

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
