package org.knime.knip.core.algorithm.convolvers;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;

public class ImgLib2IterativeConvolver<T extends RealType<T>, K extends RealType<K>, O extends RealType<O>> extends
        IterativeConvolver<T, K, O> {

    public ImgLib2IterativeConvolver(final ImgFactory<O> factory,
                                     final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactoryIn,
                                     final OutOfBoundsFactory<O, RandomAccessibleInterval<O>> outOfBoundsFactoryOut) {
        super(factory, outOfBoundsFactoryIn, outOfBoundsFactoryOut);
    }

    @Override
    public BinaryOperation<RandomAccessible<T>, RandomAccessibleInterval<K>[], RandomAccessibleInterval<O>> copy() {
        throw new UnsupportedOperationException("Copy operation in ImgLib2IterativeConvolution not supported");
    }

    @Override
    protected Convolver<T, K, O> createBaseConvolver() {
        return new ImgLib2FourierConvolver<T, K, O>();
    }

    @Override
    protected Convolver<O, K, O> createFollowerConvolver() {
        return new ImgLib2FourierConvolver<O, K, O>();
    }

}
