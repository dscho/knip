package org.knime.knip.core.ops.convolution;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Christian Dietz (Universit�t Konstanz)
 */
public class DirectConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                implements Convolver<T, O, K> {

        public DirectConvolver() {
        }

        @Override
        public RandomAccessibleInterval<O> compute(RandomAccessible<T> input,
                        RandomAccessibleInterval<K> kernel,
                        RandomAccessibleInterval<O> output) {

                // TODO: Workaround until fix in imglib2 (outofbounds gets lost
                // during optimization of transformation)
                long[] max = new long[input.numDimensions()];
                Arrays.fill(max, 2);
                final RandomAccess<T> srcRA = input.randomAccess(null);

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

                                val += srcRA.get().getRealDouble()
                                                * kernelC.get().getRealDouble();
                        }

                        accessRes.setReal(val);
                }

                return output;
        }

        @Override
        public DirectConvolver<T, O, K> copy() {
                return new DirectConvolver<T, O, K>();
        }

}
