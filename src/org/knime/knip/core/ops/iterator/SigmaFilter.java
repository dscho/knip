package org.knime.knip.core.ops.iterator;

import java.util.Iterator;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author wilderm, University of Konstanz
 *
 * @param <T>
 * @param <Integer>
 * @param <TYPE>
 */
public class SigmaFilter<T extends RealType<T>, V extends RealType<V>, TYPE extends Iterator<T>> implements
        BinaryOperation<TYPE, T, V> {

    private final double sigma;

    private final double sigmaFactor;

    private final double m_sigmaMultiplied;

    private final double pixelFraction;

    private final boolean outlierDetection;

    /**
     * @param sigma
     * @param sigmaFactor
     * @param pixelFraction
     * @param outlierDetection
     */
    public SigmaFilter(final double sigma, final double sigmaFactor, final double pixelFraction,
                       final boolean outlierDetection) {
        this.sigma = sigma;
        this.sigmaFactor = sigmaFactor;
        m_sigmaMultiplied = sigmaFactor * sigma;
        this.pixelFraction = pixelFraction;
        this.outlierDetection = outlierDetection;
    }

    @Override
    public V compute(final TYPE input, final T val, final V output) {

        final double center = val.getRealDouble();
        double sumAll = 0;
        int ctrAll = 0;
        double sumInRange = 0;
        int ctrInRange = 0;

        while (input.hasNext()) {
            final double pixel = input.next().getRealDouble();
            sumAll += pixel;
            if (((center - m_sigmaMultiplied) < pixel) && (pixel < (center + m_sigmaMultiplied))) {
                sumInRange += pixel;
                ctrInRange++;
            }
            ctrAll++;
        }

        final int minPixels = (int)Math.floor(ctrAll * pixelFraction);

        if (ctrInRange >= minPixels) {
            output.setReal(sumInRange / ctrInRange);
        } else {
            if (outlierDetection) {
                output.setReal((sumAll - center) / (ctrAll - 1));
            } else {
                output.setReal(sumAll / ctrAll);
            }
        }

        return output;

    }

    @Override
    public BinaryOperation<TYPE, T, V> copy() {
        return new SigmaFilter<T, V, TYPE>(sigma, sigmaFactor, pixelFraction, outlierDetection);
    }
}
