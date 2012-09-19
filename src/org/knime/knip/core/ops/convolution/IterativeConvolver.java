package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * @author Christian Dietz (University of Konstanz)
 */
public abstract class IterativeConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                implements MultiKernelConvolver<T, O, K> {

        private final Convolver<O, O, K> m_followConv;
        private final Convolver<T, O, K> m_initConv;

        private RandomAccessibleInterval<O> m_buffer;

        public IterativeConvolver(Convolver<T, O, K> init,
                        Convolver<O, O, K> follower) {
                m_initConv = init;
                m_followConv = follower;
        }

        @Override
        public RandomAccessibleInterval<O> compute(RandomAccessible<T> input,
                        RandomAccessibleInterval<K>[] kernelList,
                        RandomAccessibleInterval<O> output) {

                // Trivial case
                if (kernelList.length == 1)
                        return m_initConv.compute(input, kernelList[0], output);

                initBuffer(input, output);
                RandomAccessibleInterval<O> tmpOutput = output;

                RandomAccessibleInterval<O> tmpInput = m_buffer;
                RandomAccessibleInterval<O> tmp;

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

        private void initBuffer(RandomAccessible<T> input,
                        RandomAccessibleInterval<O> output) {
                if (m_buffer == null || !equalsInterval(output, m_buffer)) {
                        m_buffer = createBuffer(input, output);
                }
        }

        private boolean equalsInterval(RandomAccessibleInterval<O> i1,
                        RandomAccessibleInterval<O> i2) {
                for (int d = 0; d < i1.numDimensions(); d++)
                        if (i1.dimension(d) != i2.dimension(d))
                                return false;
                return true;
        }

        protected abstract RandomAccessibleInterval<O> createBuffer(
                        RandomAccessible<T> input,
                        RandomAccessibleInterval<O> output);

        @Override
        public BinaryOperation<RandomAccessible<T>, RandomAccessibleInterval<K>[], RandomAccessibleInterval<O>> copy() {
                return new IterativeConvolver<T, O, K>(
                                (Convolver<T, O, K>) m_initConv.copy(),
                                (Convolver<O, O, K>) m_followConv.copy()) {

                        @Override
                        protected RandomAccessibleInterval<O> createBuffer(
                                        RandomAccessible<T> input,
                                        RandomAccessibleInterval<O> output) {
                                return IterativeConvolver.this.createBuffer(
                                                input, output);
                        }

                };
        }

}
