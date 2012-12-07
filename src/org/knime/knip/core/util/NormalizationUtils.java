package org.knime.knip.core.util;

import net.imglib2.IterableInterval;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.iterableinterval.unary.MinMax;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

public class NormalizationUtils {

        /**
         * Determines the minimum and factory for scaling according to the given
         * saturation
         *
         * @param <T>
         * @param interval
         * @param saturation
         *                the percentage of pixels in the lower and upper domain
         *                to be ignored in the normalization
         * @return with the normalization factor at position 0, minimum of the
         *         image at position 1
         */
        public static synchronized <T extends RealType<T>, I extends IterableInterval<T>> double[] getNormalizationProperties(
                        I interval, double saturation) {

                T type = interval.firstElement().createVariable();
                MinMax<T> minMax = new MinMax<T>(saturation, type);

                Pair<T, T> pair = Operations.compute(minMax, interval);
                return new double[] {
                                1
                                                / (pair.b.getRealDouble() - pair.a
                                                                .getRealDouble())
                                                * (type.getMaxValue() - type
                                                                .getMinValue()),
                                pair.a.getRealDouble() };
        }
}
