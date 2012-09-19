package org.knime.knip.core.ops.convolution;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.types.OutOfBoundsStrategyEnum;

/**
 * Class wrapping a DirectConvolver. Kernels are decomposed if possible. Kernels
 * are reused if possible. Number of kernel dimensions and number of input
 * dimension must match!
 *
 * Christian Dietz (Universität Konstanz)
 */
public class DirectImageConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K> & NativeType<K>>
                implements Convolver<T, O, K, Img<T>, Img<O>, Img<K>> {

        protected OutOfBoundsStrategyEnum m_fac;
        private boolean m_decompose;

        private Img<K>[] m_decomposedKernels = null;
        private Img<K> m_kernel;

        public DirectImageConvolver() {
                // Empty constructor
        }

        private DirectImageConvolver(OutOfBoundsStrategyEnum fac,
                        boolean decompose) {
                m_fac = fac;
                m_decompose = decompose;
        }

        public DirectImageConvolver(OutOfBoundsStrategyEnum fac) {
                this(fac, true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Img<O> compute(Img<T> input, Img<K> kernel, Img<O> output) {
                if (kernel.numDimensions() != input.numDimensions()) {
                        throw new IllegalStateException(
                                        "Kernel dimensions do not match to Img dimensions in ImgLibImageConvolver!");
                }

                if (m_kernel != kernel) {
                        m_kernel = kernel;
                        if (m_decompose) {
                                m_decomposedKernels = KernelTools
                                                .decomposeKernel(kernel);
                        } else {
                                m_decomposedKernels = new Img[] { kernel };
                        }
                }

                if (m_decomposedKernels.length == 1)
                        return new DirectConvolver<T, O, K, Img<T>, Img<O>, Img<K>>(
                                        m_fac).compute(input,
                                        m_decomposedKernels[0], output);
                else {
                        return new IterativeImageConvolver<T, O, K, Img<K>>(
                                        new DirectImageConvolver<T, O, K>(
                                                        m_fac, false),
                                        new DirectImageConvolver<O, O, K>(
                                                        m_fac, false), output
                                                        .firstElement()
                                                        .createVariable())
                                        .compute(input, m_decomposedKernels,
                                                        output);
                }
        }

        @Override
        public BinaryOperation<Img<T>, Img<K>, Img<O>> copy() {
                return new DirectImageConvolver<T, O, K>(m_fac);
        }

        public void setOutOfBoundsStrategyEnum(
                        OutOfBoundsStrategyEnum oobStrategy) {
                m_fac = oobStrategy;
        }

}
