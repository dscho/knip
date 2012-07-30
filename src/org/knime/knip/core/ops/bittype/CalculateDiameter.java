package org.knime.knip.core.ops.bittype;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.img.Img;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

public class CalculateDiameter implements
                UnaryOutputOperation<Img<BitType>, DoubleType> {

        @Override
        public DoubleType compute(Img<BitType> input, DoubleType output) {
                double diameter = 0.0f;

                Cursor<BitType> cursor = input.localizingCursor();

                List<Point> points = new ArrayList<Point>((int) input.size());

                int[] position = new int[cursor.numDimensions()];
                while (cursor.hasNext()) {
                        cursor.fwd();
                        if (cursor.get().get()) {
                                cursor.localize(position);
                                points.add(new Point(position));
                        }
                }

                for (Point p : points) {
                        for (Point p2 : points) {
                                double dist = 0.0f;
                                for (int i = 0; i < p.numDimensions(); i++) {
                                        dist += (p.getIntPosition(i) - p2
                                                        .getIntPosition(i))
                                                        * (p.getIntPosition(i) - p2
                                                                        .getIntPosition(i));
                                }
                                diameter = Math.max(diameter, dist);
                        }
                }

                // sqrt for euclidean
                diameter = Math.sqrt(diameter);

                output.set(diameter);
                return output;
        }

        @Override
        public DoubleType createEmptyOutput(Img<BitType> in) {
                return new DoubleType();
        }

        @Override
        public DoubleType compute(Img<BitType> in) {
                return compute(in, createEmptyOutput(in));
        }

        @Override
        public UnaryOutputOperation<Img<BitType>, DoubleType> copy() {
                return new CalculateDiameter();
        }

}
