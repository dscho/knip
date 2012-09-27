package org.knime.knip.core.algorithm;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.util.CursorTools;

/**
 * Class which helps to retrieve a RimImage from a given grayscale image at a
 * given position.
 *
 *
 * @author hornm
 *
 */

public class PolarImageFactory<T extends RealType<T>> {

        private RandomAccessible<T> m_interval;

        private int m_angularDimension;

        private long m_numAngles;

        /**
         * Creates a new PolarImageFactory. Note that in some cases a
         * OutOfBoundFactory needs to be defined for the cursor! If not, an
         * ArrayIndexOutOfBoundsException will be thrown.
         *
         * @param interval
         *                the source 'image'
         *
         * @param cursor
         *                the cursor which has to point on a 2D Image,
         *                requiering an OutOfBoundStrategy
         * @param radius
         *                the radius of the rim images obtained with this
         *                RimImageFactory
         *
         */

        public PolarImageFactory(final RandomAccessible<T> interval) {
                this(interval, -1, -1);
        }

        /**
         * Creates a new PolarImageFactory. Note that in some cases a
         * OutOfBoundFactory needs to be defined for the cursor! If not, an
         * ArrayIndexOutOfBoundsException will be thrown.
         *
         * Here, angular information, coded in the given dimension (dividing the
         * circle in the according number of parts), is used additionally. For
         * example, the third dimension has a dimension size of 4, then at the
         * angle of, let's say, 90° the second plane will be used to get the
         * pixel values for the polar image.
         *
         * @param interval
         * @param angularDimension
         *                the dimension which holds angular information (must
         *                exist, if not an ArrayIndexOutOfBounds will be thrown)
         * @param numAng
         *                the number of angles in the angular dimensions (i.e.
         *                the it's dimension size)
         *
         */

        public PolarImageFactory(final RandomAccessible<T> interval,
                        int angularDimension, long numAng) {
                m_interval = interval;
                m_angularDimension = angularDimension;
                m_numAngles = numAng;

        }

        /**
         * Creates a PolarImage at the position (x,y), i.e. retrieving the
         * pixels at circles with different radiuses, put together to an image.
         *
         * @param center
         *
         * @param radius
         *                the radius of the polar images obtained with this
         *                PolarImageFactory, equivalent to the with of the
         *                resulting image
         *
         * @param length
         *                the number of points to be saved a on circle -> is
         *                equivalent to the later length/height of the RimImage
         *
         * @return the polar image
         *
         */
        public Img<T> createPolarImage(final long[] center, final int radius,
                        final int length) {

                @SuppressWarnings("rawtypes")
                Img<T> res = new ArrayImgFactory().create(new int[] { radius,
                                length }, m_interval.randomAccess().get()
                                .createVariable());

                return createPolarImage(center, length, res);

        }

        /**
         * Creates a PolarImage at the position (x,y), i.e. retrieving the
         * pixels at circles with different radiuses, put together to an image.
         *
         * @param center
         * @param radius
         *                the radius of the polar images obtained with this
         *                PolarImageFactory, equivalent to the with of the
         *                resulting image
         *
         * @param length
         *                the number of points to be saved a on circle -> is
         *                equivalent to the later length/height of the RimImage
         *
         * @return the polar image
         *
         */
        public Img<T> createPolarImage(final double[] center, final int radius,
                        final int length) {

                long[] centroid = new long[center.length];

                for (int l = 0; l < center.length; l++) {
                        centroid[l] = (long) center[l];
                }

                return createPolarImage(centroid, radius, length);

        }

        /**
         * Creates a PolarImage at the position (x,y), i.e. retrieving the
         * pixels at circles with different radiuses, put together to an image.
         *
         * @param center
         *
         * @param length
         *                the number of points to be saved a on circle -> is
         *                equivalent to the later length/height of the RimImage
         * @param resImg
         *                writes the result into resImg, you have to make sure
         *                that the result image has the right dimensions (radius
         *                x length)
         *
         * @return the polar image
         *
         */
        public Img<T> createPolarImage(final long[] center, final int length,
                        Img<T> resImg) {

                if (m_angularDimension != -1) {
                        return createPolarImage(center, length,
                                        m_angularDimension, m_numAngles, resImg);
                }

                RandomAccess<T> srcRA = m_interval.randomAccess();

                CursorTools.setPosition(srcRA, center);

                int tmpx, tmpy;
                Cursor<T> polarC = resImg.localizingCursor();
                double angle;
                while (polarC.hasNext()) {
                        polarC.fwd();
                        angle = (double) polarC.getIntPosition(1)
                                        / (double) length * 2 * Math.PI;
                        tmpx = (int) (Math.round(polarC.getLongPosition(0)
                                        * Math.cos(angle)) + center[0]);
                        tmpy = (int) (-Math.round(polarC.getIntPosition(0)
                                        * Math.sin(angle)) + center[1]);
                        srcRA.setPosition(tmpx, 0);
                        srcRA.setPosition(tmpy, 1);
                        polarC.get().set(srcRA.get());
                }

                return resImg;

        }

        /*
         * Creates a PolarImage at the position (x,y), i.e. retrieving the
         * pixels at circles with different radiuses, put together to an image.
         * In contrast to the
         * <code>createPolarImage(center,length)</code>-method, here angular
         * information, coded in the third dimension (dividing the circle in the
         * according number of parts), is used additionally. For example, the
         * third dimension has a dimension size of 4, then at the angle of,
         * let's say, 90° the second plane will be used to get the pixel values
         * for the polar image.
         *
         * @param center
         *
         * @param length the number of points to be saved a on circle -> is
         * equivalent to the later length/height of the RimImage
         *
         * @param angularDimension the dimension, the different angles
         *
         * @return the polar image
         */
        private Img<T> createPolarImage(final long[] center, final int length,
                        int angularDimension, long numAngles, Img<T> resImg) {

                RandomAccess<T> srcRA = m_interval.randomAccess();

                int tmpx, tmpy;
                Cursor<T> polarC = resImg.localizingCursor();
                double angle;
                int angID;

                while (polarC.hasNext()) {
                        polarC.fwd();
                        angle = (double) polarC.getIntPosition(1)
                                        / (double) length * 2 * Math.PI;
                        tmpx = (int) (Math.round(polarC.getLongPosition(0)
                                        * Math.cos(angle)) + center[0]);
                        tmpy = (int) (-Math.round(polarC.getIntPosition(0)
                                        * Math.sin(angle)) + center[1]);

                        angID = (int) ((Math.round(angle / (2 * Math.PI)
                                        * numAngles)) % numAngles);

                        srcRA.setPosition(tmpx, 0);
                        srcRA.setPosition(tmpy, 1);
                        srcRA.setPosition(angID, angularDimension);

                        polarC.get().set(srcRA.get());

                }

                return resImg;

        }
}
