package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author dietzc, University of Konstanz
 */
public interface Convolver<I extends RealType<I>, O extends RealType<O>, K extends RealType<K>, IN extends RandomAccessibleInterval<I>, OUT extends RandomAccessibleInterval<O>, KERNEL extends RandomAccessibleInterval<K>>
                extends UnaryOperation<IN, OUT> {

        void setKernel(final KERNEL kernel);
}