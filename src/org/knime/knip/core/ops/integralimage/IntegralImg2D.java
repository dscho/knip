package org.knime.knip.core.ops.integralimage;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 *
 * @author dietzc, friedrichm
 *
 */
public class IntegralImg2D<T extends RealType<T>, P extends IntegerType<P>>
                implements
                UnaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<P>> {

        @Override
        public RandomAccessibleInterval<P> compute(
                        RandomAccessibleInterval<T> input,
                        RandomAccessibleInterval<P> output) {

                if (input.numDimensions() != 2 || output.numDimensions() != 2)
                        throw new IllegalArgumentException(
                                        "Only two dimensional images are supported by TwoDIntegralImg");

                // +1 because it's used with a first index 1 instead of
                // 0.
                double[][] valueLookupRow = new double[2][(int) input
                                .dimension(0) + 1];

                double value = 0;

                Cursor<T> inCursor = Views.flatIterable(input).cursor();
                Cursor<P> outCursor = Views.flatIterable(output).cursor();
                double valueLookup = 0;

                while (inCursor.hasNext()) {
                        inCursor.fwd();
                        outCursor.fwd();

                        // lookup rows are written and read alternately.
                        int y = inCursor.getIntPosition(1);
                        int x = inCursor.getIntPosition(0);

                        if (x == 0)
                                valueLookup = 0;

                        int curLookupRow = y % 2;
                        int nextLookupRow = (y + 1) % 2;

                        double inputValue = inCursor.get().getRealDouble();

                        value = inputValue + valueLookup
                                        + valueLookupRow[curLookupRow][x + 1]
                                        - valueLookupRow[curLookupRow][x];

                        valueLookup = value;
                        valueLookupRow[nextLookupRow][x + 1] = value;

                        outCursor.get().setReal(value);

                }

                return output;
        }

        @Override
        public UnaryOperation<RandomAccessibleInterval<T>, RandomAccessibleInterval<P>> copy() {
                return new IntegralImg2D<T, P>();
        }

}
