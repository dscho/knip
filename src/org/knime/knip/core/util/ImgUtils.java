package org.knime.knip.core.util;

import net.imglib2.Interval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ImgUtils {

    public synchronized static <T extends RealType<T>> Img<T> createEmptyImg(final Img<T> in) {
        return in.factory().create(in, in.firstElement().createVariable());
    }

    public synchronized static <T extends RealType<T>> Img<T> createEmptyCopy(final Img<T> in, final long[] newDims) {
        return in.factory().create(newDims, in.firstElement().createVariable());
    }

    public synchronized static <T extends RealType<T>> Img<T> createEmptyCopy(final ImgFactory<T> fac,
                                                                              final Interval in, final T type) {
        return fac.create(in, type);
    }

    public synchronized static <T extends RealType<T>, O extends RealType<O>> Img<O> createEmptyCopy(final Img<T> in,
                                                                                                     final O type) {
        try {
            return in.factory().imgFactory(type).create(in, type);
        } catch (final IncompatibleTypeException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized static <T extends RealType<T>, O extends NativeType<O>> Img<O>
            createEmptyCopy(final Img<T> in, final ImgFactory<O> fac, final O type) {
        return fac.create(in, type);
    }

    public synchronized static <T extends RealType<T>> Img<T> createEmptyCopy(final Img<T> in) {
        return in.factory().create(in, in.firstElement().createVariable());
    }

    public synchronized static <O extends NativeType<O>> Img<O> createEmptyCopy(final long[] dims,
                                                                                final ImgFactory<O> fac, final O type) {
        return fac.create(dims, type);
    }

    public synchronized static <L extends Comparable<L>> Labeling<L> createEmptyCopy(final Labeling<L> labeling) {
        return labeling.<L> factory().create(labeling);
    }
}
