package org.knime.knip.core.algorithm.logtrackmate;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.fft.FourierConvolution;
import net.imglib2.algorithm.math.PickImagePeaks;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;

import org.knime.knip.core.algorithm.logtrackmate.SubPixelLocalization.LocationType;

public class LoGDetectorOp<T extends RealType<T> & NativeType<T>> implements
        UnaryOutputOperation<ImgPlus<T>, ImgPlus<BitType>> {

    private String baseErrorMessage = "LogDetector: ";

    private double radius;

    private double threshold;

    private boolean doSubPixelLocalization;

    protected List<Spot> spots = new ArrayList<Spot>();

    private int dimension;

    public LoGDetectorOp(final double radius, final double threshold, final boolean doSubPixelLocalization) {
        this.radius = radius;
        this.threshold = threshold;
        this.doSubPixelLocalization = doSubPixelLocalization;
    }

    @Override
    public ImgPlus<BitType> compute(final ImgPlus<T> input, final ImgPlus<BitType> output) {

        dimension = input.numDimensions();

        Img<T> temp = input;

        double sigma = radius / Math.sqrt(dimension);
        // Turn it in pixel coordinates
        final double[] calibration = new double[input.numDimensions()];
        input.calibration(calibration);

        double[] sigmas = new double[dimension];
        for (int i = 0; i < sigmas.length; i++) {
            sigmas[i] = sigma / calibration[i];
        }

        ImgFactory<FloatType> factory = new ArrayImgFactory<FloatType>();
        Img<FloatType> gaussianKernel = FourierConvolution.createGaussianKernel(factory, sigmas);
        FourierConvolution<T, FloatType> fConvGauss;
        fConvGauss =
                new FourierConvolution<T, FloatType>(temp, gaussianKernel, new ArrayImgFactory<ComplexFloatType>());
        if (!fConvGauss.checkInput() || !fConvGauss.process()) {
            baseErrorMessage += "Fourier convolution with Gaussian failed:\n" + fConvGauss.getErrorMessage();
        }
        temp = fConvGauss.getResult();

        Img<FloatType> laplacianKernel = createLaplacianKernel();
        FourierConvolution<T, FloatType> fConvLaplacian;
        fConvLaplacian =
                new FourierConvolution<T, FloatType>(temp, laplacianKernel, new ArrayImgFactory<ComplexFloatType>());
        if (!fConvLaplacian.checkInput() || !fConvLaplacian.process()) {
            baseErrorMessage += "Fourier Convolution with Laplacian failed:\n" + fConvLaplacian.getErrorMessage();
            return null;
        }
        temp = fConvLaplacian.getResult();

        PickImagePeaks<T> peakPicker = new PickImagePeaks<T>(temp);
        double[] suppressionRadiuses = new double[input.numDimensions()];
        for (int i = 0; i < dimension; i++) {
            suppressionRadiuses[i] = radius / calibration[i];
        }
        peakPicker.setSuppression(suppressionRadiuses); // in pixels
        peakPicker.setAllowBorderPeak(true);

        if (!peakPicker.checkInput() || !peakPicker.process()) {
            baseErrorMessage += "Could not run the peak picker algorithm:\n" + peakPicker.getErrorMessage();
            return null;
        }

        // Get peaks location and values
        final ArrayList<long[]> centers = peakPicker.getPeakList();
        final RandomAccess<T> cursor = temp.randomAccess();
        // Prune values lower than threshold
        List<SubPixelLocalization<T>> peaks = new ArrayList<SubPixelLocalization<T>>();
        final List<T> pruned_values = new ArrayList<T>();
        final LocationType specialPoint = LocationType.MAX;
        for (int i = 0; i < centers.size(); i++) {
            long[] center = centers.get(i);
            cursor.setPosition(center);
            T value = cursor.get().copy();
            if (value.getRealDouble() < threshold) {
                break; // because peaks are sorted, we can exit loop here
            }
            SubPixelLocalization<T> peak = new SubPixelLocalization<T>(center, value, specialPoint);
            peaks.add(peak);
            pruned_values.add(value);
        }

        // Do sub-pixel localization
        if (doSubPixelLocalization) {
            // Create localizer and apply it to the list. The list object will
            // be updated
            final QuadraticSubpixelLocalization<T> locator = new QuadraticSubpixelLocalization<T>(temp, peaks);
            locator.setNumThreads(1); // Since the calls to a detector are
                                      // already multi-threaded.
            locator.setCanMoveOutside(true);
            if (!locator.checkInput() || !locator.process()) {
                baseErrorMessage += locator.getErrorMessage();
                return null;
            }
        }

        // Create spots
        spots.clear();
        RandomAccess<BitType> randomAccess = output.randomAccess();
        for (int j = 0; j < peaks.size(); j++) {

            SubPixelLocalization<T> peak = peaks.get(j);
            double[] coords = new double[3];
            for (int i = 0; i < dimension; i++) {
                coords[i] = peak.getDoublePosition(i) * calibration[i];
                randomAccess.setPosition((long)coords[i], i);
            }

            randomAccess.get().set(true);

        }

        return output;

    }

    @Override
    public UnaryObjectFactory<ImgPlus<T>, ImgPlus<BitType>> bufferFactory() {
        return new UnaryObjectFactory<ImgPlus<T>, ImgPlus<BitType>>() {

            @Override
            public ImgPlus<BitType> instantiate(final ImgPlus<T> img) {
                try {
                    return new ImgPlus<BitType>(img.factory().imgFactory(new BitType()).create(img, new BitType()), img);
                } catch (IncompatibleTypeException e) {
                    return new ImgPlus<BitType>(new ArrayImgFactory<BitType>().create(img, new BitType()), img);
                }

            }
        };
    }

    /*
     * PRIVATE METHODS
     */
    private Img<FloatType> createLaplacianKernel() {
        final ImgFactory<FloatType> factory = new ArrayImgFactory<FloatType>();
        int numDim = dimension;
        Img<FloatType> laplacianKernel = null;
        if (numDim == 3) {
            final float laplacianArray[][][] =
                    new float[][][]{{{0, -1 / 18, 0}, {-1 / 18, -1 / 18, -1 / 18}, {0, -1 / 18, 0}},
                            {{-1 / 18, -1 / 18, -1 / 18}, {-1 / 18, 1, -1 / 18}, {-1 / 18, -1 / 18, -1 / 18}},
                            {{0, -1 / 18, 0}, {-1 / 18, -1 / 18, -1 / 18}, {0, -1 / 18, 0}}}; // laplace kernel found here:
                                                                                              // http://en.wikipedia.org/wiki/Discrete_Laplace_operator
            laplacianKernel = factory.create(new int[]{3, 3, 3}, new FloatType());
            quickKernel3D(laplacianArray, laplacianKernel);
        } else if (numDim == 2) {
            final float laplacianArray[][] =
                    new float[][]{{-1 / 8, -1 / 8, -1 / 8}, {-1 / 8, 1, -1 / 8}, {-1 / 8, -1 / 8, -1 / 8}}; // laplace kernel found here:
                                                                                                            // http://en.wikipedia.org/wiki/Discrete_Laplace_operator
            laplacianKernel = factory.create(new int[]{3, 3}, new FloatType());
            quickKernel2D(laplacianArray, laplacianKernel);
        }
        return laplacianKernel;
    }

    private static void quickKernel2D(final float[][] vals, final Img<FloatType> kern) {
        final RandomAccess<FloatType> cursor = kern.randomAccess();
        final int[] pos = new int[2];

        for (int i = 0; i < vals.length; ++i) {
            for (int j = 0; j < vals[i].length; ++j) {
                pos[0] = i;
                pos[1] = j;
                cursor.setPosition(pos);
                cursor.get().set(vals[i][j]);
            }
        }
    }

    private static void quickKernel3D(final float[][][] vals, final Img<FloatType> kern) {
        final RandomAccess<FloatType> cursor = kern.randomAccess();
        final int[] pos = new int[3];

        for (int i = 0; i < vals.length; ++i) {
            for (int j = 0; j < vals[i].length; ++j) {
                for (int k = 0; k < vals[j].length; ++k) {
                    pos[0] = i;
                    pos[1] = j;
                    pos[2] = k;
                    cursor.setPosition(pos);
                    cursor.get().set(vals[i][j][k]);
                }
            }
        }
    }

    public List<Spot> getSpots() {
        return spots;
    }

    @Override
    public UnaryOutputOperation<ImgPlus<T>, ImgPlus<BitType>> copy() {
        return new LoGDetectorOp<T>(radius, threshold, doSubPixelLocalization);
    }

}
