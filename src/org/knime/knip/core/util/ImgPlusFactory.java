package org.knime.knip.core.util;

import net.imglib2.IterableInterval;
import net.imglib2.img.ImgPlus;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.type.numeric.RealType;

public class ImgPlusFactory<T extends RealType<T>, V extends RealType<V>> implements
        UnaryObjectFactory<ImgPlus<T>, ImgPlus<V>> {

    private final V m_outType;

    public ImgPlusFactory(final V outType) {
        this.m_outType = outType;
    }

    @Override
    public ImgPlus<V> instantiate(final ImgPlus<T> a) {
        return new ImgPlus<V>(ImgUtils.createEmptyCopy(a, m_outType), a);
    }

    public static <T extends RealType<T>, V extends RealType<V>> ImgPlusFactory<T, V> get(final V outType) {
        return new ImgPlusFactory<T, V>(outType);
    }

    public static <T extends RealType<T>, V extends RealType<V>> ImgPlusFactory<T, V>
            get(final IterableInterval<V> interval) {
        return new ImgPlusFactory<T, V>(interval.firstElement());
    }

}
