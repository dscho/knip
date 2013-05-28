package org.knime.knip.core.algorithm.convolvers;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.ops.operation.BinaryObjectFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public abstract class IterativeConvolver<T extends RealType<T>, K extends RealType<K>, O extends RealType<O>>
        implements MultiKernelConvolver<T, K, O> {

    protected ImgFactory<O> m_factory;

    protected OutOfBoundsFactory<T, RandomAccessibleInterval<T>> m_outOfBoundsFactoryIn;

    protected OutOfBoundsFactory<O, RandomAccessibleInterval<O>> m_outOfBoundsFactoryOut;

    private Convolver<T, K, O> m_baseConvolver;

    private Convolver<O, K, O> m_followerConvolver;

    public IterativeConvolver(final ImgFactory<O> factory,
                              final OutOfBoundsFactory<T, RandomAccessibleInterval<T>> outOfBoundsFactoryIn,
                              final OutOfBoundsFactory<O, RandomAccessibleInterval<O>> outOfBoundsFactoryOut) {
        m_baseConvolver = createBaseConvolver();
        m_followerConvolver = createFollowerConvolver();
        m_factory = factory;
        m_outOfBoundsFactoryIn = outOfBoundsFactoryIn;
        m_outOfBoundsFactoryOut = outOfBoundsFactoryOut;
    }

    public RandomAccessibleInterval<O> compute(final RandomAccessible<T> input,
                                               final RandomAccessibleInterval<K>[] kernels,
                                               final RandomAccessibleInterval<O> output) {
        concat(createBufferFactory(output), m_baseConvolver, m_followerConvolver)
                .compute(Views.extend(Views.interval(input, output), m_outOfBoundsFactoryIn), kernels,
                         Views.interval(Views.extend(output, m_outOfBoundsFactoryOut), output));

        return output;
    };

    /*
     * Some helpers
     */
    protected
            BinaryOperation<RandomAccessible<T>, RandomAccessibleInterval<K>[], RandomAccessibleInterval<O>>
            concat(final BinaryObjectFactory<RandomAccessible<T>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>> bufferFac,
                   final Convolver<T, K, O> binaryOp1, final Convolver<O, K, O> binaryOp2) {
        return new BinaryOperation<RandomAccessible<T>, RandomAccessibleInterval<K>[], RandomAccessibleInterval<O>>() {

            @Override
            public RandomAccessibleInterval<O> compute(final RandomAccessible<T> inputA,
                                                       final RandomAccessibleInterval<K>[] inputB,
                                                       final RandomAccessibleInterval<O> output) {

                if (inputB.length == 1) {
                    return binaryOp1.compute(inputA, inputB[0], output);
                }

                RandomAccessibleInterval<O> buffer = bufferFac.instantiate(inputA, null);
                RandomAccessibleInterval<O> tmpOutput;
                RandomAccessibleInterval<O> tmpInput;
                RandomAccessibleInterval<O> tmp;
                if (inputB.length % 2 == 1) {
                    tmpOutput = output;
                    tmpInput = buffer;
                } else {
                    tmpOutput = buffer;
                    tmpInput = output;
                }

                binaryOp1.compute(inputA, inputB[0], tmpOutput);

                for (int i = 1; i < inputB.length; i++) {
                    tmp = tmpInput;
                    tmpInput = tmpOutput;
                    tmpOutput = tmp;
                    binaryOp2.compute(tmpInput, inputB[i], tmpOutput);
                }

                return output;
            }

            @Override
            public BinaryOperation<RandomAccessible<T>, RandomAccessibleInterval<K>[], RandomAccessibleInterval<O>>
                    copy() {
                return concat(bufferFac, (Convolver<T, K, O>)binaryOp1.copy(), (Convolver<O, K, O>)binaryOp2.copy());
            }

        };
    }

    protected BinaryObjectFactory<RandomAccessible<T>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>>
            createBufferFactory(final RandomAccessibleInterval<O> output) {
        return new BinaryObjectFactory<RandomAccessible<T>, RandomAccessibleInterval<K>, RandomAccessibleInterval<O>>() {

            @Override
            public RandomAccessibleInterval<O> instantiate(final RandomAccessible<T> inputA,
                                                           final RandomAccessibleInterval<K> inputB) {
                Img<O> buffer = m_factory.create(output, output.randomAccess().get().createVariable());
                return Views.interval(Views.extend(buffer, m_outOfBoundsFactoryOut), buffer);
            }
        };
    }

    protected abstract Convolver<T, K, O> createBaseConvolver();

    protected abstract Convolver<O, K, O> createFollowerConvolver();

}
