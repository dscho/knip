package org.knime.knip.core.algorithm.convolvers;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class DirectIterativeConvolver<T extends RealType<T>, K extends RealType<K> & NativeType<K>, O extends RealType<O> & NativeType<O>>
        extends IterativeConvolver<T, K, O> {

    private ImgFactory<O> m_factory;

    private OutOfBoundsFactory<T, RandomAccessibleInterval<T>> m_outOfBoundsFactoryIn;

    private OutOfBoundsFactory<O, RandomAccessibleInterval<O>> m_outOfBoundsFactoryOut;

    public DirectIterativeConvolver(final ImgFactory<O> factory,
                                    final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactoryIn,
                                    final OutOfBoundsFactory<O, RandomAccessibleInterval<O>> outOfBoundsFactoryOut) {
        super(factory, outOfBoundsFactoryIn, outOfBoundsFactoryOut);
    }

    public DirectIterativeConvolver() {
        super(new ArrayImgFactory<O>(), new OutOfBoundsMirrorFactory<T, RandomAccessibleInterval<T>>(Boundary.SINGLE),
                new OutOfBoundsMirrorFactory<O, RandomAccessibleInterval<O>>(Boundary.SINGLE));
    }

    @Override
    public DirectIterativeConvolver<T, K, O> copy() {
        return new DirectIterativeConvolver<T, K, O>(m_factory, m_outOfBoundsFactoryIn, m_outOfBoundsFactoryOut);
    }

    @Override
    protected Convolver<T, K, O> createBaseConvolver() {
        return new DirectConvolver<T, K, O>();
    }

    @Override
    protected Convolver<O, K, O> createFollowerConvolver() {
        return new DirectConvolver<O, K, O>();
    }
}
