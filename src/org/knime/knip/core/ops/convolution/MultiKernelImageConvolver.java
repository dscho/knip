package org.knime.knip.core.ops.convolution;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOutputOperation;
import net.imglib2.type.numeric.RealType;

/**
 * Placeholder for MultiKernelImageConvolver.
 *
 * @author Christian Dietz (Universität Konstanz)
 */
public interface MultiKernelImageConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                extends BinaryOutputOperation<Img<T>, Img<K>[], Img<O>> {

        public enum MultiKernelMode {
                ITERATE, ADD_DIM;
        }

}
