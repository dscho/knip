package org.knime.knip.core.ops.convolution;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.subset.views.ImgView;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.knime.knip.core.util.ApacheMathTools;
import org.knime.knip.core.util.ImgBasedRealMatrix;

import edu.emory.mathcs.backport.java.util.Arrays;

/*
 * TODO: Make Ops out of functions?
 */
public class KernelTools {

        public static <K extends RealType<K>> Img<K> adjustKernelDimensions(
                        int numResDimensions, int[] kernelDims, Img<K> kernel) {

                if (kernelDims.length > kernel.numDimensions()) {
                        throw new IllegalStateException(
                                        "Number of selected dimensions greater than KERNEL dimensions in KernelTools.");
                }

                if (kernelDims.length > numResDimensions) {
                        throw new IllegalStateException(
                                        "Number of selected dimensions greater than result dimensions in KernelTools.");
                }

                if (kernelDims.length == numResDimensions) {
                        return kernel;
                }

                RandomAccessible<K> res = kernel;

                for (int d = kernel.numDimensions(); d < numResDimensions; d++) {
                        res = SubsetViews.addDimension(res);
                }

                long[] max = new long[numResDimensions];
                for (int d = 0; d < kernel.numDimensions(); d++) {
                        max[d] = kernel.max(d);
                }

                long[] resDims = new long[max.length];
                Arrays.fill(resDims, 1);
                for (int d = 0; d < kernelDims.length; d++) {
                        res = SubsetViews.permutate(res, d, kernelDims[d]);
                        resDims[kernelDims[d]] = kernel.dimension(d);
                }

                return new ImgView<K>(Views.interval(res, new FinalInterval(
                                resDims)), kernel.factory());
        }

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

        @SuppressWarnings("unchecked")
        public static <K extends RealType<K> & NativeType<K>> Img<K>[] decomposeKernel(
                        Img<K> kernel) {

                SingularValueDecomposition svd = isDecomposable(SubsetViews
                                .subsetView(kernel, kernel));

                if (svd != null) {
                        int tmp = 0;
                        for (int d = 0; d < kernel.numDimensions(); d++) {
                                if (kernel.dimension(d) > 1)
                                        tmp++;
                        }
                        int[] kernelDims = new int[tmp];
                        tmp = 0;
                        for (int d = 0; d < kernel.numDimensions(); d++) {
                                if (kernel.dimension(d) > 1)
                                        kernelDims[tmp++] = d;
                        }

                        final RealVector v = svd.getV().getColumnVector(0);
                        final RealVector u = svd.getU().getColumnVector(0);
                        final double s = -Math.sqrt(svd.getS().getEntry(0, 0));
                        v.mapMultiplyToSelf(s);
                        u.mapMultiplyToSelf(s);

                        K type = kernel.randomAccess().get().createVariable();

                        Img<K>[] decomposed = new Img[2];

                        decomposed[0] = KernelTools
                                        .adjustKernelDimensions(
                                                        kernel.numDimensions(),
                                                        new int[] { kernelDims[0] },
                                                        ApacheMathTools.vectorToImage(
                                                                        v,
                                                                        type,
                                                                        1,
                                                                        new ArrayImgFactory<K>()));
                        decomposed[1] = KernelTools
                                        .adjustKernelDimensions(
                                                        kernel.numDimensions(),
                                                        new int[] { kernelDims[1] },
                                                        ApacheMathTools.vectorToImage(
                                                                        u,
                                                                        type,
                                                                        1,
                                                                        new ArrayImgFactory<K>()));
                        return decomposed;

                } else {
                        return new Img[] { kernel };
                }

        }
}
