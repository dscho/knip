package org.knime.knip.core.ops.misc;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterable.unary.Mean;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * calculates the mean based counterpart of the MAD (median absolute deviation)
 * 
 * @author zinsmaie
 * 
 * @param <T>
 * @param <V>
 */
public class MeanAbsoluteDeviation<T extends RealType<T>, V extends RealType<V>>
implements UnaryOperation<IterableInterval<T>, V> {

    @Override
    public V compute(final IterableInterval<T> input, final V output) {
        // mean
        final double mean = new Mean<T, DoubleType>().compute(input.cursor(),
                                                              new DoubleType()).getRealDouble();

        // abs deviation from mean
        long i = 0;
        double absDeviationSum = 0;
        final Cursor<T> c = input.cursor();

        while (c.hasNext()) {
            absDeviationSum += Math.abs(c.next().getRealDouble()
                                        - mean);
            i++;
        }

        // mean of abs deviations
        output.setReal(absDeviationSum / i);
        return output;
    }

    @Override
    public UnaryOperation<IterableInterval<T>, V> copy() {
        return new MeanAbsoluteDeviation<T, V>();
    }
}
