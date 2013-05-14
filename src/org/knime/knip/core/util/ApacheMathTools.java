package org.knime.knip.core.util;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author schoenen
 */
public final class ApacheMathTools {

    private ApacheMathTools() {
        // Utility
    }

    /**
     * Creates a numDims dimensions image where all dimensions d<numDims are
     * of size 1 and the last dimensions contains the vector.
     *
     * @param <R>
     * @param ar
     * @param type
     * @param numDims
     *                number of dimensions
     * @return
     */
    public static <R extends RealType<R>> Img<R> vectorToImage(
                                                               final RealVector ar, final R type, final int numDims,
                                                               final ImgFactory<R> fac) {
        final long[] dims = new long[numDims];

        for (int i = 0; i < (dims.length - 1); i++) {
            dims[i] = 1;
        }
        dims[dims.length - 1] = ar.getDimension();
        final Img<R> res = fac.create(dims, type);
        final Cursor<R> c = res.cursor();
        while (c.hasNext()) {
            c.fwd();
            c.get().setReal(ar.getEntry(c
                                        .getIntPosition(numDims - 1)));
        }

        return res;
    }

    /**
     *
     * @param <R>
     * @param img
     *                A two dimensional image.
     * @return
     */
    public static <R extends RealType<R>> RealMatrix toMatrix(
                                                              final Img<R> img) {

        assert img.numDimensions() == 2;
        final RealMatrix ret = new BlockRealMatrix((int) img.dimension(0),
                                                   (int) img.dimension(1));
        final Cursor<R> c = img.cursor();
        while (c.hasNext()) {
            c.fwd();
            ret.setEntry(c.getIntPosition(0), c.getIntPosition(1),
                         c.get().getRealDouble());
        }
        return ret;
    }

}
