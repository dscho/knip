package org.kniplib.tools;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math.linear.AbstractRealMatrix;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public class ImgBasedRealMatrix<T extends RealType<T>, IN extends RandomAccessibleInterval<T>>
                extends AbstractRealMatrix {

        private final RandomAccess<T> m_rndAccess;
        private final IN m_img;
        private final int m_rowIdx;
        private final int m_colIdx;

        public ImgBasedRealMatrix(IN img, int colIdx, int rowIdx) {
                if (img.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "Img must have exact to dimensions to be handled as a matrix");
                }
                m_img = img;
                m_rndAccess = img.randomAccess();
                m_rowIdx = rowIdx;
                m_colIdx = colIdx;
        }

        @Override
        public RealMatrix createMatrix(int rowDimension, int columnDimension) {
                return new Array2DRowRealMatrix(rowDimension, columnDimension);
        }

        @Override
        public RealMatrix copy() {
                throw new UnsupportedOperationException("Copy Unsupported");
                // return new ImgBasedRealMatrix<T>(m_img.copy());
        }

        @Override
        public double getEntry(int row, int column) {
                m_rndAccess.setPosition(row, m_rowIdx);
                m_rndAccess.setPosition(column, m_colIdx);

                return m_rndAccess.get().getRealDouble();
        }

        @Override
        public void setEntry(int row, int column, double value) {
                m_rndAccess.setPosition(row, m_rowIdx);
                m_rndAccess.setPosition(column, m_colIdx);
                m_rndAccess.get().setReal(value);
        }

        @Override
        public int getRowDimension() {
                return (int) m_img.dimension(m_rowIdx);
        }

        @Override
        public int getColumnDimension() {
                return (int) m_img.dimension(m_colIdx);
        }

}
