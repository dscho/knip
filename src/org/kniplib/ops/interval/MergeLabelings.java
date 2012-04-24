package org.kniplib.ops.interval;

import java.util.Arrays;
import java.util.HashSet;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;

import org.kniplib.tools.comparators.IntervalComperator;

/**
 * Operation to merge several intervals and their content to a resulting Img. If
 * intervals intersects, the interval with a higher offset will override the
 * interval with lower offset. Gaps between intervals will be filled with
 * emptyType.
 * 
 * This operation is mostly designed to compute rectangular subsets of images
 * and merge them back again.
 * 
 * 
 * @author dietzc, hornm University of Konstanz
 * 
 */

@SuppressWarnings("rawtypes")
public final class MergeLabelings<L extends Comparable<L>>
                implements
                UnaryOutputOperation<Labeling<L>[], NativeImgLabeling<L, ? extends IntegerType<?>>> {

        private IntegerType<?> m_resType;

        public MergeLabelings(IntegerType<?> resType) {
                m_resType = resType;
        }

        @Override
        public final NativeImgLabeling<L, ? extends IntegerType<?>> createEmptyOutput(
                        final Labeling<L>[] src) {
                int numMaxDims = 0;

                for (Labeling<L> interval : src) {
                        numMaxDims = Math.max(interval.numDimensions(),
                                        numMaxDims);
                }
                @SuppressWarnings("unchecked")
                java.util.Set<Long>[] setDims = new HashSet[numMaxDims];

                for (int s = 0; s < setDims.length; s++) {
                        setDims[s] = new HashSet<Long>();
                }

                long[] resDims = new long[numMaxDims];

                for (Labeling<L> interval : src) {
                        for (int d = 0; d < interval.numDimensions(); d++) {
                                for (long i = interval.min(d); i <= interval
                                                .max(d); i++)
                                        setDims[d].add(i);
                        }
                }

                for (int d = 0; d < resDims.length; d++) {
                        resDims[d] = setDims[d].size();
                }

                @SuppressWarnings("unchecked")
                NativeImgLabeling<L, ? extends IntegerType<?>> res = new NativeImgLabeling(
                                new ArrayImgFactory().create(resDims,
                                                (NativeType) m_resType));
                return res;
        }

        @Override
        public final NativeImgLabeling<L, ? extends IntegerType<?>> compute(
                        final Labeling<L>[] intervals,
                        NativeImgLabeling<L, ? extends IntegerType<?>> res) {

                // m_fill.manipulate(res, m_emptyType);

                RandomAccess<LabelingType<L>> randomAccess = res.randomAccess();
                Arrays.sort(intervals, new IntervalComperator());

                long[] offset = new long[res.numDimensions()];
                long[] intervalWidth = new long[res.numDimensions()];

                intervals[0].min(offset);
                intervals[0].dimensions(intervalWidth);

                writeInterval(randomAccess, intervals[0], offset);

                for (int i = 1; i < intervals.length; i++) {

                        for (int d = 0; d < intervals[i].numDimensions(); d++) {

                                if (intervals[i].min(d) != intervals[i - 1]
                                                .min(d)) {
                                        for (int innerD = d + 1; innerD < intervals[i]
                                                        .numDimensions(); innerD++) {
                                                intervalWidth[innerD] = 0;
                                        }

                                        offset[d] = intervals[i].min(d)
                                                        - intervalWidth[d];
                                        intervalWidth[d] += intervals[i]
                                                        .dimension(d);
                                }

                        }

                        writeInterval(randomAccess, intervals[i], offset);
                }

                return res;
        }

        /*
         * Writes an interval into the result to resulting img with respect to
         * the offset
         */
        private <T extends Type<T>> void writeInterval(
                        RandomAccess<T> resAccess,
                        IterableInterval<T> interval, long[] offset) {
                Cursor<T> localizingCursor = interval.localizingCursor();

                while (localizingCursor.hasNext()) {
                        localizingCursor.fwd();
                        for (int d = 0; d < interval.numDimensions(); d++) {
                                resAccess.setPosition(
                                                localizingCursor.getIntPosition(d)
                                                                - offset[d], d);
                        }
                        resAccess.get().set(localizingCursor.get());

                }
        }

        @Override
        public MergeLabelings<L> copy() {
                return new MergeLabelings<L>(m_resType.copy());
        }

        @Override
        public NativeImgLabeling<L, ? extends IntegerType<?>> compute(
                        Labeling<L>[] op) {
                return compute(op, createEmptyOutput(op));
        }
}
