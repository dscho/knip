package org.knime.knip.core.awt.specializedrendering;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.display.projectors.screenimages.ByteScreenImage;
import net.imglib2.display.projectors.screenimages.ShortScreenImage;
import net.imglib2.display.projectors.specializedprojectors.ArrayImgXYByteProjector;
import net.imglib2.display.projectors.specializedprojectors.ArrayImgXYShortProjector;
import net.imglib2.display.projectors.specializedprojectors.PlanarImgXYByteProjector;
import net.imglib2.display.projectors.specializedprojectors.PlanarImgXYShortProjector;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;

import org.knime.knip.core.types.NativeTypes;

public class FastNormalizingGreyRendering {

        public static <R extends RealType<R>> ScreenImage tryRendering(
                        RandomAccessibleInterval<R> source, final int dimX, final int dimY,
                        final long[] planePos, final double normalizationFactor, final double min) {

                // unwrap img plus if necessary
                while (source instanceof ImgPlus) {
                        source = ((ImgPlus) source).getImg();
                }

                RenderTripel match = new RenderTripel();

                if (!match.m_successfull) {
                        // try ArrayImage
                        match = tryArrayImage(source, dimX, dimY, planePos,
                                        normalizationFactor, min);
                }

                if (!match.m_successfull) {
                        // try PlanarImage
                        match = tryPlanarImage(source, dimX, dimY, planePos,
                                        normalizationFactor, min);
                }

                if (match.m_successfull) {
                        // speed up possible use tuned implementation
                        match.m_projector.setPosition(planePos);
                        match.m_projector.map();
                        return match.m_image;
                } else {
                        return null;
                }
        }

        private static <R extends RealType<R>> RenderTripel tryArrayImage(
                        final RandomAccessibleInterval<R> source, final int dimX, final int dimY,
                        final long[] planePos, final double normalizationFactor, final double min) {

                if (dimX == 0 && dimY == 1 && source instanceof ArrayImg) {
                        Abstract2DProjector<?, ?> projector;
                        ScreenImage target;
                        final NativeTypes type = NativeTypes.getPixelType(source
                                        .randomAccess().get());

                        final long w = source.dimension(dimX);
                        final long h = source.dimension(dimY);

                        if (type == NativeTypes.BYTETYPE
                                        || type == NativeTypes.UNSIGNEDBYTETYPE) {
                                target = new ByteScreenImage(new ByteArray(
                                                new byte[(int) (w * h)]),
                                                new long[] { w, h });

                                projector = new ArrayImgXYByteProjector<ByteType, ByteType>(
                                                (ArrayImg<ByteType, ByteArray>) source,
                                                ((ByteScreenImage) target),
                                                normalizationFactor, min, type
                                                                .isSigned());
                                return new RenderTripel(projector, target);
                        } else if (type == NativeTypes.SHORTTYPE
                                        || type == NativeTypes.UNSIGNEDSHORTTYPE) {
                                target = new ShortScreenImage(new ShortArray(
                                                new short[(int) (w * h)]),
                                                new long[] { w, h });

                                projector = new ArrayImgXYShortProjector<ShortType, ShortType>(
                                                (ArrayImg<ShortType, ShortArray>) source,
                                                ((ShortScreenImage) target),
                                                normalizationFactor, min, type
                                                                .isSigned());
                                return new RenderTripel(projector, target);
                        }
                }

                return new RenderTripel();
        }

        private static <R extends RealType<R>> RenderTripel tryPlanarImage(
                        final RandomAccessibleInterval<R> source, final int dimX, final int dimY,
                        final long[] planePos, final double normalizationFactor, final double min) {

                if (dimX == 0 && dimY == 1 && source instanceof PlanarImg) {
                        Abstract2DProjector<?, ?> projector;
                        ScreenImage target;
                        final NativeTypes type = NativeTypes.getPixelType(source
                                        .randomAccess().get());

                        final long w = source.dimension(dimX);
                        final long h = source.dimension(dimY);

                        if (type == NativeTypes.BYTETYPE
                                        || type == NativeTypes.UNSIGNEDBYTETYPE) {
                                target = new ByteScreenImage(new ByteArray(
                                                new byte[(int) (w * h)]),
                                                new long[] { w, h });

                                projector = new PlanarImgXYByteProjector<ByteType, ByteType>(
                                                (PlanarImg<ByteType, ByteArray>) source,
                                                ((ByteScreenImage) target),
                                                normalizationFactor, min, type
                                                                .isSigned());
                                return new RenderTripel(projector, target);
                        } else if (type == NativeTypes.SHORTTYPE
                                        || type == NativeTypes.UNSIGNEDSHORTTYPE) {
                                target = new ShortScreenImage(new ShortArray(
                                                new short[(int) (w * h)]),
                                                new long[] { w, h });

                                projector = new PlanarImgXYShortProjector<ShortType, ShortType>(
                                                (PlanarImg<ShortType, ShortArray>) source,
                                                ((ShortScreenImage) target),
                                                normalizationFactor, min, type
                                                                .isSigned());
                                return new RenderTripel(projector, target);
                        }
                }

                return new RenderTripel();
        }
}
