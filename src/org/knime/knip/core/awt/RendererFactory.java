package org.knime.knip.core.awt;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labeling.Labeling;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;


public class RendererFactory {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public static <T extends Type<T>, I extends RandomAccessibleInterval<T>> ImageRenderer<T, I>[] createSuitableRenderer(
                        final I img) {

                List<ImageRenderer> res = new ArrayList<ImageRenderer>();

                if (img instanceof Labeling) {
                        res.add(new RandomColorLabelingRenderer());
                        res.add(new BoundingBoxLabelRenderer());
                        res.add(new BoundingBoxRandomColorLabelRenderer());
                } else {
                        T type = img.randomAccess().get();

                        if (type instanceof RealType) {
                                res.add(new Real2GreyRenderer());
                                for (int d = 0; d < img.numDimensions(); d++) {
                                        if (img.dimension(d) > 1
                                                        && img.dimension(d) < 4) {
                                                res.add(new Real2ColorRenderer(
                                                                d));
                                        }
                                }
                        }
                }

                return res.toArray(new ImageRenderer[res.size()]);
        }

}
