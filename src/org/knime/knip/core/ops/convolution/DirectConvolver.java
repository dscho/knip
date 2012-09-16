package org.knime.knip.core.ops.convolution;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.core.types.OutOfBoundsStrategyEnum;
import org.knime.knip.core.types.OutOfBoundsStrategyFactory;

/**
 * Christian Dietz (Universität Konstanz)
 */
public class DirectConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>, IN extends RandomAccessibleInterval<T>, OUT extends RandomAccessibleInterval<O>, KERNEL extends RandomAccessibleInterval<K>>
                implements BinaryOperation<IN, KERNEL, OUT> {

        private final OutOfBoundsStrategyEnum m_fac;

        public DirectConvolver(OutOfBoundsStrategyEnum fac) {
                m_fac = fac;
        }

        @Override
        public OUT compute(IN input, KERNEL kernel, OUT output) {

                final RandomAccess<T> srcRA = OutOfBoundsStrategyFactory
                                .getStrategy(m_fac,
                                                input.randomAccess()
                                                                .get()
                                                                .createVariable())
                                .create(input);

                final Cursor<K> kernelC = Views.iterable(kernel)
                                .localizingCursor();

                final Cursor<O> resC = Views.iterable(
                                Views.offsetInterval(output, output))
                                .localizingCursor();

                final long[] pos = new long[input.numDimensions()];
                final long[] kernelRadius = new long[kernel.numDimensions()];
                for (int i = 0; i < kernelRadius.length; i++) {
                        kernelRadius[i] = kernel.dimension(i) / 2;
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

        @Override
        public BinaryOperation<IN, KERNEL, OUT> copy() {
                return new DirectConvolver<T, O, K, IN, OUT, KERNEL>(m_fac);
        }

}
