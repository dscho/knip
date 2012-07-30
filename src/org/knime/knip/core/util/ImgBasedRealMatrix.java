package org.knime.knip.core.util;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math3.linear.AbstractRealMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class ImgBasedRealMatrix<T extends RealType<T>, IN extends RandomAccessibleInterval<T>>
                extends AbstractRealMatrix {

        private final RandomAccess<T> m_rndAccess;
        private final IN m_in;

        public ImgBasedRealMatrix(IN in) {
                if (in.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "In must have exact two dimensions to be handled as a matrix");
                }
                m_in = in;
                m_rndAccess = in.randomAccess();
        }

        @Override
        public RealMatrix createMatrix(int rowDimension, int columnDimension) {
                return new Array2DRowRealMatrix(rowDimension, columnDimension);
        }

        @Override
        public RealMatrix copy() {
                throw new UnsupportedOperationException("Unsupported");
        }

        @Override
        public double getEntry(int row, int column) {
                m_rndAccess.setPosition(row, 1);
                m_rndAccess.setPosition(column, 0);

                return m_rndAccess.get().getRealDouble();
        }

        @Override
        public void setEntry(int row, int column, double value) {
                m_rndAccess.setPosition(row, 1);
                m_rndAccess.setPosition(column, 0);
                m_rndAccess.get().setReal(value);
        }

        @Override
        public int getRowDimension() {
                return (int) m_in.dimension(0);
        }

        @Override
        public int getColumnDimension() {
                return (int) m_in.dimension(1);
        }

}