package org.knime.knip.core.ops.filters;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Bilateral filtering
 * 
 * @author tcriess, University of Konstanz
 */
public class BilateralFilter<T extends RealType<T>, K extends RandomAccessibleInterval<T> & IterableInterval<T>>
        implements UnaryOperation<K, K> {

    public final static int MIN_DIMS = 2;

    public final static int MAX_DIMS = 2;

    private double m_sigmaR = 15;

    private double m_sigmaS = 5;

    private int m_radius = 10;

    public BilateralFilter(final double sigma_r, final double sigma_s, final int radius) {
        m_sigmaR = sigma_r;
        m_sigmaS = sigma_s;
        m_radius = radius;
    }

    private static double gauss(final double x, final double sigma) {
        final double mu = 0.0;
        return (1 / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp((-0.5 * (x - mu) * (x - mu)) / (sigma * sigma));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public K compute(final K srcIn, final K res) {

        if (srcIn.numDimensions() != 2) {
            throw new IllegalArgumentException("Input must be two dimensional");
        }

        final long[] size = new long[srcIn.numDimensions()];
        srcIn.dimensions(size);

        final RandomAccess<T> cr = res.randomAccess();
        final Cursor<T> cp = srcIn.localizingCursor();
        final int[] p = new int[srcIn.numDimensions()];
        final int[] q = new int[srcIn.numDimensions()];
        final long[] mi = new long[srcIn.numDimensions()];
        final long[] ma = new long[srcIn.numDimensions()];
        final long mma1 = srcIn.max(0);
        final long mma2 = srcIn.max(1);
        IterableInterval<T> si;
        Cursor<T> cq;
        while (cp.hasNext()) {
            cp.fwd();
            cp.localize(p);
            double d;
            cp.localize(mi);
            cp.localize(ma);
            mi[0] = Math.max(0, mi[0] - m_radius);
            mi[1] = Math.max(0, mi[1] - m_radius);
            ma[0] = Math.min(mma1, ma[0] + m_radius);
            ma[1] = Math.min(mma2, ma[1] + m_radius);
            final Interval in = new FinalInterval(mi, ma);
            si = Views.iterable(SubsetOperations.subsetview(srcIn, in));
            cq = si.localizingCursor();
            double s, v = 0.0;
            double w = 0.0;
            while (cq.hasNext()) {
                cq.fwd();
                cq.localize(q);
                d = ((p[0] - q[0] - mi[0]) * (p[0] - q[0] - mi[0])) + ((p[1] - q[1] - mi[1]) * (p[1] - q[1] - mi[1]));
                d = Math.sqrt(d);
                s = gauss(d, m_sigmaS);

                d = Math.abs(cp.get().getRealDouble() - cq.get().getRealDouble());
                s *= gauss(d, m_sigmaR);

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
        return new BilateralFilter<T, K>(m_sigmaR, m_sigmaS, m_radius);
    }
}
