package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz (Universit�t Konstanz)
 */
public interface MultiKernelConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                extends
                BinaryOperation<RandomAccessible<T>, RandomAccessibleInterval<K>[], RandomAccessibleInterval<O>> {

}
