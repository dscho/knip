package org.knime.knip.core.ops.img.algorithms;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * TODO: Verify the correctness of this implementation!! (e.g. compared to the
 * ImageJ plugin)
 *
 * @author hornm, University of Konstanz
 */
public class ColorDeconv<T extends RealType<T>, K extends RandomAccessibleInterval<T> & IterableInterval<T>>
                implements UnaryOperation<K, K> {

        private final int m_dimX;

        private final int m_dimY;

        private final int m_dimC;

        double[] m_Ms = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

        double[] m_M1 = { 1, 0, 0, 0, 0, 0, 0, 0, 0 };

        double[] m_M2 = { 0, 0, 0, 0, 1, 0, 0, 0, 0 };

        double[] m_M3 = { 0, 0, 0, 0, 0, 0, 0, 0, 1 };

        // // the deconvolution vectors chosen by the user, i.e. the column
        // // vectors for the destaining matrix
        // double[] m_odR = { 0.0, 0.0, 0.0 };
        // double[] m_odG = { 0.0, 0.0, 0.0 };
        // double[] m_odB = { 0.0, 0.0, 0.0 };

        // minimum and maximum of color channels in image
        double m_min, m_max, m_range;

        private double[][] m_stainVectors;

        /**
         * @param dimX
         * @param dimY
         * @param dimC
         * @param stainVectors
         *                maximal 3, minimal 1 vector
         */
        public ColorDeconv(int dimX, int dimY, int dimC,
                        double[]... stainVectors) {
                m_dimX = dimX;
                m_dimY = dimY;
                m_dimC = dimC;
                m_stainVectors = stainVectors;
                calcDeconvolutionVectors(stainVectors);

        }

        public ColorDeconv(int dimX, int dimY, int dimC, PredefinedStain stain) {
                this(dimX, dimY, dimC, stain.getVectors());
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public K compute(K in, K out) {

                // find the color range in the image
                T type = in.firstElement().createVariable();
                m_min = Math.abs(type.getMinValue());
                m_max = Math.abs(type.getMaxValue());
                m_range = m_min + m_max;

                // creating a new image of different type:

                RandomAccess<T> outRA = out.randomAccess();
                RandomAccess<T> srcRA = in.randomAccess();

                // stores the calculated optical densities of each pixel
                double fodI[] = new double[3];
                // stores the deconcolved optical densities
                double dcc[] = new double[3];

                // thee destaining matrix
                double[] Mds1 = computeDestainMatrix(1);
                double[] Mds2 = computeDestainMatrix(2);
                double[] Mds3 = computeDestainMatrix(3);

                // iterate through all pixels. note that we overwrite the
                // input-image
                // deconvolution START
                for (int y = 0; y < in.dimension(m_dimY); y++) {
                        srcRA.setPosition(y, 1);
                        for (int x = 0; x < in.dimension(m_dimX); x++) {
                                srcRA.setPosition(x, 0);

                                // read the rgb values of each pixel and convert
                                // them to their
                                // optical density, see the paper!
                                for (int i = 0; i < Math.min(3,
                                                in.dimension(m_dimC)); i++) {
                                        srcRA.setPosition(i, m_dimC);
                                        // convert to optical densities
                                        fodI[i] = rgbToOD(srcRA.get()
                                                        .getRealDouble());
                                }

                                // deconvolve through the destaining matrix
                                dcc = multMatWithVec(Mds1, fodI);

                                srcRA.setPosition(0, m_dimC);
                                dcc[0] = m_range
                                                * Math.exp(-(dcc[0] + dcc[1] + dcc[2]));
                                //
                                if (dcc[0] > m_range)
                                        dcc[0] = m_range;
                                outRA.setPosition(srcRA);
                                outRA.get().setReal(dcc[0] - m_min);

                                // deconvolve through the destaining matrix
                                dcc = multMatWithVec(Mds2, fodI);
                                srcRA.setPosition(1, m_dimC);
                                dcc[0] = m_range
                                                * Math.exp(-(dcc[0] + dcc[1] + dcc[2]));
                                //
                                if (dcc[0] > m_range)
                                        dcc[0] = m_range;
                                outRA.setPosition(srcRA);
                                outRA.get().setReal(dcc[0] - m_min);

                                // deconvolve through the destaining matrix
                                dcc = multMatWithVec(Mds3, fodI);
                                srcRA.setPosition(2, m_dimC);
                                dcc[0] = m_range
                                                * Math.exp(-(dcc[0] + dcc[1] + dcc[2]));
                                //
                                if (dcc[0] > m_range)
                                        dcc[0] = m_range;
                                outRA.setPosition(srcRA);
                                outRA.get().setReal(dcc[0] - m_min);

                                // convert the calculated optical densities back
                                // to rgb values
                                // and store them in the image
                                // for (int i = 0; i < Math.min(3,
                                // img.dimension(m_dimC)); i++)
                                // {
                                // srcRA.setPosition(i, m_dimC);
                                // dcc[i] = m_range * Math.exp(-dcc[i]);
                                //
                                // //
                                // if (dcc[i] > m_range)
                                // dcc[i] = m_range;
                                // srcRA.get().setReal(dcc[i] - m_min);
                                // }

                        }

                }
                return out;

        }

        // converts rgb values to optical densitys according to lambert-beer-law
        private double rgbToOD(double channel) {
                // +0.001 is needed because otherwise it can happen that one
                // color channel is 0 and the log of 0 would be infinity.
                // the dcc vector would be computed wrong!
                return -(Math.log((channel + m_min + 0.001) / m_range));
        }

        /*
         * stainVectors -> RGB
         */
        private void calcDeconvolutionVectors(double[]... stainVectors) {

                // the vectors need to be normalized !!
                for (int i = 0; i < stainVectors.length; i++) {
                        double len = Math.sqrt(stainVectors[i][0]
                                        * stainVectors[i][0]
                                        + stainVectors[i][1]
                                        * stainVectors[i][1]
                                        + stainVectors[i][2]
                                        * stainVectors[i][2]);
                        stainVectors[i][0] = stainVectors[i][0] / len;
                        stainVectors[i][1] = stainVectors[i][1] / len;
                        stainVectors[i][2] = stainVectors[i][2] / len;
                }

                double[] odR = stainVectors[0];
                double[] odG;
                double[] odB;
                if (stainVectors.length > 1) {
                        odG = stainVectors[1];
                } else {
                        odG = new double[3];
                        odG[1] = odR[2];
                        odG[2] = odR[0];
                        odG[3] = odR[1];

                }
                if (stainVectors.length > 2) {
                        odB = stainVectors[2];
                } else {
                        odB = new double[3];
                        if (odR[0] * odR[0] + odG[0] * odG[0] > 1) {
                                odB[0] = 0;
                        } else {
                                odB[0] = Math.sqrt(1.0 - (odR[0] * odR[0])
                                                - (odG[0] * odG[0]));
                        }
                        if (odR[1] * odR[1] + odG[1] * odG[1] > 1) {
                                odB[1] = 0;
                        } else {
                                odB[1] = Math.sqrt(1.0 - (odR[1] * odR[1])
                                                - (odG[1] * odG[1]));
                        }
                        if (odR[2] * odR[2] + odG[2] * odG[2] > 1) {
                                odB[2] = 0;
                        } else {
                                odB[2] = Math.sqrt(1.0 - (odR[2] * odR[2])
                                                - (odG[2] * odG[2]));
                        }
                }

                // m_odR-B vectors are the column vectors of the staining matrix
                // m_Ms
                m_Ms[0] = odR[0];
                m_Ms[1] = odG[0];
                m_Ms[2] = odB[0];
                m_Ms[3] = odR[1];
                m_Ms[4] = odG[1];
                m_Ms[5] = odB[1];
                m_Ms[6] = odR[2];
                m_Ms[7] = odG[2];
                m_Ms[8] = odB[2];

        }

        private double[] computeDestainMatrix(int dim) {

                // compute the destaining matrix Mds according to the chosen
                // vector of
                // the user
                double[] m = new double[9];
                if (dim == 1)
                        m = multMatrices(multMatrices(m_Ms, m_M1),
                                        inverseOf(m_Ms));
                else if (dim == 2)
                        m = multMatrices(multMatrices(m_Ms, m_M2),
                                        inverseOf(m_Ms));
                else
                        m = multMatrices(multMatrices(m_Ms, m_M3),
                                        inverseOf(m_Ms));

                return m;
        }

        private double[] multMatWithVec(double[] mat, double[] vec) {
                double[] v = new double[3];
                v[0] = mat[0] * vec[0] + mat[1] * vec[1] + mat[2] * vec[2];
                v[1] = mat[3] * vec[0] + mat[4] * vec[1] + mat[5] * vec[2];
                v[2] = mat[6] * vec[0] + mat[7] * vec[1] + mat[8] * vec[2];
                return v;
        }

        /**
         * die matrix ist links aufgebaut, die entsprechenen indizes im array
         * rechts m11 m12 m13 0 1 2 a b c m21 m22 m23 3 4 5 d e f m31 m32 m33 6
         * 7 8 g h i
         *
         */
        private double[] inverseOf(double[] m) {
                double[] res = new double[9];

                // berechne die determinante einer 3x3 matrix
                // http://www.mathe-online.at/materialien/klaus.berger/files/Matrizen/determinante.pdf
                double det = m[0] * m[4] * m[8] + m[1] * m[5] * m[6] + m[2]
                                * m[3] * m[7] - m[6] * m[4] * m[2] - m[7]
                                * m[5] * m[0] - m[8] * m[3] * m[1];

                // berechne inverse einer matrix
                // http://de.wikipedia.org/wiki/Inverse_Matrix
                det = 1.0f / det;
                res[0] = det * (m[4] * m[8] - m[5] * m[7]);
                res[1] = det * (m[2] * m[7] - m[1] * m[8]);
                res[2] = det * (m[1] * m[5] - m[2] * m[4]);
                res[3] = det * (m[5] * m[6] - m[3] * m[8]);
                res[4] = det * (m[0] * m[8] - m[2] * m[6]);
                res[5] = det * (m[2] * m[3] - m[0] * m[5]);
                res[6] = det * (m[3] * m[7] - m[4] * m[6]);
                res[7] = det * (m[1] * m[6] - m[0] * m[7]);
                res[8] = det * (m[0] * m[4] - m[1] * m[3]);

                return res;
        }

        /**
         *
         * @param a
         *                3x3 matrix
         * @param b
         *                3x3 matrix
         * @return the multiplied matrices
         */
        private double[] multMatrices(double[] a, double[] b) {
                double[] res = new double[9];
                res[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
                res[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
                res[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];
                res[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
                res[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
                res[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];
                res[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
                res[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
                res[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

                return res;
        }

        public enum PredefinedStain {

                HandE("H&E") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { .644211, .716556, .266844 },
                                                { .092789, .954111, 0.283111 } };
                        }

                },
                HandETwo("H&E2") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { .49015734, .076897085,
                                                                .41040173 },
                                                { .04615336, .842068, .5373925 } };
                        }

                },
                HDAB("H DAB") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] { { .650, .704, .286 },
                                                { .268, .570, .776 } };
                        }

                },
                FEULGEN("Feulgen Light Green") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { .46420921, .83008335,
                                                                .30827187 },
                                                { .94705542, 0.25373821,
                                                                0.19650764 } };
                        }

                },
                GIEMSA("Giemsa") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.834750233, 0.513556283,
                                                                0.196330403 },
                                                { 0.092789, 0.954111, 0.283111 } };
                        }

                },
                FASTRED("FastRed FastBlue DAB") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.21393921, 0.85112669,
                                                                0.47794022 },
                                                { 0.74890292, 0.60624161,
                                                                0.26731082 } };
                        }
                },
                METHYLGREEN("Methyl Green DAB") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.98003, 0.144316, 0.133146 },
                                                { 0.268, 0.570, 0.776 } };
                        }
                },
                HAndEDAB("H&E DAB") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.650, 0.704, 0.286 },
                                                { 0.072, 0.990, 0.105 },
                                                { 0.268, 0.570, 0.776 } };
                        }
                },
                HAEC("H AEC") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.650, 0.704, 0.286 },
                                                { 0.2743, 0.6796, 0.6803 } };
                        }
                },
                AZANMALLORY("Azan-Mallory") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { .853033, .508733, .112656 },
                                                { 0.09289875, 0.8662008,
                                                                0.49098468 },
                                                { 0.10732849, 0.36765403,
                                                                0.9237484 } };
                        }
                },
                MASSONTRICHROME("Masson Trichrome") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.7995107, 0.5913521,
                                                                0.10528667 },
                                                { 0.09997159, 0.73738605,
                                                                0.6680326 } };
                        }
                },
                ALCIAN("Alcian blue & H") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.874622, 0.457711, 0.158256 },
                                                { 0.552556, 0.7544, 0.353744 } };
                        }
                },

                HPAS("H PAS") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] {
                                                { 0.644211, 0.716556, 0.266844 },
                                                { 0.175411, 0.972178, 0.154589 } };
                        }
                },
                CMY("CMY") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] { { 0, 1, 1 },
                                                { 1, 0, 1 }, { 1, 1, 0 } };
                        }
                },
                RGB("RGB") {
                        @Override
                        public double[][] getVectors() {
                                return new double[][] { { 1, 0, 0 },
                                                { 0, 1, 0 }, { 0, 0, 1 } };
                        }
                };

                private final String m_name;

                private PredefinedStain(String name) {
                        m_name = name;
                }

                public static PredefinedStain getStainByName(String name) {
                        for (PredefinedStain s : PredefinedStain.values()) {
                                if (s.m_name.equals(name)) {
                                        return s;
                                }
                        }
                        return null;
                }

                public String getName() {
                        return m_name;
                }

                public abstract double[][] getVectors();

        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new ColorDeconv<T, K>(m_dimX, m_dimY, m_dimC,
                                m_stainVectors.clone());
        }

}
