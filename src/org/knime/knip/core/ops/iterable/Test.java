package org.knime.knip.core.ops.iterable;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.algorithm.region.localneighborhood.Neighborhood;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.ops.iterable.MedianOp;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Test {

        public static void main(String[] args) {

                MedianOp<FloatType, FloatType> medianOp = new MedianOp<FloatType, FloatType>();

                Img<FloatType> input = new PlanarImgFactory<FloatType>()
                                .create(new long[] { 100, 100, 5 },
                                                new FloatType());

                Img<FloatType> output = new PlanarImgFactory<FloatType>()
                                .create(new long[] { 100, 100, 5 },
                                                new FloatType());

                RectangleShape shape = new RectangleShape(1, false);
                // Neighboor update

                IntervalView<FloatType> interval = Views
                                .interval(Views.extend(
                                                input,
                                                new OutOfBoundsConstantValueFactory<FloatType, Img<FloatType>>(
                                                                new FloatType(
                                                                                1337))),
                                                input);

                IterableInterval<Neighborhood<FloatType>> neighborhoods = shape
                                .neighborhoods(interval);

                // Create an iterable to check iteration order
                if (!neighborhoods.iterationOrder().equals(
                                output.iterationOrder()))
                        throw new IllegalArgumentException(
                                        "Iteration order doesn't fit in SlidingNeighborhoodOp");

                Cursor<FloatType> outCursor = output.cursor();
                for (final Neighborhood<FloatType> neighborhood : neighborhoods) {
                        medianOp.compute(neighborhood.cursor(),
                                        outCursor.next());
                }
        }
}
