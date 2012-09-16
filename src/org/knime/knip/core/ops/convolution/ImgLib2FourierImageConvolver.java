package org.knime.knip.core.ops.convolution;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * Convolvution, using ImgLib2Fourier implementation
 *
 * @author Christian Dietz (University of Konstanz)
 */
public class ImgLib2FourierImageConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>>
                implements Convolver<T, O, K, Img<T>, Img<O>, Img<K>> {

        private Img<T> m_lastImg = null;

        private KNIPFFTConvolution<T, O, K> m_fc = null;

        // Empty constructor for extension point
        public ImgLib2FourierImageConvolver() {
        }

        @Override
        public ImgLib2FourierImageConvolver<T, O, K> copy() {
                return new ImgLib2FourierImageConvolver<T, O, K>();
        }

        @Override
        public Img<O> compute(final Img<T> in, Img<K> kernel, final Img<O> out) {

                if (in.numDimensions() != kernel.numDimensions()) {
                        throw new IllegalStateException(
                                        "Kernel dimensions do not match to Img dimensions in ImgLibImageConvolver!");
                }

                if (m_lastImg != in) {
                        m_lastImg = in;
                        m_fc = new KNIPFFTConvolution<T, O, K>(m_lastImg,
                                        kernel, out);
                        m_fc.setKeepImgFFT(true);
                } else {
                        m_fc.setKernel(kernel);
                        m_fc.setOutput(out);
                }

                m_fc.run();

                return out;
        }

}
