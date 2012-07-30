package org.knime.knip.core.ops.convolution;

import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFT;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

/* Test for different input types*/
public class FFTConvolution<R extends RealType<R>, O extends RealType<O>, K extends RealType<K>>
                implements Runnable {
        Img<ComplexFloatType> fftImg, fftKernel;
        ImgFactory<ComplexFloatType> fftFactory;

        RandomAccessible<R> img;
        RandomAccessible<K> kernel;
        Interval imgInterval, kernelInterval;
        RandomAccessibleInterval<O> output;

        boolean keepImgFFT = false;

        /**
         * Compute a Fourier space based convolution The image will be extended
         * by mirroring with single boundary, the kernel will be zero-padded.
         * The {@link ImgFactory} for creating the FFT will be identical to the
         * one used by the 'img' if possible, otherwise an
         * {@link ArrayImgFactory} or {@link CellImgFactory} depending on the
         * size.
         *
         * @param img
         *                - the image
         * @param kernel
         *                - the convolution kernel
         * @param output
         *                - the result of the convolution
         */
        public FFTConvolution(final Img<R> img,
                        final RandomAccessibleInterval<K> kernel,
                        final RandomAccessibleInterval<O> output) {
                this(img, kernel, output, getFFTFactory(img));
        }

        /**
         * Compute a Fourier space based convolution The image will be extended
         * by mirroring with single boundary, the kernel will be zero-padded.
         *
         * @param img
         *                - the image
         * @param kernel
         *                - the convolution kernel
         * @param output
         *                - the output
         * @param factory
         *                - the {@link ImgFactory} to create the fourier
         *                transforms
         */
        public FFTConvolution(final RandomAccessibleInterval<R> img,
                        final RandomAccessibleInterval<K> kernel,
                        final RandomAccessibleInterval<O> output,
                        final ImgFactory<ComplexFloatType> factory) {
                this(Views.extendMirrorSingle(img), img, Views.extendValue(
                                kernel, Util.getTypeFromInterval(kernel)
                                                .createVariable()), kernel,
                                output, factory);
        }

        /**
         * Compute a Fourier space based convolution. The input as well as the
         * kernel need to be extended or infinite already as the
         * {@link Interval} required to perform the Fourier convolution is
         * significantly bigger than the {@link Interval} provided here.
         *
         * Interval size of img and kernel: size(img) + 2*(size(kernel)-1) + pad
         * to fft compatible size
         *
         * @param img
         *                - the input
         * @param imgInterval
         *                - the input interval (i.e. the area to be convolved)
         * @param kernel
         *                - the kernel
         * @param kernelInterval
         *                - the kernel interval
         * @param output
         *                - the output data+interval
         * @param factory
         *                - the {@link ImgFactory} to create the fourier
         *                transforms
         */
        public FFTConvolution(final RandomAccessible<R> img,
                        final Interval imgInterval,
                        final RandomAccessible<K> kernel,
                        final Interval kernelInterval,
                        final RandomAccessibleInterval<O> output,
                        final ImgFactory<ComplexFloatType> factory) {
                this.img = img;
                this.imgInterval = imgInterval;
                this.kernel = kernel;
                this.kernelInterval = kernelInterval;
                this.output = output;
                this.fftFactory = factory;
        }

        public void setImg(final RandomAccessibleInterval<R> img) {
                this.img = img;
                this.imgInterval = img;
                this.fftImg = null;
        }

        public void setImg(final RandomAccessible<R> img,
                        final Interval imgInterval) {
                this.img = img;
                this.imgInterval = imgInterval;
                this.fftImg = null;
        }

        public void setKernel(final RandomAccessibleInterval<K> kernel) {
                this.kernel = kernel;
                this.kernelInterval = kernel;
                this.fftKernel = null;
        }

        public void setKernel(final RandomAccessible<K> kernel,
                        final Interval kernelInterval) {
                this.kernel = kernel;
                this.kernelInterval = kernelInterval;
                this.fftKernel = null;
        }

        public void setOutput(final RandomAccessibleInterval<O> output) {
                this.output = output;
        }

        public void setKeepImgFFT(final boolean keep) {
                this.keepImgFFT = true;
        }

        public boolean keepImgFFT() {
                return keepImgFFT;
        }

        public void setFFTImgFactory(final ImgFactory<ComplexFloatType> factory) {
                this.fftFactory = factory;
        }

        public ImgFactory<ComplexFloatType> fftImgFactory() {
                return fftFactory;
        }

        public Img<ComplexFloatType> imgFFT() {
                return fftImg;
        }

        public Img<ComplexFloatType> kernelFFT() {
                return fftKernel;
        }

        @Override
        public void run() {
                final int numDimensions = imgInterval.numDimensions();

                // the image has to be extended at least by kernelDimensions/2-1
                // in each dimension so that
                // the pixels outside of the interval are used for the
                // convolution.
                final long[] newDimensions = new long[numDimensions];

                for (int d = 0; d < numDimensions; ++d)
                        newDimensions[d] = (int) imgInterval.dimension(d)
                                        + (int) kernelInterval.dimension(d) - 1;

                // compute the size of the complex-valued output and the
                // required padding
                // based on the prior extended input image
                final long[] paddedDimensions = new long[numDimensions];
                final long[] fftDimensions = new long[numDimensions];

                FFTMethods.dimensionsRealToComplexFast(
                                FinalDimensions.wrap(newDimensions),
                                paddedDimensions, fftDimensions);

                // compute the new interval for the input image
                final Interval imgConvolutionInterval = FFTMethods
                                .paddingIntervalCentered(
                                                imgInterval,
                                                FinalDimensions.wrap(paddedDimensions));

                // compute the new interval for the kernel image
                final Interval kernelConvolutionInterval = FFTMethods
                                .paddingIntervalCentered(
                                                kernelInterval,
                                                FinalDimensions.wrap(paddedDimensions));

                // compute where to place the final Interval for the kernel so
                // that the coordinate in the center
                // of the kernel is at position (0,0)
                final long[] min = new long[numDimensions];
                final long[] max = new long[numDimensions];

                for (int d = 0; d < numDimensions; ++d) {
                        min[d] = kernelInterval.min(d)
                                        + kernelInterval.dimension(d) / 2;
                        max[d] = min[d]
                                        + kernelConvolutionInterval
                                                        .dimension(d) - 1;
                }

                // assemble the correct kernel (size of the input + extended
                // periodic + top left at center of input kernel)
                final RandomAccessibleInterval<K> kernelInput = Views.interval(
                                Views.extendPeriodic(Views.interval(kernel,
                                                kernelConvolutionInterval)),
                                new FinalInterval(min, max));
                final RandomAccessibleInterval<R> imgInput = Views.interval(
                                img, imgConvolutionInterval);

                // compute the FFT's if they do not exist yet
                if (fftImg == null)
                        fftImg = FFT.realToComplex(imgInput, fftFactory);

                if (fftKernel == null)
                        fftKernel = FFT.realToComplex(kernelInput, fftFactory);

                final Img<ComplexFloatType> fftconvolved;

                if (keepImgFFT)
                        fftconvolved = fftImg.copy();
                else
                        fftconvolved = fftImg;

                // multiply in place
                multiplyComplex(fftconvolved, fftKernel);

                // inverse FFT in place
                FFT.complexToRealUnpad(fftconvolved, output);
        }

        final public static <R extends RealType<R>> void convolve(
                        final RandomAccessible<R> img,
                        final Interval imgInterval,
                        final RandomAccessible<R> kernel,
                        final Interval kernelInterval,
                        final RandomAccessibleInterval<R> output,
                        final ImgFactory<ComplexFloatType> factory) {
                final int numDimensions = imgInterval.numDimensions();

                // the image has to be extended at least by kernelDimensions/2-1
                // in each dimension so that
                // the pixels outside of the interval are used for the
                // convolution.
                final long[] newDimensions = new long[numDimensions];

                for (int d = 0; d < numDimensions; ++d)
                        newDimensions[d] = (int) imgInterval.dimension(d)
                                        + (int) kernelInterval.dimension(d) - 1;

                // compute the size of the complex-valued output and the
                // required padding
                // based on the prior extended input image
                final long[] paddedDimensions = new long[numDimensions];
                final long[] fftDimensions = new long[numDimensions];

                FFTMethods.dimensionsRealToComplexFast(
                                FinalDimensions.wrap(newDimensions),
                                paddedDimensions, fftDimensions);

                // compute the new interval for the input image
                final Interval imgConvolutionInterval = FFTMethods
                                .paddingIntervalCentered(
                                                imgInterval,
                                                FinalDimensions.wrap(paddedDimensions));

                // compute the new interval for the kernel image
                final Interval kernelConvolutionInterval = FFTMethods
                                .paddingIntervalCentered(
                                                kernelInterval,
                                                FinalDimensions.wrap(paddedDimensions));

                // compute where to place the final Interval for the kernel so
                // that the coordinate in the center
                // of the kernel is at position (0,0)
                long[] min = new long[numDimensions];
                long[] max = new long[numDimensions];

                for (int d = 0; d < numDimensions; ++d) {
                        min[d] = kernelInterval.min(d)
                                        + kernelInterval.dimension(d) / 2;
                        max[d] = min[d]
                                        + kernelConvolutionInterval
                                                        .dimension(d) - 1;
                }

                // assemble the correct kernel (size of the input + extended
                // periodic + top left at center of input kernel)
                final RandomAccessibleInterval<R> kernelInput = Views.interval(
                                Views.extendPeriodic(Views.interval(kernel,
                                                kernelConvolutionInterval)),
                                new FinalInterval(min, max));
                final RandomAccessibleInterval<R> imgInput = Views.interval(
                                img, imgConvolutionInterval);

                // compute the FFT's
                final Img<ComplexFloatType> fftImg = FFT.realToComplex(
                                imgInput, factory);
                final Img<ComplexFloatType> fftKernel = FFT.realToComplex(
                                kernelInput, factory);

                // multiply in place
                multiplyComplex(fftImg, fftKernel);

                // inverse FFT in place
                FFT.complexToRealUnpad(fftImg, output);
        }

        final public static void multiplyComplex(
                        final Img<ComplexFloatType> img,
                        final Img<ComplexFloatType> kernel) {
                final Cursor<ComplexFloatType> cursorA = img.cursor();
                final Cursor<ComplexFloatType> cursorB = kernel.cursor();

                while (cursorA.hasNext())
                        cursorA.next().mul(cursorB.next());
        }

        protected static ImgFactory<ComplexFloatType> getFFTFactory(
                        final Img<? extends RealType<?>> img) {
                try {
                        return img.factory().imgFactory(new ComplexFloatType());
                } catch (IncompatibleTypeException e) {
                        if (img.size() > Integer.MAX_VALUE / 2)
                                return new CellImgFactory<ComplexFloatType>(
                                                1024);
                        else
                                return new ArrayImgFactory<ComplexFloatType>();
                }
        }
}
