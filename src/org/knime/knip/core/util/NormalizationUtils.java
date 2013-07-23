package org.knime.knip.core.util;

import net.imglib2.IterableInterval;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.iterableinterval.unary.MinMaxWithSaturation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.ValuePair;

public class NormalizationUtils {

    /**
     * Determines the minimum and factory for scaling according to the given saturation
     *
     * @param <T>
     * @param interval
     * @param saturation the percentage of pixels in the lower and upper domain to be ignored in the normalization
     * @return with the normalization factor at position 0, minimum of the image at position 1
     */
    public static synchronized <T extends RealType<T>, I extends IterableInterval<T>> double[]
            getNormalizationProperties(final I interval, final double saturation) {

        final T type = interval.firstElement().createVariable();
        final MinMaxWithSaturation<T> minMax = new MinMaxWithSaturation<T>(saturation, type);

        final ValuePair<T, T> ValuePair = Operations.compute(minMax, interval);
        return new double[]{
                (1 / (ValuePair.b.getRealDouble() - ValuePair.a.getRealDouble()))
                        * (type.getMaxValue() - type.getMinValue()), ValuePair.a.getRealDouble()};
    }
}
