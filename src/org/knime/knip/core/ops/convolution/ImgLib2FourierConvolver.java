package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;

/**
 * Convolution, using ImgLib2Fourier implementation
 *
 * @author Christian Dietz (University of Konstanz)
 */
public class ImgLib2FourierConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                implements Convolver<T, O, K> {

        private RandomAccessible<T> m_lastImg = null;

        private KNIPFFTConvolution<T, O, K> m_fc = null;

        // Empty constructor for extension point
        public ImgLib2FourierConvolver() {
        }

        @Override
        public ImgLib2FourierConvolver<T, O, K> copy() {
                return new ImgLib2FourierConvolver<T, O, K>();
        }

        @Override
        public RandomAccessibleInterval<O> compute(
                        final RandomAccessible<T> in,
                        final RandomAccessibleInterval<K> kernel,
                        final RandomAccessibleInterval<O> out) {

                if (in.numDimensions() != kernel.numDimensions()) {
                        throw new IllegalStateException(
                                        "Kernel dimensions do not match to Img dimensions in ImgLibImageConvolver!");
                }

                if (m_lastImg != in) {
                        m_lastImg = in;
                        m_fc = new KNIPFFTConvolution<T, O, K>(m_lastImg, out,
                                        kernel, kernel, out,
                                        new ArrayImgFactory<ComplexFloatType>());
                        m_fc.setKernel(kernel);
                        m_fc.setKeepImgFFT(true);
                } else {
                        m_fc.setKernel(kernel);
                        m_fc.setOutput(out);
                }

                m_fc.run();

                return out;
        }
}
