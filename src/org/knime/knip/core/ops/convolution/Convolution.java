package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author hornm, schoenen, dietzc, University of Konstanz
 */
public abstract class Convolution<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, IN extends RandomAccessibleInterval<I>, OUT extends RandomAccessibleInterval<O>, KERNEL extends RandomAccessibleInterval<K>>
                implements UnaryOperation<IN, OUT> {

        public abstract void setKernel(final KERNEL kernel);

}