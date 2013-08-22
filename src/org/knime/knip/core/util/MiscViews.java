package org.knime.knip.core.util;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.constant.ConstantCursor;
import net.imglib2.labeling.LabelingFactory;
import net.imglib2.labeling.LabelingType;
import net.imglib2.meta.DefaultTypedAxis;
import net.imglib2.meta.DefaultTypedSpace;
import net.imglib2.meta.ImgPlus;
import net.imglib2.meta.MetadataUtil;
import net.imglib2.meta.TypedAxis;
import net.imglib2.meta.TypedSpace;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.subset.views.ImgPlusView;
import net.imglib2.ops.operation.subset.views.ImgView;
import net.imglib2.ops.operation.subset.views.LabelingView;
import net.imglib2.sampler.special.ConstantRandomAccessible;
import net.imglib2.type.Type;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;

public class MiscViews {

    public synchronized static <T extends Type<T>> IterableRandomAccessibleInterval<T>
            constant(final T constant, final Interval interval) {

        final long[] dimensions = new long[interval.numDimensions()];
        interval.dimensions(dimensions);

        return new IterableRandomAccessibleInterval<T>((Views.interval(new ConstantRandomAccessible<T>(constant,
                interval.numDimensions()), interval))) {
            @Override
            public Cursor<T> cursor() {
                return new ConstantCursor<T>(constant, interval.numDimensions(), dimensions,
                        Intervals.numElements(interval));
            }

            @Override
            public Cursor<T> localizingCursor() {
                return cursor();
            }
        };

    }


    /**
     * removes dimensions of size 1 if any.
     * @param ret
     * @return
     */
    public static <T extends Type<T>> ImgPlusView<T> cleanImgPlus(final ImgPlus<T> ret) {
        ImgPlusView<T> imgPlusView = new ImgPlusView<T>(SubsetOperations.subsetview(ret.getImg(), ret.getImg()),  ret.factory());
        MetadataUtil.copyAndCleanTypedSpace(ret, ret, imgPlusView);
        return imgPlusView;
    }

    public static <T extends Type<T>> ImgView<T> imgView(final RandomAccessibleInterval<T> randAccessible,
                                                         final ImgFactory<T> fac) {
        if (randAccessible instanceof ImgView) {
            return (ImgView<T>)randAccessible;
        } else {
            return new ImgView<T>(randAccessible, fac);
        }
    }

    public static <L extends Comparable<L>> LabelingView<L>
            labelingView(final RandomAccessibleInterval<LabelingType<L>> randAccessible, final LabelingFactory<L> fac) {
        if (randAccessible instanceof LabelingView) {
            return (LabelingView<L>)randAccessible;
        } else {
            return new LabelingView<L>(randAccessible, fac);
        }
    }

