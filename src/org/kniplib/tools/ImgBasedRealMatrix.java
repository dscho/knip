package org.kniplib.tools;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.apache.commons.math.linear.AbstractRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

public class ImgBasedRealMatrix<T extends RealType<T>> extends
                AbstractRealMatrix {

        private final RandomAccess<T> m_rndAccess;
        private final Img<T> m_img;

        public ImgBasedRealMatrix(Img<T> img) {
                if (img.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "Img must have exact to dimensions to be handled as a matrix");
                }
                m_img = img;
                m_rndAccess = img.randomAccess();
        }

        @Override
        public RealMatrix createMatrix(int rowDimension, int columnDimension) {
                return new ImgBasedRealMatrix<T>(m_img.factory().create(
                                new int[] { rowDimension, columnDimension },
                                m_img.firstElement().createVariable()));
        }

        @Override
        public RealMatrix copy() {
                return new ImgBasedRealMatrix<T>(m_img.copy());
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
                return (int) m_img.dimension(0);
        }

        @Override
        public int getColumnDimension() {
                return (int) m_img.dimension(1);
        }

}
