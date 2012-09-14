package org.knime.knip.core.ops.convolution;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 *
 * @author hornm, dietzc University of Konstanz
 */
public class ImgLibConvolver<T extends RealType<T>, O extends RealType<O>, K extends RealType<K>, OUT extends RandomAccessibleInterval<O>, KERNEL extends RandomAccessibleInterval<K>>
                implements Convolver<T, O, K, Img<T>, OUT, KERNEL> {

        protected KERNEL m_kernel;

        private Img<T> m_lastImg = null;

        private FFTConvolution<T, O, K> m_fc = null;

        // Empty constructor for extension point
        public ImgLibConvolver() {
        }

        public ImgLibConvolver(final KERNEL kernel) {
                setKernel(kernel);
        }

        @Override
        public final void setKernel(final KERNEL kernel) {
                m_kernel = kernel;
        }

        @Override
        public ImgLibConvolver<T, O, K, OUT, KERNEL> copy() {
                return new ImgLibConvolver<T, O, K, OUT, KERNEL>(m_kernel);
        }

        @Override
        public OUT compute(final Img<T> in, final OUT out) {

                if (m_kernel == null) {
                        throw new IllegalStateException(
                                        "Kernel in ImgLibImageConvolution may not be null");
                }

                if (m_lastImg != in) {
                        m_lastImg = in;
                        m_fc = new FFTConvolution<T, O, K>(m_lastImg, m_kernel,
                                        out);
                        m_fc.setKeepImgFFT(true);
                } else {
                        m_fc.setKernel(m_kernel);
                }

                m_fc.run();

                return out;
        }

}
