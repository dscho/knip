/*
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 *
 * History
 *   29.03.2005 (cebron): created
 */
package org.kniplib.data.statistics;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.kniplib.features.seg.MatrixOrientation;

/**
 * Second order statistics features provide information about the relative
 * positions of the various gray levels within the image. (Texture information)
 * To keep the computational complexity and memory space low, images are
 * converted to 8-bit.
 * 
 * TODO: Validation if this is right
 * 
 * @author dietzc, cebron, University of Konstanz
 */
public class SecondOrderStatistics<T extends RealType<T>> {

        /*
         * Matrix consisting of all pixel values of the image
         */
        private int[][] m_pixels;

        /*
         * distance between pixel pairs
         */
        private int m_distance = 1;

        /*
         * Number of gray levels
         */
        private int m_nrGrayLevels;

        /*
         * Co-occurence matrix 0 degree
         */
        private CoocurenceMatrix m_matrix;

        private double m_asm = Double.NaN;

        private double m_correlation = Double.NaN;

        private double m_variance = Double.NaN;

        private double m_idfm = Double.NaN;

        private double m_entropy = Double.NaN;

        private double m_contrast = Double.NaN;

        private double m_diffentropy = Double.NaN;

        private double m_sumavg = Double.NaN;

        private double m_sumentropy = Double.NaN;

        private double m_sumvariance = Double.NaN;

        private double m_diffvariance = Double.NaN;

        private double m_icm1 = Double.NaN;

        private double m_icm2 = Double.NaN;

        private double m_min;

        private double m_max;

        private double[][] m_matrixAsDouble;

        private int m_nrPixelPairs;

        private IterableInterval<T> m_iterableInterval;

        private Cursor<T> m_iteratableRoiCursor;

        private MatrixOrientation m_orientation;

        /**
         * @return Returns the m_antidiagonalCOM.
         */
        public CoocurenceMatrix getMatrix() {
                return m_matrix;
        }

        /**
         * Constructs a new SecondOrderStatistics object based on the given
         * image and bitmask.
         * <p>
         * With the given distance, four different matrices for the four
         * directions (horizontal, diagonal, vertical, antidiagonal) are
         * calculated.
         * </p>
         * <p>
         * The resulting values for the four directions are averaged out. This
         * makes the textural features rotation tolerant.
         * </p>
         * <p>
         * The calculation of the features is optimized according to the work
         * 'Fast Calculation of Haralick texture Features' by Miyamoto, Merryman
         * Jr. 2005
         * </p>
         * 
         * @param ip
         *                the image
         * @param distance
         *                relative distance measured in pixels
         * @param nrGrayLevels
         *                The number of gray levels to use. A smaller value will
         *                result in a faster computation but also in a loss of
         *                information.
         */
        public SecondOrderStatistics(final IterableInterval<T> mask,
                        final int nrGrayLevels, final int distance,
                        MatrixOrientation orientation) {
                m_distance = distance;
                m_nrGrayLevels = nrGrayLevels;
                m_orientation = orientation;
                // copy pixel values in own pixel array

                m_iterableInterval = mask;
                m_iteratableRoiCursor = m_iterableInterval.cursor();
                m_min = m_iteratableRoiCursor.get().getMinValue();
                m_max = m_iteratableRoiCursor.get().getMaxValue();

                m_matrixAsDouble = new double[m_nrGrayLevels][m_nrGrayLevels];

                updateROI(m_iterableInterval);
        }

        private void clearValues() {

                m_asm = Double.NaN;
                m_correlation = Double.NaN;
                m_variance = Double.NaN;
                m_idfm = Double.NaN;
                m_entropy = Double.NaN;
                m_contrast = Double.NaN;
                m_diffentropy = Double.NaN;
                m_sumavg = Double.NaN;
                m_sumentropy = Double.NaN;
                m_sumvariance = Double.NaN;
                m_diffvariance = Double.NaN;
                m_icm1 = Double.NaN;
                m_icm2 = Double.NaN;

        }

