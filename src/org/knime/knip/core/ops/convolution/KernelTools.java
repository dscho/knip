package org.knime.knip.core.ops.convolution;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.img.UnaryConstantRightAssignment;
import net.imglib2.ops.operation.iterable.unary.Sum;
import net.imglib2.ops.operation.real.binary.RealDivide;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.view.Views;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.knime.knip.core.util.ApacheMathTools;
import org.knime.knip.core.util.ImgBasedRealMatrix;

/*
 * TODO: Make Ops out of functions?
 */
public class KernelTools {

        private static <K extends RealType<K>, KERNEL extends RandomAccessibleInterval<K>> SingularValueDecomposition isDecomposable(
                        KERNEL kernel) {

                if (kernel.numDimensions() != 2)
                        return null;

                final RealMatrix mKernel = new ImgBasedRealMatrix<K, KERNEL>(
                                kernel);

                final SingularValueDecomposition svd = new SingularValueDecomposition(
                                mKernel);

                if (svd.getRank() > 1)
                        return null;

                return svd;

        }

        public static synchronized final <T extends RealType<T>, TT extends RandomAccessibleInterval<T>> void normalizeKernelInPlace(
                        final TT kernel) {

                IterableInterval<T> iterable = Views.iterable(kernel);

                DoubleType dsum = new Sum<T, DoubleType>().compute(
                                iterable.cursor(), new DoubleType());

                if (dsum.getRealDouble() == 0)
                        return;

                new UnaryConstantRightAssignment<T, DoubleType, T>(
                                new RealDivide<T, DoubleType, T>()).compute(
                                iterable, dsum, iterable);
        }

        public static <K extends RealType<K>, KERNEL extends RandomAccessibleInterval<K>> RandomAccessibleInterval<DoubleType>[] decomposeKernel(
                        KERNEL kernel) {

                SingularValueDecomposition svd = isDecomposable(kernel);
                if (svd != null) {
                        Img<DoubleType> vkernel;
                        Img<DoubleType> ukernel;

                        final RealVector v = svd.getV().getColumnVector(0);
                        final RealVector u = svd.getU().getColumnVector(0);
                        final double s = -Math.sqrt(svd.getS().getEntry(0, 0));
                        v.mapMultiplyToSelf(s);
                        u.mapMultiplyToSelf(s);
                        vkernel = null;
                        ukernel = null;

                        // V -> horizontal
                        vkernel = ApacheMathTools.vectorToImage(v,
                                        new DoubleType(), 1,
                                        new ArrayImgFactory<DoubleType>());
                        // U -> vertical
                        ukernel = ApacheMathTools.vectorToImage(u,
                                        new DoubleType(), 2,
                                        new ArrayImgFactory<DoubleType>());

                        return new RandomAccessibleInterval[] { vkernel,
                                        ukernel };

                } else {

                        ConvertedRandomAccessibleInterval<K, DoubleType> conv = new ConvertedRandomAccessibleInterval<K, DoubleType>(
                                        kernel, new Converter<K, DoubleType>() {

                                                @Override
                                                public void convert(
                                                                K input,
                                                                DoubleType output) {
                                                        output.set(input.getRealDouble());
                                                }
                                        }, new DoubleType());

                        return new RandomAccessibleInterval[] { conv };
                }

        }
}
