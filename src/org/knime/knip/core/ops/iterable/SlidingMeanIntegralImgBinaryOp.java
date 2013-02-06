package org.knime.knip.core.ops.iterable;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.Shape;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.knime.knip.core.ops.integralimage.IntegralImg2D;

public class SlidingMeanIntegralImgBinaryOp<T extends RealType<T>, V extends RealType<V> & NativeType<V>, IN extends RandomAccessibleInterval<T>, OUT extends IterableInterval<V>>
                extends SlidingShapeOp<T, V, IN, OUT> {

        private final IntegralImg2D<T, IntType> m_iiOp;
        private final BinaryOperation<DoubleType, T, V> m_binaryOp;
        private final OutOfBoundsFactory<IntType, RandomAccessibleInterval<IntType>> m_outOfBoundsII;

        public SlidingMeanIntegralImgBinaryOp(
                        BinaryOperation<DoubleType, T, V> binaryOp,
                        Shape shape,
                        OutOfBoundsFactory<T, IN> outOfBounds,
                        OutOfBoundsFactory<IntType, RandomAccessibleInterval<IntType>> outOfBoundsII) {
                super(shape, outOfBounds);
                m_binaryOp = binaryOp;
                m_outOfBoundsII = outOfBoundsII;
                m_iiOp = new IntegralImg2D<T, IntType>();
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingMeanIntegralImgBinaryOp<T, V, IN, OUT>(
                                m_binaryOp.copy(), shape, outofbounds,
                                m_outOfBoundsII);
        }

        @Override
        protected OUT compute(IterableInterval<Neighborhood<T>> neighborhoods,
                        IN input, OUT output) {

                Cursor<T> inCursor = Views.iterable(
                                SubsetOperations.subsetview(input, input))
                                .cursor();
                Cursor<V> outCursor = output.cursor();

                RandomAccessibleInterval<IntType> ii = Operations
                                .compute(Operations
                                                .wrap(m_iiOp,
                                                                new UnaryObjectFactory<RandomAccessibleInterval<T>, RandomAccessibleInterval<IntType>>() {

                                                                        @Override
                                                                        public RandomAccessibleInterval<IntType> instantiate(
                                                                                        RandomAccessibleInterval<T> a) {
                                                                                return new ArrayImgFactory<IntType>()
                                                                                                .create(a,
                                                                                                                new IntType());
                                                                        }
                                                                }), input);

                OutOfBounds<IntType> randomAccess = Views.extend(ii,
                                m_outOfBoundsII).randomAccess();

                DoubleType mean = new DoubleType();
                long[] tmpStore = new long[2];

                for (final Neighborhood<T> neighborhood : neighborhoods) {
                        inCursor.fwd();
                        outCursor.fwd();

                        int sizeX = (int) neighborhood.dimension(0);
                        int sizeY = (int) neighborhood.dimension(1);

                        mean.setReal(getAvg(inCursor.getLongPosition(0),
                                        inCursor.getLongPosition(1), tmpStore,
                                        randomAccess, sizeX, sizeY)
                                        / neighborhood.size());

                        m_binaryOp.compute(mean, inCursor.get(),
                                        outCursor.get());
                }

                return output;
        }

        private double getAvg(long x, long y, long[] tmpStore,
                        RandomAccess<IntType> ii, int sizeX, int sizeY) {
                double res = 0;

                tmpStore[0] = x + sizeX / 2 - ((sizeX + 1) % 2);
                tmpStore[1] = y + sizeY / 2 - ((sizeY + 1) % 2);
                ii.setPosition(tmpStore);
                res += ii.get().getRealDouble();

                tmpStore[0] = x - sizeX / 2 - 1;
                tmpStore[1] = y - sizeY / 2 - 1;
                ii.setPosition(tmpStore);
                res += ii.get().getRealDouble();

                tmpStore[0] = x + sizeX / 2 - ((sizeX + 1) % 2);
                tmpStore[1] = y - sizeY / 2 - 1;
                ii.setPosition(tmpStore);
                res -= ii.get().getRealDouble();

                tmpStore[0] = x - sizeX / 2 - 1;
                tmpStore[1] = y + sizeY / 2 - ((sizeY + 1) % 2);
                ii.setPosition(tmpStore);
                res -= ii.get().getRealDouble();

                return res;
        }

}
