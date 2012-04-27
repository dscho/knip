package org.kniplib.ops.fft;

import net.imglib2.Cursor;
import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.numeric.RealType;

/**
 * Convolved an RandomAccessibleInterval with a kernel
 *
 * @author eethyo
 *
 * @param <T>
 * @param <KT>
 * @param <K>
 * @param <TYPE>
 * @param <OUT>
 */
public class DirectConvolver<T extends RealType<T>, KT extends RealType<KT>, K extends IterableInterval<KT>, TYPE extends RandomAccessibleInterval<T> & IterableInterval<T>>
                implements UnaryOperation<TYPE, TYPE> {

        private final K m_kernel;

        public DirectConvolver(K kernel) {
                m_kernel = kernel;
        }

        @Override
        public TYPE compute(TYPE input, TYPE output) {
                final T min = input.randomAccess().get().createVariable();
                min.setReal(min.getMinValue());

                // TODO: chose the right factory -> Mirror?
                final RandomAccess<T> srcRA = new ExtendedRandomAccessibleInterval<T, TYPE>(
                                input,
                                new OutOfBoundsConstantValueFactory<T, TYPE>(
                                                min)).randomAccess();

                final Cursor<KT> kernelC = m_kernel.localizingCursor();

                final Cursor<T> resC = output.localizingCursor();
                final long[] pos = new long[input.numDimensions()];
                final long[] kernelRadius = new long[m_kernel.numDimensions()];
                for (int i = 0; i < kernelRadius.length; i++) {
                        kernelRadius[i] = m_kernel.dimension(i) / 2;
                }
                while (resC.hasNext()) {
                        resC.fwd();
                        resC.localize(pos);
                        resC.get().setReal(
                                        DirectConvolver.convolve(srcRA,
                                                        kernelC, pos,
                                                        kernelRadius));
                }

                return output;
        }

        /**
         * Straightforward convolution. For small kernels faster than the
         * convolution in the frequency domain for small images.
         *
         * @param img
         *                the image in the spatial domain
         *
         * @param kerC
         *                the kernel in the spatial domain
         *
         * @param pos
         *                the position to apply the kernel
         *
         * @return
         */
        public final static <KT extends RealType<KT>, T extends RealType<T>> float convolve(
                        final RandomAccess<T> srcRA, final Cursor<KT> kerC,
                        final long[] pos, final long[] kernelRadii) {
                srcRA.setPosition(pos);
                float val = 0;
                kerC.reset();
                while (kerC.hasNext()) {
                        kerC.fwd();
                        for (int i = 0; i < kernelRadii.length; i++) {
                                srcRA.setPosition(
                                                pos[i]
                                                                + kerC.getLongPosition(i)
                                                                - kernelRadii[i],
                                                i);
                        }
                        val += srcRA.get().getRealFloat()
                                        * kerC.get().getRealFloat();
                }
                return val;
        }

        @Override
        public UnaryOperation<TYPE, TYPE> copy() {
                return new DirectConvolver<T, KT, K, TYPE>(m_kernel);
        }

}
