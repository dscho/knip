package org.knime.knip.core.ops.convolution;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

/**
 * Convolved an RandomAccessibleInterval with a kernel
 *
 * @author eethyo
 *
 * @param <K>
 * @param <KK>
 * @param <KERNEL>
 * @param <IN>
 * @param <OUT>
 */
public class DirectConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>, IN extends RandomAccessibleInterval<T>, OUT extends RandomAccessibleInterval<O>, KERNEL extends RandomAccessibleInterval<K>>
                implements UnaryOperation<IN, OUT> {

        private final KERNEL m_kernel;
        private OutOfBoundsFactory<T, IN> m_fac;

        public DirectConvolver(OutOfBoundsFactory<T, IN> fac) {
                this(null, fac);
        }

        public DirectConvolver(KERNEL kernel, OutOfBoundsFactory<T, IN> fac) {
                m_kernel = kernel;
                m_fac = fac;
        }

        @Override
        public OUT compute(IN input, OUT output) {

                if (m_kernel == null)
                        throw new IllegalStateException(
                                        "Kernel not set in Direct Convolver");

                final RandomAccess<T> srcRA = m_fac.create(input);

                final Cursor<K> kernelC = Views.iterable(m_kernel)
                                .localizingCursor();

                final Cursor<O> resC = Views.iterable(
                                Views.offsetInterval(output, output))
                                .localizingCursor();

                final long[] pos = new long[input.numDimensions()];
                final long[] kernelRadius = new long[m_kernel.numDimensions()];
                for (int i = 0; i < kernelRadius.length; i++) {
                        kernelRadius[i] = m_kernel.dimension(i) / 2;
                }

                RealType<T> accessSrc = srcRA.get();
                O accessRes = resC.get();

                float val;

                while (resC.hasNext()) {
                        // image
                        resC.fwd();
                        resC.localize(pos);

                        // kernel inlined version of the method convolve
                        val = 0;
                        srcRA.setPosition(pos);
                        kernelC.reset();
                        while (kernelC.hasNext()) {
                                kernelC.fwd();

                                for (int i = 0; i < kernelRadius.length; i++) {
                                        if (kernelRadius[i] > 0) { // dimension
                                                                   // can have
                                                                   // zero
                                                                   // extension
                                                                   // e.g.
                                                                   // vertical
                                                                   // 1d kernel
                                                srcRA.setPosition(
                                                                pos[i]
                                                                                + kernelC.getLongPosition(i)
                                                                                - kernelRadius[i],
                                                                i);
                                        }
                                }

                                val += accessSrc.getRealDouble()
                                                * kernelC.get().getRealDouble();
                        }

                        accessRes.setReal(val);
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

                float val = 0;

                srcRA.setPosition(pos);
                kerC.reset();
                while (kerC.hasNext()) {
                        kerC.fwd();

                        for (int i = 0; i < kernelRadii.length; i++) {
                                if (kernelRadii[i] > 0) { // dimension can have
                                                          // zero extension e.g.
                                                          // vertical 1d kernel
                                        srcRA.setPosition(
                                                        pos[i]
                                                                        + kerC.getLongPosition(i)
                                                                        - kernelRadii[i],
                                                        i);
                                }
                        }

                        val += srcRA.get().getRealFloat()
                                        * kerC.get().getRealFloat();
                }
                return val;
        }

        @Override
        public UnaryOperation<IN, OUT> copy() {
                return new DirectConvolver<T, O, K, IN, OUT, KERNEL>(m_kernel,
                                m_fac);
        }

}
