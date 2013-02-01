package org.knime.knip.core.ops.misc;

import java.util.LinkedList;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterable.unary.MedianOp;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * Calculates the MAD (median absolute deviation)
 * 
 * @author zinsmaie
 * 
 * @param <T>
 * @param <V>
 */
public class MAD<T extends RealType<T>, V extends RealType<V>> implements
                UnaryOperation<IterableInterval<T>, V> {

        @Override
        public V compute(IterableInterval<T> input, V output) {
                // median
                double median = new MedianOp<T, DoubleType>().compute(
                                input.cursor(), new DoubleType())
                                .getRealDouble();

                // abs deviation from median
                LinkedList<DoubleType> absDeviations = new LinkedList<DoubleType>();
                Cursor<T> c = input.cursor();

                while (c.hasNext()) {
                        double absDeviation = Math.abs(c.next().getRealDouble()
                                        - median);
                        absDeviations.add(new DoubleType(absDeviation));
                }

                // median of abs deviations
                output = new MedianOp<DoubleType, V>().compute(
                                absDeviations.iterator(),
                                output.createVariable());
                return output;
        }

        @Override
        public UnaryOperation<IterableInterval<T>, V> copy() {
                return new MAD<T, V>();
        }
}
