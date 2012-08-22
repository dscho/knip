package org.knime.knip.core.ops.filters;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IterableRandomAccessibleInterval;

/**
 * Bilateral filtering
 *
 * @author tcriess, University of Konstanz
 */
public class BilateralFilter<T extends RealType<T>, K extends RandomAccessibleInterval<T> & IterableInterval<T>>
                implements UnaryOperation<K, K> {

        public final static int MIN_DIMS = 2;

        public final static int MAX_DIMS = 2;

        private double m_sigma_r = 15;

        private double m_sigma_s = 5;

        private int m_radius = 10;

        public BilateralFilter(double sigma_r, double sigma_s, int radius) {
                m_sigma_r = sigma_r;
                m_sigma_s = sigma_s;
                m_radius = radius;
        }

        private static double gauss(double x, double sigma) {
                double mu = 0.0;
                return (1 / (sigma * Math.sqrt(2 * Math.PI)))
                                * Math.exp(-0.5 * (x - mu) * (x - mu)
                                                / (sigma * sigma));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public K compute(K srcIn, K res) {

                if (srcIn.numDimensions() != 2)
                        throw new IllegalArgumentException(
                                        "Input must be two dimensional");

                long[] size = new long[srcIn.numDimensions()];
                srcIn.dimensions(size);

                RandomAccess<T> cr = res.randomAccess();
                Cursor<T> cp = srcIn.localizingCursor();
                int[] p = new int[srcIn.numDimensions()];
                int[] q = new int[srcIn.numDimensions()];
                long[] mi = new long[srcIn.numDimensions()];
                long[] ma = new long[srcIn.numDimensions()];
                long mma1 = srcIn.max(0);
                long mma2 = srcIn.max(1);
                IterableRandomAccessibleInterval<T> si;
                Cursor<T> cq;
                while (cp.hasNext()) {
                        cp.fwd();
                        cp.localize(p);
                        double d;
                        // Cursor<T> cq = srcIn.localizingCursor();
                        cp.localize(mi);
                        cp.localize(ma);
                        mi[0] = Math.max(0, mi[0] - m_radius);
                        mi[1] = Math.max(0, mi[1] - m_radius);
                        ma[0] = Math.min(mma1, mi[0] + m_radius);
                        ma[1] = Math.min(mma2, mi[1] + m_radius);
                        Interval in = new FinalInterval(mi, ma);
                        si = new IterableRandomAccessibleInterval<T>(
                                        SubsetViews.iterableSubsetView(srcIn,
                                                        in));
                        cq = si.localizingCursor();
                        double s, v = 0.0;
                        double w = 0.0;
                        while (cq.hasNext()) {
                                cq.fwd();
                                cq.localize(q);
                                // d = 0.0;
                                d = (p[0] - q[0] - mi[0])
                                                * (p[0] - q[0] - mi[0])
                                                + (p[1] - q[1] - mi[1])
                                                * (p[1] - q[1] - mi[1]);
                                // for(int i=0; i<q.length; i++) {
                                // d += (p[i]-q[i]-mi[i])*(p[i]-q[i]-mi[i]);
                                // }
                                d = Math.sqrt(d);
                                s = gauss(d, m_sigma_s);

                                d = Math.abs(cp.get().getRealDouble()
                                                - cq.get().getRealDouble());
                                s *= gauss(d, m_sigma_r);

                                v += s * cq.get().getRealDouble();
                                w += s;
                        }
                        cr.setPosition(p);
                        cr.get().setReal(v / w);
                }
                return res;

        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new BilateralFilter<T, K>(m_sigma_r, m_sigma_s, m_radius);
        }
}
