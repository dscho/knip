package org.knime.knip.core.awt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labeling.Labeling;
import net.imglib2.meta.ImageMetadata;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;


public class RendererFactory {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends Type<T>> ImageRenderer<T>[] createSuitableRenderer(
                                                                                final RandomAccessibleInterval<T> img) {

        final List<ImageRenderer> res = new ArrayList<ImageRenderer>();

        if (img instanceof Labeling) {
            res.add(new RandomColorLabelingRenderer());
            res.add(new BoundingBoxLabelRenderer());
            res.add(new BoundingBoxRandomColorLabelRenderer());
        } else {
            final T type = img.randomAccess().get();

            if (type instanceof RealType) {
                res.add(new Real2GreyRenderer());
                for (int d = 0; d < img.numDimensions(); d++) {
                    if ((img.dimension(d) > 1)
                            && (img.dimension(d) < 4)) {
                        res.add(new Real2ColorRenderer(
                                                       d));
                    }
                }
            }
        }

        return res.toArray(new ImageRenderer[res.size()]);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends Type<T>> ImageRenderer<T>[] createSuitableRenderer(
                                                                                final RandomAccessibleInterval<T> img,
                                                                                final ImageMetadata imageMetaData) {

        final List<ImageRenderer> res = new ArrayList<ImageRenderer>();
        res.addAll(Arrays.asList(createSuitableRenderer(img)));

        // color rendering
        final T type = img.randomAccess().get();

        if (type instanceof RealType) {
            if ((imageMetaData != null)
                    && (imageMetaData.getColorTableCount() > 0)) {

                for (int d = 0; d < img.numDimensions(); d++) {
                    if (img.dimension(d) == imageMetaData
                            .getColorTableCount()) {

                        res.add(new Real2TableColorRenderer(
                                                            d));
                    }
                }
            }
        }

        return res.toArray(new ImageRenderer[res.size()]);
    }

}
