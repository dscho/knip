package org.knime.knip.core.ops.img.algorithms;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.legacy.fft.PhaseCorrelation;
import net.imglib2.algorithm.legacy.fft.PhaseCorrelationPeak;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.subset.ImgView;
import net.imglib2.img.subset.SubsetViews;
import net.imglib2.ops.BinaryOutputOperation;
import net.imglib2.ops.operation.unary.img.CopyImgOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ops.imgplus.ImgPlusCrop;

/**
 * Image projection.
 *
 * @author tcriess, University of Konstanz
 */
public class Aligner<T extends RealType<T>, V extends RealType<V>> implements
                BinaryOutputOperation<Img<T>, Img<V>, Img<T>> {

        public final static int MIN_DIMS = 3;

        public final static int MAX_DIMS = 5;

        // private static final Img<FloatType> smoothkernel = getKernel();

        int[] m_selectedDims;

        int m_alignDim;

        Interval m_iv;

        int[] m_xind;

        int[] m_yind;

        int[] m_gxind;

        int[] m_gyind;

        long[] m_offset;

        long[] m_size;

        public enum SIZEMODES {
                NOTHING, CROP, EXTEND
        };

        SIZEMODES m_sizemode;

        public enum ALIGNMODES {
                FIRST, LAST, PAIRWISE, STEPWISE
        };

        ALIGNMODES m_alignmode;

        private int m_stepsize;

        public Aligner(int[] selectedDims, int alignDim, Interval iv,
                        SIZEMODES sizemode, ALIGNMODES alignmode, int stepsize) {
                m_selectedDims = selectedDims.clone();
                m_alignDim = alignDim;
                m_sizemode = sizemode;
                m_alignmode = alignmode;
                m_iv = iv;
                m_stepsize = stepsize;
        }

        public Aligner(int[] selectedDims, int alignDim, Interval iv) {
                this(selectedDims, alignDim, iv, SIZEMODES.CROP,
                                ALIGNMODES.FIRST, 1);
        }

        /**
         * {@inheritDoc}
         */
        private Img<T> createType(Img<T> src, Img<V> srcFiltered) {
                computeShifts(srcFiltered);
                if (m_sizemode != SIZEMODES.NOTHING) {
                        return createType(src, srcFiltered, m_size);
                } else {
                        long dims[] = new long[srcFiltered.numDimensions()];
                        srcFiltered.dimensions(dims);
                        return createType(src, srcFiltered, dims);
                }
        }

        private void computeShifts(Img<V> src) {
                Img<V> imgPlus = src;
                int[] selectedDims1 = m_selectedDims;
                int[] selectedDims2 = new int[1];
                selectedDims2[0] = m_alignDim;
                int selectedDim2 = selectedDims2[0];

                long tmin = imgPlus.min(selectedDim2);
                long tmax = imgPlus.max(selectedDim2);

                long[] ipmin = new long[imgPlus.numDimensions()];
                long[] ipmax = new long[imgPlus.numDimensions()];
                imgPlus.min(ipmin);
                imgPlus.max(ipmax);
                long[] spmin = new long[imgPlus.numDimensions()];
                long[] spmax = new long[imgPlus.numDimensions()];
                imgPlus.min(spmin);
                imgPlus.max(spmax);

                int[] allDims = new int[imgPlus.numDimensions()];
                for (int i = 0; i < imgPlus.numDimensions(); i++) {
                        allDims[i] = i;
                }
                int[] remainingDims = new int[imgPlus.numDimensions()
                                - selectedDims1.length - selectedDims2.length];
                for (int i = 0; i < selectedDims1.length; i++) {
                        allDims[selectedDims1[i]] = -1;
                }
                for (int i = 0; i < selectedDims2.length; i++) {
                        allDims[selectedDims2[i]] = -1;
                }
                int j = 0;
                for (int i = 0; i < imgPlus.numDimensions(); i++) {
                        if (allDims[i] > -1) {
                                remainingDims[j] = i;
                                j++;
                        }
                }
                // use the imglib2-algorithms

                // collect the subimages along the alignment axis
                @SuppressWarnings("unchecked")
                Img<V>[] sis = new ImgView[(int) (ipmax[selectedDim2]
                                - ipmin[selectedDim2] + 1)];

                Img<V> tmpsis;

                for (long t = tmin; t <= tmax; t++) {
                        ipmin[selectedDim2] = t;
                        ipmax[selectedDim2] = t;
                        for (int i = 0; i < remainingDims.length; i++) {
                                ipmin[remainingDims[i]] = m_iv
                                                .min(remainingDims[i]);
                                ipmax[remainingDims[i]] = m_iv
                                                .min(remainingDims[i]);
                        }
                        Interval i = new FinalInterval(ipmin, ipmax);
                        // sis[(int) (t - tmin)] = new SubImg<T>(imgPlus, i);
                        // sis[(int) (t - tmin)] = new SubImg<T>(smoothedimg,
                        // i);

                        tmpsis = new ImgView<V>(SubsetViews.subsetView(src, i),
                                        imgPlus.factory());
                        long[] tmpmin = new long[tmpsis.numDimensions()];
                        long[] tmpmax = new long[tmpsis.numDimensions()];
                        tmpsis.min(tmpmin);
                        tmpsis.max(tmpmax);
                        i = new FinalInterval(tmpmin, tmpmax);

                        // TODO: Each time there is a new tmpsis result created.
                        // can't this be directlny pushed to res?
                        // ggf. hier copy anlegen
                        sis[(int) (t - tmin)] = new ImgView<V>(
                                        SubsetViews.subsetView(tmpsis, i),
                                        tmpsis.factory());
                }

                // Crop data
                m_offset = new long[imgPlus.numDimensions()];
                m_size = new long[imgPlus.numDimensions()];
                long[] origsize = new long[imgPlus.numDimensions()];

                src.dimensions(m_size);
                src.dimensions(origsize);
                for (int i = 0; i < imgPlus.numDimensions(); i++) {
                        m_offset[i] = 0;
                }
                // end crop data

                m_xind = new int[(int) (spmax[selectedDim2]
                                - spmin[selectedDim2] + 1)];
                m_yind = new int[(int) (spmax[selectedDim2]
                                - spmin[selectedDim2] + 1)];

                m_gxind = new int[(int) (spmax[selectedDim2]
                                - spmin[selectedDim2] + 1)];
                m_gyind = new int[(int) (spmax[selectedDim2]
                                - spmin[selectedDim2] + 1)];

                m_xind[0] = 0;
                m_yind[0] = 0;

                long tstart = tmin + 1;
                long tend = tmax;
                if (m_alignmode == ALIGNMODES.LAST) {
                        tstart = tmin;
                        tend = tmax - 1;
                }

                PhaseCorrelation<V, V> p;
                long ref;
                long[][] cache = new long[(int) tend + 1][(int) (spmax[selectedDim2]
                                - spmin[selectedDim2] + 1)];

                for (long t = tstart; t <= tend; t++) {

                        if (m_alignmode == ALIGNMODES.FIRST) {
                                ref = 0;
                        } else if (m_alignmode == ALIGNMODES.LAST) {
                                ref = tmax;
                        } else if (m_alignmode == ALIGNMODES.PAIRWISE) {
                                ref = t - 1;
                        } else { // if (m_alignmode == ALIGNMODES.STEPWISE)
                                ref = (long) Math.ceil(t / (double) m_stepsize)
                                                * m_stepsize - m_stepsize;
                                if (ref < tstart)
                                        ref = tstart;
                        }

                        p = new PhaseCorrelation<V, V>(sis[(int) ref],
                                        sis[(int) t]);
                        if (p.process()) { // success
                                // List<PhaseCorrelationPeak> peaks =
                                // p.getAllShifts();
                                // int i=0;
                                // long poss[][] = new long[peaks.size()][];
                                // for(PhaseCorrelationPeak peak: peaks) {
                                // poss[i] = peak.getPosition();
                                // i++;
                                // }
                                // long[] pos = poss[0];
                                // double mins = 0.0;
                                // for(int kk=0; kk<poss[0].length; kk++) {
                                // mins += poss[0][kk]*poss[0][kk];
                                // }
                                // for(int k=1; k<poss.length; k++) {
                                // double s = 0.0;
                                // for(int kk=0; kk<poss[k].length; kk++) {
                                // s += poss[k][kk]*poss[k][kk];
                                // }
                                // if(s<mins) {
                                // pos = poss[k];
                                // }
                                // }
                                PhaseCorrelationPeak pe = p.getShift();
                                long[] pos = pe.getPosition();
                                if (m_alignmode == ALIGNMODES.STEPWISE) {
                                        cache[(int) t] = pos;

                                        pos[0] += cache[(int) ref][0];
                                        pos[1] += cache[(int) ref][1];
                                } else if (m_alignmode == ALIGNMODES.PAIRWISE) {
                                        // accumulate the shifts
                                        pos[0] -= m_xind[(int) (t - tmin - 1)];
                                        pos[1] -= m_yind[(int) (t - tmin - 1)];
                                }

                                m_xind[(int) (t - tmin)] = (int) -pos[0];
                                m_yind[(int) (t - tmin)] = (int) -pos[1];

                                if (m_sizemode == SIZEMODES.CROP) {
                                        // crop data
                                        if (pos[0] < 0) {
                                                // move to the left
                                                m_size[selectedDims1[0]] = Math
                                                                .min(origsize[selectedDims1[0]]
                                                                                + pos[0],
                                                                                m_size[selectedDims1[0]]);
                                        } else {
                                                // move to the right
                                                m_offset[selectedDims1[0]] = Math
                                                                .max(m_offset[selectedDims1[0]],
                                                                                pos[0]);
                                        }
                                        if (pos[1] < 0) {
                                                // move up
                                                m_size[selectedDims1[1]] = Math
                                                                .min(origsize[selectedDims1[1]]
                                                                                + pos[1],
                                                                                m_size[selectedDims1[1]]);
                                        } else {
                                                // move down
                                                m_offset[selectedDims1[1]] = Math
                                                                .max(m_offset[selectedDims1[1]],
                                                                                pos[1]);
                                        }
                                        // end crop data
                                } else if (m_sizemode == SIZEMODES.EXTEND) {
                                        // extend data
                                        if (pos[0] < 0) {
                                                // move to the left
                                                m_size[selectedDims1[0]] = Math
                                                                .max(origsize[selectedDims1[0]]
                                                                                + pos[0],
                                                                                m_size[selectedDims1[0]]);
                                                m_offset[selectedDims1[0]] = Math
                                                                .min(m_offset[selectedDims1[0]],
                                                                                pos[0]);
                                        } else {
                                                // move to the right
                                                m_size[selectedDims1[0]] = Math
                                                                .max(origsize[selectedDims1[0]]
                                                                                + pos[0],
                                                                                m_size[selectedDims1[0]]);
                                                m_offset[selectedDims1[0]] = Math
                                                                .min(m_offset[selectedDims1[0]],
                                                                                pos[0]);
                                        }
                                        if (pos[1] < 0) {
                                                // move up
                                                m_size[selectedDims1[1]] = Math
                                                                .max(origsize[selectedDims1[1]]
                                                                                + pos[1],
                                                                                m_size[selectedDims1[1]]);
                                                m_offset[selectedDims1[1]] = Math
                                                                .min(m_offset[selectedDims1[1]],
                                                                                pos[1]);
                                        } else {
                                                // move down
                                                m_size[selectedDims1[1]] = Math
                                                                .max(origsize[selectedDims1[1]]
                                                                                + pos[1],
                                                                                m_size[selectedDims1[1]]);
                                                m_offset[selectedDims1[1]] = Math
                                                                .min(m_offset[selectedDims1[1]],
                                                                                pos[1]);
                                        }
                                        // end extend data
                                }
                        }
                }

                for (long t = tmin; t <= tmax; t++) {
                        m_gxind[(int) (t - tmin)] = m_xind[(int) (t - tmin)];
                        m_gyind[(int) (t - tmin)] = m_yind[(int) (t - tmin)];
                }

                if (m_sizemode == SIZEMODES.CROP) {
                        // crop data
                        // adjust size
                        for (int i = 0; i < imgPlus.numDimensions(); i++) {
                                m_size[i] -= m_offset[i];
                        }
                        // end crop data
                } else if (m_sizemode == SIZEMODES.EXTEND) {
                        for (int i = 0; i < imgPlus.numDimensions(); i++) {
                                m_size[i] -= m_offset[i];
                        }
                        // adjust the shift for extension (also for the first
                        // image)
                        for (long t = tmin; t <= tmax; t++) {
                                m_xind[(int) (t - tmin)] = -(int) m_offset[selectedDims1[0]]
                                                - m_xind[(int) (t - tmin)];
                                m_yind[(int) (t - tmin)] = -(int) m_offset[selectedDims1[1]]
                                                - m_yind[(int) (t - tmin)];
                        }
                }

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<T> compute(Img<T> srcIn, Img<V> srcFiltered, Img<T> res) {
                Img<T> imgPlus = srcIn;
                int[] selectedDims1 = m_selectedDims;
                int[] selectedDims2 = new int[1];
                selectedDims2[0] = m_alignDim;

                Img<T> res2;
                if (m_sizemode == SIZEMODES.CROP
                                || m_sizemode == SIZEMODES.NOTHING) {
                        res2 = srcIn.copy();
                } else {
                        long[] pos = new long[srcIn.numDimensions()];
                        res2 = res.copy();
                        Cursor<T> c = srcIn.localizingCursor();
                        RandomAccess<T> ra = res2.randomAccess();
                        while (c.hasNext()) {
                                c.fwd();
                                c.localize(pos);
                                ra.setPosition(pos);
                                double v = c.get().getRealDouble();
                                ra.get().setReal(v);
                        }
                }

                // this is the dimension along which the alignment is done
                int selectedDim2 = selectedDims2[0];

                long tmin = imgPlus.min(selectedDim2);
                long tmax = imgPlus.max(selectedDim2);

                long[] ipmin = new long[imgPlus.numDimensions()];
                long[] ipmax = new long[imgPlus.numDimensions()];
                res2.min(ipmin);
                res2.max(ipmax);
                long[] spmin = new long[imgPlus.numDimensions()];
                long[] spmax = new long[imgPlus.numDimensions()];
                res2.min(spmin);
                res2.max(spmax);

                int[] allDims = new int[imgPlus.numDimensions()];
                for (int i = 0; i < imgPlus.numDimensions(); i++) {
                        allDims[i] = i;
                }
                int[] remainingDims = new int[imgPlus.numDimensions()
                                - selectedDims1.length - selectedDims2.length];
                for (int i = 0; i < selectedDims1.length; i++) {
                        allDims[selectedDims1[i]] = -1;
                }
                for (int i = 0; i < selectedDims2.length; i++) {
                        allDims[selectedDims2[i]] = -1;
                }
                int j = 0;
                for (int i = 0; i < imgPlus.numDimensions(); i++) {
                        if (allDims[i] > -1) {
                                remainingDims[j] = i;
                                j++;
                        }
                }

                long tstart = tmin + 1;
                if (m_sizemode == SIZEMODES.EXTEND
                                || m_alignmode == ALIGNMODES.LAST) {
                        // also adjust the first image if extending
                        tstart = tmin;
                }
                long tend = tmax;
                if (m_alignmode == ALIGNMODES.LAST
                                && (!(m_sizemode == SIZEMODES.EXTEND))) {
                        tend = tmax - 1;
                }
                // TODO: maybe something else here
                // set the remaining dimensions to their minimum value.
                for (int i = 0; i < remainingDims.length; i++) {
                        ipmin[remainingDims[i]] = spmin[remainingDims[i]];
                        ipmax[remainingDims[i]] = spmin[remainingDims[i]];
                }
                if (remainingDims.length > 0) {
                        alignRemainingDims((int) tstart, (int) tend, res2,
                                        selectedDims1, selectedDim2,
                                        remainingDims, (int) tmin, (int) tmax,
                                        ipmin, ipmax, spmin, spmax);
                        // for (int rd = 0; rd < remainingDims.length; rd++) {
                        // for (int rdplane = (int) spmin[remainingDims[rd]];
                        // rdplane <=
                        // spmax[remainingDims[rd]]; rdplane++) {
                        // ipmin[remainingDims[rd]] = rdplane;
                        // ipmax[remainingDims[rd]] = rdplane;
                        //
                        // alignPlane((int) tstart, (int) tend, res2,
                        // selectedDims1,
                        // selectedDim2, remainingDims, (int) tmin,
                        // (int) tmax, ipmin, ipmax, spmin, spmax);
                        // }
                        // }
                } else {

                        alignPlane((int) tstart, (int) tend, res2,
                                        selectedDims1, selectedDim2,
                                        remainingDims, (int) tmin, (int) tmax,
                                        ipmin, ipmax, spmin, spmax);
                }

                if (m_sizemode == SIZEMODES.CROP) {
                        // Crop the resulting image...
                        ImgPlus<T> iplusres = new ImgPlus<T>(res);
                        ImgPlus<T> iplus = new ImgPlus<T>(res2);
                        new ImgPlusCrop<T>(m_offset, m_size).compute(iplus,
                                        iplusres);
                } else if (m_sizemode == SIZEMODES.NOTHING) {
                        new CopyImgOperation<T>().compute(res2, res);
                } else { // extend
                        new CopyImgOperation<T>().compute(res2, res);
                }
                return res;
        }

        void alignRemainingDims(int tstart, int tend, Img<T> res2,
                        int[] selectedDims1, int selectedDim2,
                        int[] remainingDims, int tmin, int tmax, long[] ipmin,
                        long[] ipmax, long[] spmin, long[] spmax) {
                if (remainingDims.length == 1) {
                        for (int rdplane = (int) spmin[remainingDims[0]]; rdplane <= spmax[remainingDims[0]]; rdplane++) {
                                ipmin[remainingDims[0]] = rdplane;
                                ipmax[remainingDims[0]] = rdplane;
                                alignPlane(tstart, tend, res2, selectedDims1,
                                                selectedDim2, remainingDims,
                                                tmin, tmax, ipmin, ipmax,
                                                spmin, spmax);
                        }
                } else {
                        for (int rdplane = (int) spmin[remainingDims[0]]; rdplane <= spmax[remainingDims[0]]; rdplane++) {
                                ipmin[remainingDims[0]] = rdplane;
                                ipmax[remainingDims[0]] = rdplane;
                                alignRemainingDims(
                                                tstart,
                                                tend,
                                                res2,
                                                selectedDims1,
                                                selectedDim2,
                                                Arrays.copyOfRange(
                                                                remainingDims,
                                                                1,
                                                                remainingDims.length),
                                                tmin, tmax, ipmin, ipmax,
                                                spmin, spmax);
                        }
                }
        }

        void alignPlane(int tstart, int tend, Img<T> res2, int[] selectedDims1,
                        int selectedDim2, int[] remainingDims, int tmin,
                        int tmax, long[] ipmin, long[] ipmax, long[] spmin,
                        long[] spmax) {

                // now: align the planes
                for (long t = tstart; t <= tend; t++) {
                        ipmin[selectedDim2] = t;
                        ipmax[selectedDim2] = t;

                        Interval i = new FinalInterval(ipmin, ipmax);
                        ImgView<T> si = new ImgView<T>(SubsetViews.subsetView(
                                        res2, i), res2.factory());
                        RandomAccess<T> c = si.randomAccess();
                        int[] pos = new int[2];
                        pos[0] = (int) spmin[selectedDims1[0]];
                        pos[1] = (int) spmin[selectedDims1[1]];
                        c.setPosition(pos);
                        if (m_sizemode == SIZEMODES.EXTEND) {
                                for (int x = (int) spmax[selectedDims1[0]]; x >= (int) spmin[selectedDims1[0]]; x--) {
                                        for (int y = (int) spmax[selectedDims1[1]]; y >= (int) spmin[selectedDims1[1]]; y--) {
                                                if (x
                                                                - m_xind[(int) (t - tmin)] >= spmin[selectedDims1[0]]
                                                                && x
                                                                                - m_xind[(int) (t - tmin)] <= spmax[selectedDims1[0]]
                                                                && y
                                                                                - m_yind[(int) (t - tmin)] >= spmin[selectedDims1[1]]
                                                                && y
                                                                                - m_yind[(int) (t - tmin)] <= spmax[selectedDims1[1]]) {
                                                        c.setPosition(x
                                                                        - m_xind[(int) (t - tmin)],
                                                                        0);
                                                        c.setPosition(y
                                                                        - m_yind[(int) (t - tmin)],
                                                                        1);
                                                        double v = c.get()
                                                                        .getRealDouble();
                                                        c.setPosition(x, 0);
                                                        c.setPosition(y, 1);
                                                        c.get().setReal(v);
                                                } else {
                                                        c.setPosition(x, 0);
                                                        c.setPosition(y, 1);
                                                        c.get().setReal(0);
                                                }
                                        }
                                }
                        } else {
                                if (m_gxind[(int) (t - tmin)] >= 0
                                                && m_gyind[(int) (t - tmin)] >= 0) { // move
                                        // left
                                        // and
                                        // up
                                        for (int x = (int) spmin[selectedDims1[0]]; x <= (int) spmax[selectedDims1[0]]; x++) {
                                                for (int y = (int) spmin[selectedDims1[1]]; y <= (int) spmax[selectedDims1[1]]; y++) {
                                                        if (x
                                                                        + m_xind[(int) (t - tmin)] <= spmax[selectedDims1[0]]
                                                                        && y
                                                                                        + m_yind[(int) (t - tmin)] <= spmax[selectedDims1[1]]) {
                                                                c.setPosition(x
                                                                                + m_xind[(int) (t - tmin)],
                                                                                0);
                                                                c.setPosition(y
                                                                                + m_yind[(int) (t - tmin)],
                                                                                1);
                                                                double v = c.get()
                                                                                .getRealDouble();
                                                                c.setPosition(x,
                                                                                0);
                                                                c.setPosition(y,
                                                                                1);
                                                                c.get()
                                                                                .setReal(v);
                                                        } else {
                                                                c.setPosition(x,
                                                                                0);
                                                                c.setPosition(y,
                                                                                1);
                                                                c.get()
                                                                                .setReal(0);
                                                        }
                                                }
                                        }
                                } else {
                                        if (m_gxind[(int) (t - tmin)] < 0
                                                        && m_gyind[(int) (t - tmin)] >= 0) { // move
                                                // right
                                                // and
                                                // up
                                                for (int x = (int) spmax[selectedDims1[0]]; x >= (int) spmin[selectedDims1[0]]; x--) {
                                                        for (int y = (int) spmin[selectedDims1[1]]; y <= (int) spmax[selectedDims1[1]]; y++) {
                                                                if (x
                                                                                + m_xind[(int) (t - tmin)] >= spmin[selectedDims1[0]]
                                                                                && y
                                                                                                + m_yind[(int) (t - tmin)] <= spmax[selectedDims1[1]]) {
                                                                        c.setPosition(x
                                                                                        + m_xind[(int) (t - tmin)],
                                                                                        0);
                                                                        c.setPosition(y
                                                                                        + m_yind[(int) (t - tmin)],
                                                                                        1);
                                                                        double v = c.get()
                                                                                        .getRealDouble();
                                                                        c.setPosition(x,
                                                                                        0);
                                                                        c.setPosition(y,
                                                                                        1);
                                                                        c.get()
                                                                                        .setReal(v);
                                                                } else {
                                                                        c.setPosition(x,
                                                                                        0);
                                                                        c.setPosition(y,
                                                                                        1);
                                                                        c.get()
                                                                                        .setReal(0);
                                                                }
                                                        }
                                                }
                                        } else {
                                                if (m_gxind[(int) (t - tmin)] >= 0
                                                                && m_gyind[(int) (t - tmin)] < 0) { // move
                                                        // left
                                                        // and
                                                        // down
                                                        for (int x = (int) spmin[selectedDims1[0]]; x <= (int) spmax[selectedDims1[0]]; x++) {
                                                                for (int y = (int) spmax[selectedDims1[1]]; y >= (int) spmin[selectedDims1[1]]; y--) {
                                                                        if (x
                                                                                        + m_xind[(int) (t - tmin)] <= spmax[selectedDims1[0]]
                                                                                        && y
                                                                                                        + m_yind[(int) (t - tmin)] >= spmin[selectedDims1[1]]) {
                                                                                c.setPosition(x
                                                                                                + m_xind[(int) (t - tmin)],
                                                                                                0);
                                                                                c.setPosition(y
                                                                                                + m_yind[(int) (t - tmin)],
                                                                                                1);
                                                                                double v = c.get()
                                                                                                .getRealDouble();
                                                                                c.setPosition(x,
                                                                                                0);
                                                                                c.setPosition(y,
                                                                                                1);
                                                                                c.get()
                                                                                                .setReal(v);
                                                                        } else {
                                                                                c.setPosition(x,
                                                                                                0);
                                                                                c.setPosition(y,
                                                                                                1);
                                                                                c.get()
                                                                                                .setReal(0);
                                                                        }
                                                                }
                                                        }
                                                } else { // xind[(int)(t-tmin)]<0
                                                         // &&
                                                         // yind[(int)(t-tmin)]<0
                                                         // // move right and
                                                         // down
                                                        for (int x = (int) spmax[selectedDims1[0]]; x >= (int) spmin[selectedDims1[0]]; x--) {
                                                                for (int y = (int) spmax[selectedDims1[1]]; y >= (int) spmin[selectedDims1[1]]; y--) {
                                                                        if (x
                                                                                        + m_xind[(int) (t - tmin)] >= spmin[selectedDims1[0]]
                                                                                        && y
                                                                                                        + m_yind[(int) (t - tmin)] >= spmin[selectedDims1[1]]) {
                                                                                c.setPosition(x
                                                                                                + m_xind[(int) (t - tmin)],
                                                                                                0);
                                                                                c.setPosition(y
                                                                                                + m_yind[(int) (t - tmin)],
                                                                                                1);
                                                                                double v = c.get()
                                                                                                .getRealDouble();
                                                                                c.setPosition(x,
                                                                                                0);
                                                                                c.setPosition(y,
                                                                                                1);
                                                                                c.get()
                                                                                                .setReal(v);
                                                                        } else {
                                                                                c.setPosition(x,
                                                                                                0);
                                                                                c.setPosition(y,
                                                                                                1);
                                                                                c.get()
                                                                                                .setReal(0);
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        /**
         * @param dims
         */
        public Img<T> createType(Img<T> img, Img<V> srcFiltered, long[] dims) {
                ImgFactory<T> fac = img.factory();
                return fac.create(dims, img.firstElement().createVariable());
        }

        @Override
        public BinaryOutputOperation<Img<T>, Img<V>, Img<T>> copy() {
                return new Aligner<T, V>(m_selectedDims, m_alignDim, m_iv,
                                m_sizemode, m_alignmode, m_stepsize);
        }

        @Override
        public Img<T> createEmptyOutput(Img<T> in1, Img<V> in2) {
                return createType(in1, in2);
        }

        @Override
        public Img<T> compute(Img<T> in1, Img<V> in2) {
                return compute(in1, in2, createEmptyOutput(in1, in2));
        }

}
