package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.util.ImgUtils;

/**
 * Class applying a convolution of multiple kernels with an {@link Img} in an
 * iterative by using first the initital convolver and afterwards the followup
 * convolver.
 *
 * @author Christian Dietz (University of Konstanz)
 */
public class IterativeImageConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>, KERNEL extends RandomAccessibleInterval<K>>
                implements MultiKernelImageConvolver<T, O, K> {

        private final Convolver<O, O, K, Img<O>, Img<O>, Img<K>> m_followConv;
        private final Convolver<T, O, K, Img<T>, Img<O>, Img<K>> m_initConv;

        private Img<O> m_buffer;
        private final O m_resType;

        public IterativeImageConvolver(
                        Convolver<T, O, K, Img<T>, Img<O>, Img<K>> init,
                        Convolver<O, O, K, Img<O>, Img<O>, Img<K>> follower,
                        O resType) {
                m_initConv = init;
                m_followConv = follower;
                m_resType = resType;
        }

        @Override
        public Img<O> compute(Img<T> input, Img<K>[] kernelList, Img<O> output) {

                // Trivial case
                if (kernelList.length == 1)
                        return m_initConv.compute(input, kernelList[0], output);

                initBuffer(input, output);
                Img<O> tmpOutput = output;

                Img<O> tmpInput = m_buffer;
                Img<O> tmp;

                if (kernelList.length % 2 == 0) {
                        tmpOutput = m_buffer;
                        tmpInput = output;
                }

                m_initConv.compute(input, kernelList[0], tmpOutput);

                for (int i = 1; i < kernelList.length; i++) {
                        tmp = tmpInput;
                        tmpInput = tmpOutput;
                        tmpOutput = tmp;
                        m_followConv.compute(tmpInput, kernelList[1], tmpOutput);
                }

                return output;
        }

        private void initBuffer(Img<T> input, Img<O> output) {
                if (m_buffer == null || !equalsInterval(output, m_buffer)) {
                        m_buffer = ImgUtils.createEmptyCopy(output,
                                        output.firstElement());
                }

        }

        private boolean equalsInterval(Img<O> i1, Img<O> i2) {
                for (int d = 0; d < i1.numDimensions(); d++)
                        if (i1.dimension(d) != i2.dimension(d))
                                return false;
                return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public BinaryOperation<Img<T>, Img<K>[], Img<O>> copy() {
                return new IterativeImageConvolver<T, O, K, Img<K>>(
                                (Convolver<T, O, K, Img<T>, Img<O>, Img<K>>) m_initConv
                                                .copy(),
                                (Convolver<O, O, K, Img<O>, Img<O>, Img<K>>) m_followConv
                                                .copy(), m_resType
                                                .createVariable());
        }

        @Override
        public Img<O> createEmptyOutput(Img<T> input, Img<K>[] kernels) {
                return ImgUtils.createEmptyCopy(input, m_resType);
        }

        @Override
        public Img<O> compute(Img<T> in, Img<K>[] kernels) {
                return compute(in, kernels, createEmptyOutput(in, kernels));
        }

}
