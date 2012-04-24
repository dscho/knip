package org.kniplib.io;

import net.imglib2.Interval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgIOException;

public abstract class ImgOpener<T> {

        /**
         * Opens an {@link ImgPlus} from any DataSource defined in the
         * ImgPlusOpener. Only the given intervals in selected intervals will be
         * opened.
         * 
         * @param file
         *                Absolute path to the file which will be read in
         * @param selectedIntervals
         *                The selected intervals of the source img
         * @return The opened imgplus
         * @throws ImgIOException
         * @throws IncompatibleTypeException
         */
        public abstract Img<T> openImg(final String file,
                        Interval[] selectedIntervals) throws ImgIOException,
                        IncompatibleTypeException;

        /**
         * Opens an {@link ImgPlus} from any DataSource defined in the
         * ImgPlusOpener. The complete image will be read in.
         * 
         * @param file
         *                Absolute path to the file which will be read in.
         * 
         * @return The opened {@link ImgPlus}
         * @throws ImgIOException
         * @throws IncompatibleTypeException
         */
        public Img<T> openImg(final String file) throws ImgIOException,
                        IncompatibleTypeException {
                return openImg(file, null);
        }

        /**
         * Calculates the overall dimensionality of a set of orthogonal
         * intervals. You might use MergeIntervals Operation to merge your
         * source intervals. Please be sure that your source intervals are
         * orthogonal.
         * 
         * @param mergedIntervals
         *                Merged intervals from a set of orthogonal, unmerged
         *                intervals
         * 
         * @param dims
         *                Container for the result
         * 
         * @return
         */
        protected final void getMergedIntervalDimensionality(
                        Interval[] mergedIntervals, long[] dims) {

                if (dims == null)
                        throw new RuntimeException("dims must not be null");

                for (Interval i : mergedIntervals) {
                        if (i.numDimensions() != dims.length)
                                throw new RuntimeException(
                                                "All merged intervals must have the same size");

                        for (int d = 0; d < i.numDimensions(); d++)
                                dims[d] = Math.max(dims[d],
                                                i.min(d) + i.dimension(d));
                }

        }
}
