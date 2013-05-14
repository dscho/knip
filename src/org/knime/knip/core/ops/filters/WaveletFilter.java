package org.knime.knip.core.ops.filters;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.roi.RectangleRegionOfInterest;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Image projection.
 * 
 * @author jmetzner, University of Konstanz
 */
public class WaveletFilter<T extends RealType<T>, K extends IterableInterval<T> & RandomAccessibleInterval<T>>
implements UnaryOperation<K, K> {

    public final static int MIN_DIMS = 1;

    public final static int MAX_DIMS = 3;

    private final ExecutorService m_executor;

    private final double m_lambda_min;

    private final double m_lambda_max;

    private final double m_ignorePercent;

    /* Inital ROI */
    private RectangleRegionOfInterest m_SelectedRowRoi;

    /* Inital origin of the sliding window */
    private double[] m_SelectedRowRoiOrigin;

    /* Extend of the sliding window */
    private double[] m_SelectedRowRoiExtend;

    /* Region of interest SrcCur */
    private Cursor<T> m_SelectedRowTempRoiCur;

    /* Region of interest SrcCur */
    private Cursor<T> m_srcCursor;

    /* Region of interest SrcCur */
    // private RandomAccess<T> m_resRandomAccess;

    /* Inital ROI */
    private RectangleRegionOfInterest m_BeginRoi;

    /* Inital origin of the sliding window */
    private double[] m_BeginRoiOrigin;

    /* Extend of the sliding window */
    private double[] m_BeginRoiExtend;

    /* Region of interest SrcCur */
    private Cursor<T> m_BeginTempRoiCur;

    /* Region of interest SrcCur */
    // private Cursor< T > m_BeginResRoiCur;

    /* Region of interest SrcCur */
    private int m_rowLength;

    private RandomAccess<T> m_tempRandomAccess;

    private Cursor<T> m_resCursor;

    private Cursor<T> m_tempCursor;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public K compute(final K src, final K res) {
        final int numDim = src.numDimensions();
        if (numDim > MAX_DIMS) {
            throw new IllegalArgumentException("Too many dimensions are selected.");
        }
        if (src.numDimensions() < MIN_DIMS) {
            throw new IllegalArgumentException("Two dimensions have to be selected.");
        }
        final boolean[] dimSrcCur = new boolean[numDim];
        final long[] dims = new long[numDim];
        final long[] useDims = new long[dims.length];

        for (int i = 0; i < numDim; ++i) {
            final long d = src.dimension(i);
            useDims[i] = (long)(d * (1 - m_ignorePercent));
            if (dimSrcCur[i] = (src.dimension(i) > 1)) {
                int n = 2;
                while (d > n) {
                    n <<= 1;
                }
                dims[i] = n;
            } else {
                dims[i] = d;
            }
        }
        Img<T> temp;
        try {
            temp =
                    new ArrayImgFactory().imgFactory(src.firstElement().createVariable())
                    .create(dims, src.firstElement().createVariable());
            m_tempRandomAccess = temp.randomAccess();
        } catch (final IncompatibleTypeException e1) {
            throw new IllegalArgumentException("Cannot create temp img.");
        }
        m_srcCursor = src.cursor();

        while (m_srcCursor.hasNext()) {
            m_srcCursor.fwd();
            m_tempRandomAccess.setPosition(m_srcCursor);
            m_tempRandomAccess.get().setReal(m_srcCursor.get().getRealDouble());
        }
        m_srcCursor.reset();

        final T obj = src.firstElement();
        obj.setReal(0);

        final Queue<FutureTask<WaveletDecompositionThread>> hashQueueDecomposition =
                new LinkedList<FutureTask<WaveletDecompositionThread>>();
        final Queue<FutureTask<WaveletCompositionThread>> hashQueueComposition =
                new LinkedList<FutureTask<WaveletCompositionThread>>();

        {
            for (int dim = 0; dim < numDim; ++dim) {
                if (dimSrcCur[dim]) {
                    if ((m_SelectedRowRoi == null)
                            || ((m_SelectedRowRoiOrigin.length != m_SelectedRowRoi.numDimensions()) && (m_BeginRoi == null))
                            || (m_BeginRoiOrigin.length != m_BeginRoi.numDimensions())) {
                        m_SelectedRowRoiOrigin = new double[numDim];
                        m_SelectedRowRoiExtend = new double[numDim];
                        m_BeginRoiOrigin = new double[numDim];
                        m_BeginRoiExtend = new double[numDim];
                    }
                    for (int d = 0; d < numDim; d++) {
                        if (dim == d) {
                            m_rowLength = (int)temp.dimension(d);
                            m_SelectedRowRoiExtend[d] = temp.dimension(d);
                            m_BeginRoiExtend[d] = 1;
                        } else {
                            m_SelectedRowRoiExtend[d] = 1;
                            m_BeginRoiExtend[d] = temp.dimension(d);
                        }
                        m_SelectedRowRoiOrigin[d] = 0;
                        m_BeginRoiOrigin[d] = 0;
                    }
                    m_BeginRoi = new RectangleRegionOfInterest(m_BeginRoiOrigin, m_BeginRoiExtend);
                    m_BeginTempRoiCur = m_BeginRoi.getIterableIntervalOverROI(Views.extendValue(temp, obj)).cursor();
                    m_BeginRoi.setOrigin(m_BeginRoiOrigin);

                    m_SelectedRowRoi = new RectangleRegionOfInterest(m_SelectedRowRoiOrigin, m_SelectedRowRoiExtend);
                    m_SelectedRowTempRoiCur =
                            m_SelectedRowRoi.getIterableIntervalOverROI(Views.extendValue(temp, obj)).cursor();
                    // m_SelectedRowRoi.setOrigin(m_SelectedRowRoiOrigin);

                    while (m_BeginTempRoiCur.hasNext()) {
                        m_BeginTempRoiCur.next();
                        m_tempRandomAccess.setPosition(m_BeginTempRoiCur);
                        final double[] pos = new double[numDim];
                        for (int d = 0; d < numDim; d++) {
                            pos[d] = m_tempRandomAccess.getDoublePosition(d);
                        }
                        m_SelectedRowRoiOrigin = pos;
                        m_SelectedRowRoi.setOrigin(m_SelectedRowRoiOrigin);
                        int p = 0;
                        final double[] row = new double[m_rowLength];
                        while (m_SelectedRowTempRoiCur.hasNext()) {
                            row[p++] = m_SelectedRowTempRoiCur.next().getRealDouble();
                        }
                        m_SelectedRowTempRoiCur.reset();
                        final WaveletDecompositionThread wave = new WaveletDecompositionThread(row, pos);
                        final FutureTask<WaveletDecompositionThread> task =
                                new FutureTask<WaveletDecompositionThread>(wave);
                        hashQueueDecomposition.add(task);
                        m_executor.execute(task);
                    }
                    m_BeginTempRoiCur.reset();

                    while (!hashQueueDecomposition.isEmpty()) {
                        final FutureTask<WaveletDecompositionThread> result = hashQueueDecomposition.poll();
                        try {
                            final WaveletDecompositionThread wave = result.get();
                            m_tempRandomAccess.setPosition(wave.getPos());
                            final double[] row = wave.getWaveOut();
                            for (int p = 0; p < m_rowLength; ++p) {
                                m_tempRandomAccess.setPosition(p, dim);
                                m_tempRandomAccess.get().setReal(row[p]);
                            }
                        } catch (final ExecutionException e) {
                            final Throwable th = e.getCause();
                            if (th == null) {
                                throw new IllegalArgumentException("Unknow Error during execution.");
                            } else if (th instanceof InterruptedException) {
                                throw new IllegalArgumentException("Canceled");
                            } else {
                                throw new IllegalArgumentException("Error:" + th);
                            }
                        } catch (final InterruptedException e) {
                            throw new IllegalArgumentException("Canceled");
                        }
                    }
                }
            }
        }

        {

            /**
             * Wavelet Filter
             */

            m_tempCursor = temp.cursor();

            while (m_tempCursor.hasNext()) {

                if ((m_tempCursor.next().getRealDouble() < m_lambda_max)
                        && (m_tempCursor.get().getRealDouble() > m_lambda_min)) {
                    m_tempCursor.get().setZero();
                }
                for (int i = 0; i < useDims.length; ++i) {
                    if (useDims[i] < m_tempCursor.getDoublePosition(i)) {
                        m_tempCursor.get().setZero();
                        continue;
                    }
                }
            }

            m_tempCursor.reset();

        }

        {

            for (int dim = numDim - 1; 0 <= dim; --dim) {
                if (dimSrcCur[dim]) {
                    if ((m_SelectedRowRoi == null)
                            || ((m_SelectedRowRoiOrigin.length != m_SelectedRowRoi.numDimensions()) && (m_BeginRoi == null))
                            || (m_BeginRoiOrigin.length != m_BeginRoi.numDimensions())) {
                        m_SelectedRowRoiOrigin = new double[numDim];
                        m_SelectedRowRoiExtend = new double[numDim];
                        m_BeginRoiOrigin = new double[numDim];
                        m_BeginRoiExtend = new double[numDim];
                    }
                    for (int d = 0; d < numDim; d++) {
                        if (dim == d) {
                            m_rowLength = (int)temp.dimension(d);
                            m_SelectedRowRoiExtend[d] = temp.dimension(d);
                            m_BeginRoiExtend[d] = 1;
                        } else {
                            m_SelectedRowRoiExtend[d] = 1;
                            m_BeginRoiExtend[d] = temp.dimension(d);
                        }
                        m_SelectedRowRoiOrigin[d] = 0;
                        m_BeginRoiOrigin[d] = 0;
                    }
                    m_BeginRoi = new RectangleRegionOfInterest(m_BeginRoiOrigin, m_BeginRoiExtend);
                    m_BeginTempRoiCur = m_BeginRoi.getIterableIntervalOverROI(Views.extendValue(temp, obj)).cursor();
                    m_BeginRoi.setOrigin(m_BeginRoiOrigin);

                    m_SelectedRowRoi = new RectangleRegionOfInterest(m_SelectedRowRoiOrigin, m_SelectedRowRoiExtend);
                    m_SelectedRowTempRoiCur =
                            m_SelectedRowRoi.getIterableIntervalOverROI(Views.extendValue(temp, obj)).cursor();
                    // m_SelectedRowRoi.setOrigin(m_SelectedRowRoiOrigin);

                    while (m_BeginTempRoiCur.hasNext()) {
                        m_BeginTempRoiCur.next();
                        m_tempRandomAccess.setPosition(m_BeginTempRoiCur);
                        final double[] pos = new double[numDim];
                        for (int d = 0; d < numDim; d++) {
                            pos[d] = m_tempRandomAccess.getDoublePosition(d);
                        }
                        m_SelectedRowRoiOrigin = pos;
                        m_SelectedRowRoi.setOrigin(m_SelectedRowRoiOrigin);
                        int p = 0;
                        final double[] row = new double[m_rowLength];
                        while (m_SelectedRowTempRoiCur.hasNext()) {
                            row[p++] = m_SelectedRowTempRoiCur.next().getRealDouble();
                        }
                        m_SelectedRowTempRoiCur.reset();
                        final WaveletCompositionThread wave = new WaveletCompositionThread(row, pos);
                        final FutureTask<WaveletCompositionThread> task =
                                new FutureTask<WaveletCompositionThread>(wave);
                        hashQueueComposition.add(task);
                        m_executor.execute(task);
                    }
                    m_BeginTempRoiCur.reset();

                    while (!hashQueueComposition.isEmpty()) {
                        final FutureTask<WaveletCompositionThread> result = hashQueueComposition.poll();
                        try {
                            final WaveletCompositionThread wave = result.get();
                            m_tempRandomAccess.setPosition(wave.getPos());
                            final double[] row = wave.getWaveOut();
                            for (int p = 0; p < m_rowLength; ++p) {
                                m_tempRandomAccess.setPosition(p, dim);
                                m_tempRandomAccess.get().setReal(row[p]);
                            }
                        } catch (final ExecutionException e) {
                            final Throwable th = e.getCause();
                            if (th == null) {
                                throw new IllegalArgumentException("Unknow Error during execution.");
                            } else if (th instanceof InterruptedException) {
                                throw new IllegalArgumentException("Canceled");
                            } else {
                                throw new IllegalArgumentException("Error:" + th);
                            }
                        } catch (final InterruptedException e) {
                            throw new IllegalArgumentException("Canceled");
                        }
                    }
                }
            }
        }

        m_resCursor = res.cursor();

        while (m_resCursor.hasNext()) {
            m_resCursor.fwd();
            m_tempRandomAccess.setPosition(m_resCursor);
            m_resCursor.get().setReal(m_tempRandomAccess.get().getRealDouble());
        }
        m_resCursor.reset();

        return res;
    }

    private class WaveletDecompositionThread implements Callable<WaveletDecompositionThread> {
        final double[] m_waveOut;

        final double[] m_waveIn;

        final long[] m_pos;

        public WaveletDecompositionThread(final double[] in, final double[] pos) {
            m_waveOut = new double[in.length];
            m_waveIn = in;
            this.m_pos = new long[pos.length];
            for (int d = 0; d < pos.length; ++d) {
                this.m_pos[d] = (long)pos[d];
            }
        }

        public long[] getPos() {
            return m_pos;
        }

        public double[] getWaveOut() {
            return m_waveOut;
        }

        @Override
        public WaveletDecompositionThread call() {
            int length = m_waveIn.length;
            while (length > 1) {
                for (int i = 0; i < (length >> 1); ++i) {
                    final int p = i << 1;
                    m_waveOut[i] = (m_waveIn[p] + m_waveIn[p + 1]) / 2;
                    m_waveOut[(length >> 1) + i] = (m_waveIn[p] - m_waveIn[p + 1]) / 2;
                }
                for (int i = 0; i < length; ++i) {
                    m_waveIn[i] = m_waveOut[i];
                }
                length >>= 1;
            }
            return this;
        }
    }

    class WaveletCompositionThread implements Callable<WaveletCompositionThread> {
        final double[] waveOut;

        final double[] waveIn;

        final long[] pos;

        public WaveletCompositionThread(final double[] in, final double[] pos) {
            waveOut = new double[in.length];
            waveIn = in;
            this.pos = new long[pos.length];
            for (int d = 0; d < pos.length; ++d) {
                this.pos[d] = (long)pos[d];
            }
        }

        public long[] getPos() {
            return pos;
        }

        public double[] getWaveOut() {
            return waveOut;
        }

        @Override
        public WaveletCompositionThread call() {
            final int n = waveIn.length;
            final int q = n << 1;
            int i = 2;
            int v = 0;
            int u = n >> 1;
            int d = 1;
            for (int t = 0; t < n; ++t) {
                waveOut[t] = waveIn[0];
            }
            while (i < q) {
                waveOut[v % n] += ((i % 2) == 0 ? 1 : -1) * waveIn[i - d];
                if ((++v % u) == 0) {
                    ++i;
                    d += ((i % 2) == 1 ? 1 : 0);
                    u >>= ((v % n) == 0 ? 1 : 0);
                }
            }
            return this;
        }
    }

    /**
     * @param executor
     * @param lambda_min
     * @param lambda_max
     * @param ignorePercent
     */
    public WaveletFilter(final ExecutorService executor, final double lambda_min, final double lambda_max,
                         final double ignorePercent) {
        m_executor = executor;
        m_lambda_min = lambda_min;
        m_lambda_max = lambda_max;
        m_ignorePercent = ignorePercent;
    }

    @Override
    public UnaryOperation<K, K> copy() {
        return new WaveletFilter<T, K>(m_executor, m_lambda_min, m_lambda_max, m_ignorePercent);
    }
}
