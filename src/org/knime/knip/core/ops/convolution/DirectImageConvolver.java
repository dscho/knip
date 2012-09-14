package org.knime.knip.core.ops.convolution;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.knip.core.types.OutOfBoundsStrategyEnum;
import org.knime.knip.core.types.OutOfBoundsStrategyFactory;
import org.knime.knip.core.util.ImgUtils;

/**
 * Convolved an RandomAccessibleInterval with a kernel
 * 
 * @author dietzc
 * 
 * @param <T>
 * @param <KK>
 * @param <KERNEL>
 * @param <IN>
 * @param <Img<O>>
 */
public class DirectImageConvolver<T extends RealType<T>, O extends RealType<O>, KK extends RealType<KK>, IN extends RandomAccessibleInterval<T>, KERNEL extends RandomAccessibleInterval<KK>>
                implements Convolver<T, O, KK, IN, Img<O>, KERNEL> {

        private RandomAccessibleInterval<DoubleType>[] m_kernels;
        protected boolean m_normalize;
        protected OutOfBoundsStrategyEnum m_fac;

        public DirectImageConvolver() {
                // Empty constructor for extension point
        }

        public DirectImageConvolver(boolean normalizeKernel,
                        OutOfBoundsStrategyEnum fac) {
                m_normalize = normalizeKernel;
                m_fac = fac;
        }

        public DirectImageConvolver(KERNEL kernel, boolean normalizeKernel,
                        OutOfBoundsStrategyEnum fac) {
                this(normalizeKernel, fac);
                setKernel(kernel);
        }

        private DirectImageConvolver(
                        RandomAccessibleInterval<DoubleType>[] kernels,
                        boolean normalizeKernel, OutOfBoundsStrategyEnum fac) {
                this(normalizeKernel, fac);
                m_kernels = kernels;
        }

        @Override
        public Img<O> compute(IN input, Img<O> output) {

                if (m_kernels == null || m_kernels.length == 0)
                        throw new IllegalArgumentException(
                                        "Kernel is not set in DirectImageConvolution");

                DirectConvolver<T, O, DoubleType, IN, Img<O>, RandomAccessibleInterval<DoubleType>> directConvolver = new DirectConvolver<T, O, DoubleType, IN, Img<O>, RandomAccessibleInterval<DoubleType>>(
                                m_kernels[0],
                                OutOfBoundsStrategyFactory
                                                .<T, IN> getStrategy(
                                                                m_fac,
                                                                input.randomAccess()
                                                                                .get()
                                                                                .createVariable()));
                if (m_kernels.length == 1) {
                        return directConvolver.compute(input, output);
                } else {

                        Img<O> buffer = ImgUtils.createEmptyCopy(
                                        output.factory(), output, output
                                                        .randomAccess().get()
                                                        .createVariable());
                        Img<O> tmpOutput = output;
                        Img<O> tmpInput = buffer;
                        Img<O> tmp;

                        // Check needs to be done as the number of operations
                        // may be uneven and
                        // the result may not be written to output
                        if (m_kernels.length % 2 == 0) {
                                tmpOutput = buffer;
                                tmpInput = output;
                        }

                        directConvolver.compute(input, tmpOutput);

                        for (int i = 1; i < m_kernels.length; i++) {
                                tmp = tmpInput;
                                tmpInput = tmpOutput;
                                tmpOutput = tmp;
                                new DirectConvolver<O, O, DoubleType, Img<O>, Img<O>, RandomAccessibleInterval<DoubleType>>(
                                                m_kernels[i],
                                                OutOfBoundsStrategyFactory
                                                                .<O, Img<O>> getStrategy(
                                                                                m_fac,
                                                                                buffer.firstElement()
                                                                                                .createVariable()))
                                                .compute(tmpInput, tmpOutput);
                        }

                }

                return output;
        }

        @Override
        public UnaryOperation<IN, Img<O>> copy() {
                return new DirectImageConvolver<T, O, KK, IN, KERNEL>(
                                m_kernels, m_normalize, m_fac);
        }

        @Override
        public void setKernel(KERNEL kernel) {
                RandomAccessibleInterval<DoubleType>[] decomposeKernel = KernelTools
                                .decomposeKernel(kernel);
                if (m_normalize) {
                        for (RandomAccessibleInterval<DoubleType> decomposed : KernelTools
                                        .decomposeKernel(kernel)) {
                                KernelTools.normalizeKernelInPlace(decomposed);
                        }
                }

                m_kernels = decomposeKernel;
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

}
