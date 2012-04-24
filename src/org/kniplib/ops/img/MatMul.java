package org.kniplib.ops.img;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.BinaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

/**
 * Matrix multiplication.
 * 
 * @author hornm, University of Konstanz
 */
public class MatMul<T extends RealType<T> & NativeType<T>>
                implements
                BinaryOutputOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, Img<T>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<T> createEmptyOutput(RandomAccessibleInterval<T> op0,
                        RandomAccessibleInterval<T> op1) {
                checkContraints(op0, op1);
                Img<T> res = new ArrayImgFactory<T>()
                                .create(new long[] { op0.dimension(0),
                                                op1.dimension(1) }, op1
                                                .randomAccess().get()
                                                .createVariable());
                return res;
        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        @Override
        public Img<T> compute(RandomAccessibleInterval<T> op0,
                        RandomAccessibleInterval<T> op1, Img<T> r) {
                checkContraints(op0, op1);

                // perform matrix multiplication
                RandomAccess2D<T> ra1 = new RandomAccess2D<T>(op0);
                RandomAccess2D<T> ra2 = new RandomAccess2D<T>(op1);

                RandomAccess2D<T> raRes = new RandomAccess2D<T>(r);

                for (int x = 0; x < op0.dimension(0); x++) {
                        for (int y = 0; y < op1.dimension(1); y++) {
                                T res = raRes.get(x, y);
                                for (int i = 0; i < op0.dimension(1); i++) {
                                        res.setReal(res.getRealDouble()
                                                        + ra1.get(x, i)
                                                                        .getRealDouble()
                                                        * ra2.get(i, y)
                                                                        .getRealDouble());

                                }
                        }
                }
                return r;
        }

        private void checkContraints(Interval op0, Interval op1) {
                if (op0.numDimensions() != 2 || op1.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "Matrix multiplication only suitable for 2D images.");
                }
                if (op0.dimension(1) != op1.dimension(0)) {
                        throw new IllegalArgumentException(
                                        "Dimensions of images doesn't fit for matrix multiplication: img1.dimY != img2.dimX");
                }
        }

        /**
         * 
         * @author hornm, University of Konstanz
         */
        private class RandomAccess2D<T extends RealType<T>> {

                private RandomAccess<T> m_ra;

                public RandomAccess2D(RandomAccessibleInterval<T> i) {
                        m_ra = i.randomAccess();
                }

                public T get(int row, int col) {
                        m_ra.setPosition(row, 0);
                        m_ra.setPosition(col, 1);
                        return m_ra.get();
                }

        }

        public Img<T> compute(RandomAccessibleInterval<T> op0,
                        RandomAccessibleInterval<T> op1) {
                return compute(op0, op1, createEmptyOutput(op0, op1));
        }

        @Override
        public BinaryOutputOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>, Img<T>> copy() {
                return new MatMul<T>();
        }

}
