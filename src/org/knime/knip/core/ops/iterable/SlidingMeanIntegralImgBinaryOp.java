package org.knime.knip.core.ops.iterable;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.UnaryOperation;
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
        private final int m_span;

        public SlidingMeanIntegralImgBinaryOp(
                        BinaryOperation<DoubleType, T, V> binaryOp,
                        RectangleShape shape, int span,
                        OutOfBoundsFactory<T, IN> outOfBounds) {
                super(shape, outOfBounds);
                m_binaryOp = binaryOp;
                m_iiOp = new IntegralImg2D<T, IntType>();
                m_span = span;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new SlidingMeanIntegralImgBinaryOp<T, V, IN, OUT>(
                                m_binaryOp.copy(), (RectangleShape) shape,
                                m_span, outofbounds);
        }

        @Override
        protected OUT compute(IterableInterval<Neighborhood<T>> neighborhoods,
                        IN input, OUT output) {

                if (input.numDimensions() != 2 || output.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "Only two dimensional images allowed");
                }

                long[] extendedDims = new long[input.numDimensions()];
                input.dimensions(extendedDims);

                extendedDims[0] += m_span;
                extendedDims[1] += m_span;

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
                                                                }),
                                                Views.interval(Views.extend(
                                                                input,
                                                                outofbounds),
                                                                new FinalInterval(
                                                                                extendedDims)));

                RandomAccess<IntType> randomAccess = Views.extendValue(ii,
                                new IntType(0)).randomAccess();

                DoubleType mean = new DoubleType();
                long[] tmpStore = new long[2];

                for (final Neighborhood<T> neighborhood : neighborhoods) {
                        inCursor.fwd();
                        outCursor.fwd();

                        int sizeX = (int) neighborhood.dimension(0);
                        int sizeY = (int) neighborhood.dimension(1);

                        mean.setReal(getSum(inCursor.getLongPosition(0),
                                        inCursor.getLongPosition(1), tmpStore,
                                        randomAccess, sizeX, sizeY)
                                        / neighborhood.size());

                        m_binaryOp.compute(mean, inCursor.get(),
                                        outCursor.get());
                }

                return output;
        }

        private double getSum(long x, long y, long[] tmpStore,
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
