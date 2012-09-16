package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz (University of Konstanz)
 * @author Martin Horn (University of Konstanz)
 */
public interface Convolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>, IN extends RandomAccessibleInterval<T>, OUT extends RandomAccessibleInterval<O>, KERNEL extends RandomAccessibleInterval<K>>
                extends BinaryOperation<IN, KERNEL, OUT> {

}