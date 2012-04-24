/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
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
 *   30.03.2006 (Tom): created
 */
package org.kniplib.data.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Helper Class holds a Coocurencematrix and has some acces methods and
 * operation on the matrix.
 * 
 * @author Tom, University of Konstanz
 */
public class CoocurenceMatrix {

        /*
         * Node Logger of this class.
         */
        private static final Logger LOGGER = LoggerFactory
                        .getLogger(CoocurenceMatrix.class);

        /*
         * Number of gray levels
         */
        private int m_nrGrayLevels;

        private double[][] m_matrix = null;

        private int m_nrOfPixelPairs;

        private double[] m_pX = null;

        private double[] m_pY = null;

        private double m_sigmaX = Double.NaN;

        private double m_sigmaY = Double.NaN;

        private double m_mean = Double.NaN;

        private double m_meanX = Double.NaN;

        private double m_meanY = Double.NaN;

        private double m_hxy = Double.NaN;

        private double m_hxy1 = Double.NaN;

        private double m_hx = Double.NaN;

        private double m_hy = Double.NaN;

        private double m_hxy2 = Double.NaN;

        private double[] m_pXminusY;

        private double[] m_pXplusY;

        /**
         * Constructor.
         * 
         * @param matrix
         *                - the matrix
         * @param nrGrayLevels
         *                the number of gray levels used.
         */
        public CoocurenceMatrix(final double[][] matrix, final int nrGrayLevels) {
                super();
                this.m_matrix = matrix;
                this.m_nrGrayLevels = nrGrayLevels;
                m_pXminusY = new double[m_nrGrayLevels];
                m_pXplusY = new double[2 * m_nrGrayLevels];

                // init arrays
                for (int i = 0; i < m_nrGrayLevels; i++) {
                        m_pXminusY[i] = Double.NaN;
                        m_pXplusY[i] = Double.NaN;
                }
                for (int i = m_nrGrayLevels - 1; i < matrix.length; i++) {
                        m_pXplusY[i + m_nrGrayLevels] = Double.NaN;
                }

        }

        /**
         * returns the value at row, col of the coorurence matrix.
         * 
         * @param row
         *                - the row
         * @param col
         *                - the column
         * @return value at that position
         */
        public final double getValueAt(final int row, final int col) {
                return m_matrix[row][col];
        }

