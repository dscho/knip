package org.kniplib.tools.matrix;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.kniplib.awt.AWTImageTools;
import org.kniplib.filter.linear.DerivativeOfGaussian;
import org.kniplib.tools.ImgBasedRealMatrix;

/**
 * Experimental
 *
 * @author eethyo
 *
 */
public class MatrixTools {

        /**
         * Based on:
         * http://blogs.mathworks.com/steve/2006/11/28/separable-convolution
         * -part-2/
         *
         * Since now: only two dimensional filters are supported
         *
         * @param filter
         * @return
         */
        @SuppressWarnings("unchecked")
        public static <T extends RealType<T>> Img<T>[] isSeparable(Img<T> filter) {

                if (filter.numDimensions() != 2) {
                        return new Img[] { filter };
                }

                SingularValueDecomposition svd = new SingularValueDecomposition(
                                new ImgBasedRealMatrix<T, Img<T>>(filter, 0, 1));

                if (svd.getRank() != 1) {
                        return new Img[] { filter };
                }

                double scaleFactor = Math.sqrt(svd.getSingularValues()[0]);

                // use V
                Img<T> dim0 = filter.factory().create(
                                new int[] { (int) filter.dimension(0) },
                                filter.firstElement().createVariable());

                RandomAccess<T> dim0Access = dim0.randomAccess();
                RealMatrix v = svd.getV();
                for (int i = 0; i < dim0.dimension(0); i++) {
                        dim0Access.setPosition(i, 0);
                        dim0Access.get()
                                        .setReal(v.getEntry(i, 0) * scaleFactor);
                }

                // use U

                RealMatrix u = svd.getU();
                Img<T> dim1 = filter.factory().create(
                                new int[] { (int) filter.dimension(1) },
                                filter.firstElement().createVariable());
                RandomAccess<T> dim1Access = dim1.randomAccess();
                for (int i = 0; i < dim1.dimension(0); i++) {
                        dim1Access.setPosition(i, 0);
                        dim1Access.get()
                                        .setReal(u.getEntry(i, 0) * scaleFactor);
                }

                return new Img[] { dim0, dim1 };
        }

        public static void main(String[] args) {
                DerivativeOfGaussian filter = new DerivativeOfGaussian(1, 1, 1,
                                0);

                Img<FloatType> test = new ArrayImgFactory<FloatType>().create(
                                new long[] { 3, 3 }, new FloatType());

                RandomAccess<FloatType> randomAccess = test.randomAccess();
                // First Row
                randomAccess.setPosition(0, 1);

                randomAccess.setPosition(0, 0);
                randomAccess.get().set(-1);
                randomAccess.setPosition(1, 0);
                randomAccess.get().set(0);
                randomAccess.setPosition(2, 0);
                randomAccess.get().set(1.0f);

                // Second row
                randomAccess.setPosition(1, 1);

                randomAccess.setPosition(0, 0);
                randomAccess.get().set(-2);
                randomAccess.setPosition(1, 0);
                randomAccess.get().set(0);
                randomAccess.setPosition(2, 0);
                randomAccess.get().set(2);

                // Third row
                randomAccess.setPosition(2, 1);

                randomAccess.setPosition(0, 0);
                randomAccess.get().set(-1);
                randomAccess.setPosition(1, 0);
                randomAccess.get().set(0);
                randomAccess.setPosition(2, 0);
                randomAccess.get().set(1);

                AWTImageTools.showInSameFrame(filter, 2.0);

                MatrixTools.isSeparable(filter);
        }
}