    /**
     * {@link RandomAccessibleInterval} with same sice as target is returned
     *
     * @param src {@link RandomAccessibleInterval} to be adjusted
     * @param target {@link Interval} describing the resulting sizes
     * @return Adjusted {@link RandomAccessibleInterval}
     */
    public static <T> RandomAccessibleInterval<T>
            synchronizeDimensionality(RandomAccessibleInterval<T> src, final TypedSpace<? extends TypedAxis> srcSpace,
                                      final Interval target, final TypedSpace<? extends TypedAxis> targetSpace) {

        // must hold, if not: most likely an implementation error
        assert ((srcSpace.numDimensions() == src.numDimensions()) && (target.numDimensions() == targetSpace
                .numDimensions()));

        // Check direction of conversion
        if (Intervals.equals(src, target) && spaceEquals(srcSpace, targetSpace)) {
            return src;
        }

        // Extend
        src = Views.interval(Views.extendBorder(src), src);

        // Init result vars
        RandomAccessibleInterval<T> res = src;
        final TypedSpace<TypedAxis> resSpace = new DefaultTypedSpace(target.numDimensions());

        // 1. Step remove axis from source which can't be found in
        // target
        final TypedAxis[] dispensable = getDeltaAxisTypes(targetSpace, srcSpace);
        for (int d = dispensable.length - 1; d >= 0; --d) {
            final int idx = srcSpace.dimensionIndex(dispensable[d].type());
            res = Views.hyperSlice(res, idx, 0);
        }

        int i = 0;
        outer: for (int d = 0; d < srcSpace.numDimensions(); d++) {
            for (final TypedAxis typedAxis : dispensable) {
                if (d == srcSpace.dimensionIndex(typedAxis.type())) {
                    continue outer;
                }
            }

            resSpace.setAxis(srcSpace.axis(d), i++);
        }

        // 2. Add Axis which are available in target but not in source
        final TypedAxis[] missing = getDeltaAxisTypes(srcSpace, targetSpace);

        // Dimensions are added and resSpace is synchronized with res
        i = srcSpace.numDimensions() - dispensable.length;
        for (final TypedAxis typedAxis : missing) {
            final int idx = targetSpace.dimensionIndex(typedAxis.type());
            res = Views.addDimension(res, target.min(idx), target.max(idx));
            resSpace.setAxis(new DefaultTypedAxis(typedAxis.type()), i++);
        }

        // res should have the same size, but with different metadata
        assert (res.numDimensions() == targetSpace.numDimensions());

        // 3. Permutate axis if necessary
        RandomAccessible<T> resRndAccessible = res;
        for (int d = 0; d < res.numDimensions(); d++) {
            final int srcIdx = resSpace.dimensionIndex(targetSpace.axis(d).type());

            if (srcIdx != d) {
                resRndAccessible = Views.permute(resRndAccessible, srcIdx, d);

                // also permutate calibrated space
                final TypedAxis tmp = resSpace.axis(d);
                resSpace.setAxis(targetSpace.axis(d), d);
                resSpace.setAxis(tmp, srcIdx);
            }
        }

        return Views.interval(res, target);
    }

    /**
     * {@link RandomAccessibleInterval} with same sice as target is returned
     *
     * @param src {@link RandomAccessibleInterval} to be adjusted
     * @param target {@link Interval} describing the resulting sizes
     * @return Adjusted {@link RandomAccessibleInterval}
     */
    public static <T> RandomAccessibleInterval<T> synchronizeDimensionality(final RandomAccessibleInterval<T> src,
                                                                            final Interval target) {
        IntervalView<T> res = Views.interval(Views.extendBorder(src), src);

        // Check direction of conversion
        if (Intervals.equals(src, target)) {
            return res;
        }

        // adjust dimensions
        if (res.numDimensions() < target.numDimensions()) {
            for (int d = res.numDimensions(); d < target.numDimensions(); d++) {
                res = Views.addDimension(res, target.min(d), target.max(d));
            }
        } else {
            for (int d = res.numDimensions() - 1; d >= target.numDimensions(); --d) {
                res = Views.hyperSlice(res, d, 0);
            }
        }

        final long[] resDims = new long[res.numDimensions()];
        res.dimensions(resDims);

        return Views.interval(res, target);

    }

    private static boolean spaceEquals(final TypedSpace<? extends TypedAxis> srcSpace,
                                       final TypedSpace<? extends TypedAxis> targetSpace) {

        if (srcSpace.numDimensions() != targetSpace.numDimensions()) {
            return false;
        }

        for (int d = 0; d < srcSpace.numDimensions(); d++) {
            if (!srcSpace.axis(d).equals(targetSpace.axis(d))) {
                return false;
            }
        }
        return true;
    }

    /*
     * Calculate the delta axis which are missing in the smaller space. >
     * From the smallest index of axistype to the biggest
     */
    private synchronized static TypedAxis[] getDeltaAxisTypes(final TypedSpace<? extends TypedAxis> sourceSpace,
                                                              final TypedSpace<? extends TypedAxis> targetSpace) {

        final List<TypedAxis> delta = new ArrayList<TypedAxis>();
        for (int d = 0; d < targetSpace.numDimensions(); d++) {
            final TypedAxis axis = targetSpace.axis(d);
            if (sourceSpace.dimensionIndex(axis.type()) == -1) {
                delta.add(axis);
            }
        }
        return delta.toArray(new TypedAxis[delta.size()]);
    }
}