        /**
         * @param mask
         */
        public final void updateROI(final IterableInterval<T> mask) {
                m_pixels = new int[(int) mask.dimension(1)][(int) mask
                                .dimension(0)];

                for (int i = 0; i < m_pixels.length; i++) {
                        Arrays.fill(m_pixels[i], Integer.MAX_VALUE);
                }

                while (m_iteratableRoiCursor.hasNext()) {
                        m_iteratableRoiCursor.fwd();
                        m_pixels[m_iteratableRoiCursor.getIntPosition(1)
                                        - (int) mask.min(1)][m_iteratableRoiCursor
                                        .getIntPosition(0) - (int) mask.min(0)] = (int) (((m_iteratableRoiCursor
                                        .get().getRealDouble() - m_min) / (m_max
                                        - m_min + 1)) * m_nrGrayLevels);

                }
                clearValues();
                computeAllMatrices();
        }

        private final void computeAllMatrices() {

                if (m_pixels == null) {
                        throw new IllegalStateException(
                                        "No Segment set in Second order statistics. Make sure you called updateSegmentInformation");
                }

                m_nrPixelPairs = 0;

                for (int g = 0; g < m_nrGrayLevels; g++) {

                        Arrays.fill(m_matrixAsDouble[g], 0);

                }

                for (int y = 0; y < m_pixels.length; y++) {
                        for (int x = 0; x < m_pixels[0].length; x++) {
                                // only consider pixel-pairs that are within the
                                // mask
                                if (m_pixels[y][x] != Integer.MAX_VALUE) {

                                        switch (m_orientation) {
                                        case ANTIDIAGONAL:
                                                if (x - m_distance >= 0
                                                                && y
                                                                                - m_distance >= 0) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y
                                                                        - m_distance][x
                                                                        - m_distance] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y
                                                                                - m_distance][x
                                                                                - m_distance]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }
                                                // is second pixel in range of
                                                // the image?
                                                if (x + m_distance < m_pixels[0].length
                                                                && y
                                                                                + m_distance < m_pixels.length) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y
                                                                        + m_distance][x
                                                                        + m_distance] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y
                                                                                + m_distance][x
                                                                                + m_distance]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }
                                                break;
                                        case DIAGONAL:
                                                if (x + m_distance < m_pixels[0].length
                                                                && y
                                                                                - m_distance >= 0) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y
                                                                        - m_distance][x
                                                                        + m_distance] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y
                                                                                - m_distance][x
                                                                                + m_distance]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }
                                                // is second pixel in range of
                                                // the image?
                                                if (x - m_distance >= 0
                                                                && y
                                                                                + m_distance < m_pixels.length) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y
                                                                        + m_distance][x
                                                                        - m_distance] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y
                                                                                + m_distance][x
                                                                                - m_distance]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }

                                                break;
                                        case HORIZONTAL:
                                                // is second pixel in range of
                                                // the image?
                                                if (x + m_distance < m_pixels[0].length) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y][x
                                                                        + m_distance] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y][x
                                                                                + m_distance]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }
                                                // is second pixel in range of
                                                // the image?
                                                if (x - m_distance >= 0) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y][x
                                                                        - m_distance] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y][x
                                                                                - m_distance]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }

                                                break;
                                        case VERTICAL:
                                                if (y - m_distance >= 0) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y
                                                                        - m_distance][x] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y
                                                                                - m_distance][x]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }
                                                // is second pixel in range of
                                                // the image?
                                                if (y + m_distance < m_pixels.length) {
                                                        // is second pixel in
                                                        // the mask ?
                                                        if (m_pixels[y
                                                                        + m_distance][x] != Integer.MAX_VALUE) {
                                                                m_matrixAsDouble[m_pixels[y][x]][m_pixels[y
                                                                                + m_distance][x]]++;
                                                                m_nrPixelPairs++;
                                                        }
                                                }

                                                break;

                                        }

                                }
                        }
                }

                // divide each coefficient with the number of total pairs
                for (int y = 0; y < m_matrixAsDouble.length; y++) {
                        for (int x = 0; x < m_matrixAsDouble[0].length; x++) {
                                if (m_nrPixelPairs != 0) {
                                        m_matrixAsDouble[y][x] /= m_nrPixelPairs;
                                }
                        }
                }
                m_matrix = new CoocurenceMatrix(m_matrixAsDouble,
                                m_nrGrayLevels);
                m_matrix.setNrOfPixelPairs(m_nrPixelPairs);

        }

        /**
         * The Angular Second Moment (ASM) is a measure of the smoothness of the
         * image. If all pixels are of the same gray level, then ASM = 1. The
         * less smooth the image is, the lower the ASM.
         * 
         * @return ASM value
         */
        public final double getASM() {
                if (Double.isNaN(m_asm)) {
                        m_asm = computeASM();
                }
                if (Double.isNaN(m_asm)) {
                        m_asm = 0;
                }
                return m_asm;
        }

        private double computeASM() {
                double asm = 0.0;
                for (int i = 0; i < m_nrGrayLevels; i++) {
                        for (int j = i; j < m_nrGrayLevels; j++) {
                                if (j == i) {
                                        asm += Math.pow(m_matrix.getValueAt(i,
                                                        j), 2);
                                } else {
                                        asm += 2 * Math.pow(m_matrix
                                                        .getValueAt(i, j), 2);
                                }
                        }
                }
                return (asm);
        }

        /**
         * 
         * @return correlation.
         */
        public final double getCorrelation() {
                if (Double.isNaN(m_correlation)) {
                        m_correlation = computeCorrelation();
                }
                if (Double.isNaN(m_correlation)) {
                        m_correlation = 0;
                }
                return m_correlation;
        }

        private double computeCorrelation() {
                double correlation = 0.0;

                for (int i = 0; i < m_nrGrayLevels; i++) {
                        for (int j = 0; j < m_nrGrayLevels; j++) {
                                // if (j == i) {
                                correlation += (m_matrix.getValueAt(i, j))
                                                * (i - m_matrix.getMeanX())
                                                * (j - m_matrix.getMeanY());

                        }
                }

                return correlation;
        }

        /**
         * 
         * @return variance.
         */
        public final double getVariance() {
                if (Double.isNaN(m_variance)) {
                        m_variance = computeVariance();
                }
                if (Double.isNaN(m_variance)) {
                        m_variance = 0;
                }
                return m_variance;
        }

        private double computeVariance() {
                double variance = 0.0;
                for (int i = 0; i < m_nrGrayLevels; i++) {
                        for (int j = 0; j < m_nrGrayLevels; j++) {
                                variance += (i - m_matrix.getMean())
                                                * (i - m_matrix.getMean())
                                                * m_matrix.getValueAt(i, j);

                        }
                }
                return variance;
        }

        /**
         * 
         * @return Information Measure of Correlation 1.
         */
        public final double getICM1() {
                if (Double.isNaN(m_icm1)) {
                        m_icm1 = computeICM1();
                }
                if (Double.isNaN(m_icm1)) {
                        m_icm1 = 0;
                }
                return m_icm1;
        }

        private double computeICM1() {

                return m_matrix.getHXY() - m_matrix.getHXY1()
                                / Math.max(m_matrix.getHX(), m_matrix.getHY());

        }

        /**
         * 
         * @return Information Measure of Correlation 2.
         */
        public final double getICM2() {
                if (Double.isNaN(m_icm2)) {
                        m_icm2 = computeICM2();
                }
                if (Double.isNaN(m_icm2)) {
                        m_icm2 = 0;
                }
                return m_icm2;
        }

        private double computeICM2() {

                return Math.sqrt(1 - Math.exp(-2
                                * Math.abs(m_matrix.getHXY2()
                                                - m_matrix.getHXY())));
        }

        /**
         * 
         * @return difference variance.
         */
        public final double getDifferenceVariance() {
                if (Double.isNaN(m_diffvariance)) {
                        m_diffvariance = computeDifferenceVariance();
                }
                if (Double.isNaN(m_diffvariance)) {
                        m_diffvariance = 0;
                }
                return m_diffvariance;
        }

        /**
         * TODO: Validation if this is right
         * 
         * @return
         */
        private double computeDifferenceVariance() {
                double diffvarianceMatrix = 0;
                double mean = 0;

                int z = 0;
                // compute mean of P(x-y)
                for (int n = 2; n <= 2 * m_nrGrayLevels; n++) {
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = i; j < m_nrGrayLevels; j++) {
                                        if (Math.abs(i - j) == n) {
                                                z++;
                                                mean += m_matrix.getValueAt(i,
                                                                j);

                                        }
                                }
                        }
                }
                mean /= z;

                // compute variance
                for (int n = 2; n <= 2 * m_nrGrayLevels; n++) {
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = i; j < m_nrGrayLevels; j++) {
                                        if (Math.abs(i - j) == n) {
                                                diffvarianceMatrix += (m_matrix
                                                                .getValueAt(i,
                                                                                j) - mean)
                                                                * (m_matrix.getValueAt(
                                                                                i,
                                                                                j) - mean);

                                        }
                                }
                        }
                }

                diffvarianceMatrix /= z;

                return diffvarianceMatrix;
        }

        /**
         * Image contrast is a measure of local gray level variations. Takes
         * high values for images of high contrast.
         * 
         * @return image contrast value
         */
        public final double getContrast() {
                if (Double.isNaN(m_contrast)) {
                        m_contrast = computeContrast();
                }
                if (Double.isNaN(m_contrast)) {
                        m_contrast = 0;
                }
                return m_contrast;
        }

        private double computeContrast() {
                double contrast = 0;
                for (int n = 0; n < m_nrGrayLevels; n++) {
                        int n2 = 2 * n * n;
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                // use the absolute property
                                for (int j = i; j < m_nrGrayLevels; j++) {
                                        if (Math.abs(i - j) == n) {
                                                // Contrast

                                                contrast += n2
                                                                * m_matrix.getValueAt(
                                                                                i,
                                                                                j);

                                        }
                                }
                        }
                }

                return (contrast);
        }

        /**
         * The Inverse Difference Moment (IDFM) takes high values for low
         * contrast images.
         * 
         * @return IDFM value
         */
        public final double getIDFM() {
                if (Double.isNaN(m_idfm)) {
                        m_idfm = computeIDFM();
                }
                if (Double.isNaN(m_idfm)) {
                        m_idfm = 0;
                }
                return m_idfm;
        }

        private double computeIDFM() {
                double idfm = 0.0;
                for (int i = 0; i < m_nrGrayLevels; i++) {
                        for (int j = i; j < m_nrGrayLevels; j++) {
                                double divisor = (1 + Math.pow(i - j, 2));
                                if (j == i) {
                                        idfm += m_matrix.getValueAt(i, j)
                                                        / divisor;

                                } else {
                                        idfm += 2 * m_matrix.getValueAt(i, j)
                                                        / divisor;

                                }
                        }
                }
                return idfm;
        }

        /**
         * Entropy is a measure of randomness and takes low values for smooth
         * images.
         * 
         * @return entropy value
         */
        public final double getEntropy() {
                if (Double.isNaN(m_entropy)) {
                        m_entropy = computeEntropy();
                }
                if (Double.isNaN(m_entropy)) {
                        m_entropy = 0;
                }
                return m_entropy;
        }

        private double computeEntropy() {
                double entropy = 0.0;
                for (int i = 0; i < m_nrGrayLevels; i++) {
                        for (int j = i; j < m_nrGrayLevels; j++) {
                                if (j == i) {
                                        entropy -= m_matrix.getValueAt(i, j)
                                                        * mylog(m_matrix.getValueAt(
                                                                        i, j));

                                } else {
                                        entropy -= 2
                                                        * m_matrix.getValueAt(
                                                                        i, j)
                                                        * mylog(m_matrix.getValueAt(
                                                                        i, j));

                                }
                        }
                }
                return entropy;
        }

        /**
         * @return Difference Entropy
         */
        public final double getDifferenceEntropy() {
                if (Double.isNaN(m_diffentropy)) {
                        m_diffentropy = computeDifferenceEntropy();
                }
                if (Double.isNaN(m_diffentropy)) {
                        m_diffentropy = 0;
                }
                return m_diffentropy;
        }

        private double computeDifferenceEntropy() {
                double diffentropy = 0.0;

                for (int n = 0; n < m_nrGrayLevels; n++) {
                        diffentropy -= m_matrix.getPXminusY(n)
                                        * mylog(m_matrix.getPXminusY(n));

                }

                return diffentropy;
        }

        /**
         * @return sum average
         */
        public final double getSumAverage() {
                if (Double.isNaN(m_sumavg)) {
                        m_sumavg = computeSumAverage();
                }
                if (Double.isNaN(m_sumavg)) {
                        m_sumavg = 0;
                }
                return m_sumavg;
        }

        private double computeSumAverage() {
                double sumavg = 0;
                for (int n = 2; n <= 2 * m_nrGrayLevels; n++) {
                        for (int i = 1; i < m_nrGrayLevels; i++) {
                                for (int j = i; j < m_nrGrayLevels; j++) {
                                        if (i + j == n) {
                                                if (i == j) {
                                                        sumavg += n
                                                                        * m_matrix.getValueAt(
                                                                                        i,
                                                                                        j);

                                                } else {
                                                        // Sum Average
                                                        sumavg += 2
                                                                        * n
                                                                        * m_matrix.getValueAt(
                                                                                        i,
                                                                                        j);

                                                }
                                        }
                                }
                        }
                }
                return sumavg;
        }

        /**
         * @return sum variance
         */
        public final double getSumVariance() {
                if (Double.isNaN(m_sumvariance)) {
                        m_sumvariance = computeSumVariance();
                }
                if (Double.isNaN(m_sumvariance)) {
                        m_sumvariance = 0;
                }
                return m_sumvariance;
        }

        private double computeSumVariance() {
                double sumvariance = 0.0;
                double sumentropy = getSumEntropy();
                if (Double.isNaN(m_sumvariance)) {
                        for (int n = 2; n <= 2 * m_nrGrayLevels; n++) {
                                for (int i = 1; i < m_nrGrayLevels; i++) {
                                        for (int j = i; j < m_nrGrayLevels; j++) {
                                                if (i + j == n) {
                                                        if (i == j) {
                                                                sumvariance += ((n - sumentropy) * (n - sumentropy))
                                                                                * m_matrix.getValueAt(
                                                                                                i,
                                                                                                j);

                                                        } else {
                                                                sumvariance += 2
                                                                                * ((n - sumentropy) * (n - sumentropy))
                                                                                * m_matrix.getValueAt(
                                                                                                i,
                                                                                                j);

                                                        }
                                                }
                                        }
                                }
                        }
                }
                return sumvariance;
        }

        /**
         * @return sum entropy
         */
        public final double getSumEntropy() {
                if (Double.isNaN(m_sumentropy)) {
                        m_sumentropy = computeSumEntropy();
                }
                if (Double.isNaN(m_sumentropy)) {
                        m_sumentropy = 0;
                }
                return m_sumentropy;
        }

        private double computeSumEntropy() {
                double sumentropy = 0.0;
                for (int n = 2; n < 2 * m_nrGrayLevels; n++) {
                        sumentropy -= m_matrix.getPXplusY(n)
                                        * mylog(m_matrix.getPXplusY(n));

                }
                return sumentropy;
        }

        /*
         * Since some of the values x may be zero, and log(0) is not defined,
         * log(0) = 0.
         */
        private static double mylog(final double d) {
                if (d != 0) {
                        return Math.log(d);
                }
                return 0;
        }

} // end SecondOrderStatistics