        /**
         * @param p
         *                - position in the array
         * @return Returns the PX.
         */
        public final double getPX(final int p) {
                if (m_pX == null || Double.isNaN(m_pX[p])) {
                        m_pX = new double[m_nrGrayLevels];
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = 0; j < m_nrGrayLevels; j++) {
                                        m_pX[i] += m_matrix[i][j];
                                }
                        }
                }
                return m_pX[p];
        }

        /**
         * @param p
         *                - position in the array
         * @return Returns the PY.
         */
        public final double getPY(final int p) {
                if (m_pY == null || Double.isNaN(m_pY[p])) {
                        m_pY = new double[m_nrGrayLevels];
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = 0; j < m_nrGrayLevels; j++) {
                                        m_pY[i] += m_matrix[j][i];
                                }
                        }
                }
                return m_pY[p];
        }

        /**
         * @return sigmaX of the matrix
         */
        public final double getSigmaX() {
                if (Double.isNaN(m_sigmaX)) {
                        double sigmaX = 0.0;
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        sigmaX += getValueAt(i, j)
                                                        * (i - getMeanX())
                                                        * (i - getMeanX());
                                }
                        }
                        m_sigmaX = sigmaX;
                }
                return m_sigmaX;
        }

        /**
         * @return sigmaY of the matrix
         */
        public final double getSigmaY() {
                if (Double.isNaN(m_sigmaY)) {
                        double sigmaY = 0.0;
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        sigmaY += getValueAt(i, j)
                                                        * (i - getMeanY())
                                                        * (i - getMeanY());
                                }
                        }
                        m_sigmaY = sigmaY;
                }
                return m_sigmaY;
        }

        /**
         * @return Returns the y mean.
         */
        public final double getMeanY() {
                if (Double.isNaN(m_meanY)) {
                        double mean = 0.0;
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        mean += j * getValueAt(i, j);
                                }
                        }
                        m_meanY = mean;
                }
                return m_meanY;
        }

        /**
         * @return returns the x mean
         */
        public final double getMeanX() {
                if (Double.isNaN(m_meanX)) {
                        double mean = 0.0;
                        for (int i = 0; i < m_nrGrayLevels; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        mean += i * getValueAt(i, j);
                                }
                        }
                        m_meanX = mean;
                }
                return m_meanX;
        }

        /**
         * calculates the mean of all matrixfields.
         * 
         * @return mean
         */
        public final double getMean() {
                if (Double.isNaN(m_mean)) {
                        m_mean = 0.0;
                        for (int i = 0; i < m_matrix.length; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        m_mean += m_matrix[i][j];
                                }
                        }
                        m_mean /= (m_nrGrayLevels * m_nrGrayLevels);
                }
                return m_mean;
        }

        /**
         * @return Returns the nrOfPixelPairs.
         */
        public final int getNrOfPixelPairs() {
                return m_nrOfPixelPairs;
        }

        /**
         * @param nrOfPixelPairs
         *                The nrOfPixelPairs to set.
         */
        public final void setNrOfPixelPairs(final int nrOfPixelPairs) {
                this.m_nrOfPixelPairs = nrOfPixelPairs;
        }

        /**
         * @return the matrix
         */
        public final double[][] getMatrix() {
                return m_matrix;
        }

        /**
         * @return HXY
         */
        public final double getHXY() {
                if (Double.isNaN(m_hxy)) {
                        double hxy = 0.0;
                        for (int i = 0; i < m_matrix.length; i++) {
                                for (int j = i; j < m_matrix.length; j++) {
                                        if (i == j) {
                                                hxy -= getValueAt(i, j)
                                                                * myLog(getValueAt(
                                                                                i,
                                                                                j));
                                        } else {
                                                hxy -= 2
                                                                * getValueAt(i,
                                                                                j)
                                                                * myLog(getValueAt(
                                                                                i,
                                                                                j));
                                        }
                                }
                        }
                        m_hxy = hxy;
                }
                return m_hxy;
        }

        /**
         * @return HXY1
         */
        public final double getHXY1() {
                if (Double.isNaN(m_hxy1)) {
                        double hxy1 = 0.0;
                        for (int i = 0; i < m_matrix.length; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        hxy1 -= getValueAt(i, j)
                                                        * myLog(getPX(i)
                                                                        * getPY(j));
                                }
                        }
                        m_hxy1 = hxy1;
                }
                return m_hxy1;
        }

        /**
         * @return HXY2
         */
        public final double getHXY2() {
                if (Double.isNaN(m_hxy2)) {
                        double hxy2 = 0.0;
                        for (int i = 0; i < m_matrix.length; i++) {
                                for (int j = 0; j < m_matrix.length; j++) {
                                        hxy2 -= getPX(i)
                                                        * getPY(j)
                                                        * myLog(getPX(i)
                                                                        * getPY(j));
                                }
                        }
                        m_hxy2 = hxy2;
                }
                return m_hxy2;
        }

        /**
         * @return HX
         */
        public final double getHX() {
                if (Double.isNaN(m_hx)) {
                        double hx = 0.0;
                        for (int i = 0; i < m_matrix.length; i++) {
                                hx -= getPX(i) * myLog(getPX(i));
                        }
                        m_hx = hx;
                }
                return m_hx;
        }

        /**
         * @return HY
         */
        public final double getHY() {
                if (Double.isNaN(m_hy)) {
                        double hy = 0.0;
                        for (int j = 0; j < m_matrix.length; j++) {
                                hy -= getPY(j) * myLog(getPY(j));
                        }
                        m_hy = hy;
                }
                return m_hy;
        }

        /**
         * @param value
         * @return returns ln(value) if value != 0, else 0
         */
        private final double myLog(final double value) {
                if (value != 0) {
                        return Math.log(value);
                }
                return 0;
        }

        /**
         * @param k
         *                - Difference
         * @return result of p_(x-y)
         */
        public final double getPXminusY(final int k) {
                if (Double.isNaN(m_pXminusY[k])) {
                        double pXminusY = 0.0;
                        for (int i = 1; i < m_nrGrayLevels; i++) {
                                for (int j = i; j < m_nrGrayLevels; j++) {
                                        if (Math.abs(i - j) == k) {
                                                if (i == j) {
                                                        pXminusY += getValueAt(
                                                                        i, j);
                                                } else {
                                                        pXminusY += 2 * getValueAt(
                                                                        i, j);

                                                }
                                        }
                                }
                        }
                        m_pXminusY[k] = pXminusY;
                }
                return m_pXminusY[k];
        }

        /**
         * @param k
         *                - Comperator
         * @return result of p_(x-y)
         */
        public final double getPXplusY(final int k) {
                if (Double.isNaN(m_pXplusY[k])) {
                        double pXplusY = 0.0;
                        for (int i = 1; i < m_nrGrayLevels; i++) {
                                for (int j = i; j < m_nrGrayLevels; j++) {
                                        if (i + j == k) {
                                                if (i == j) {
                                                        pXplusY += getValueAt(
                                                                        i, j);
                                                } else {
                                                        pXplusY += 2 * getValueAt(
                                                                        i, j);

                                                }
                                        }
                                }
                        }
                        m_pXplusY[k] = pXplusY;
                }
                return m_pXplusY[k];
        }

        /**
         * {@inheritDoc}
         */
        public final String toString() {
                StringBuffer b = new StringBuffer();
                for (int i = 0; i < m_matrix.length; i++) {
                        for (int j = 0; j < m_matrix.length; j++) {
                                b.append("[ ");
                                b.append(m_matrix[i][j]);
                                b.append(" ] ");
                        }
                        b.append("\n");
                }
                return b.toString();
        }

        /**
   * 
   */
        public final void printMatrixInfo() {
                for (int i = 0; i < m_matrix.length; i++) {
                        for (int j = 0; j < m_matrix.length; j++) {
                                if (m_matrix[i][j] != 0) {
                                        LOGGER.info(i + ":" + j + " | "
                                                        + m_matrix[i][j]
                                                        * m_nrOfPixelPairs);
                                }
                        }
                }
        }

}
