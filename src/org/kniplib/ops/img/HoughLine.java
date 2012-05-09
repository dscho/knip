package org.kniplib.ops.img;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;

public class HoughLine<T extends RealType<T> & NativeType<T>, S extends RealType<S> & NativeType<S>, K extends IterableInterval<T>>
                implements UnaryOutputOperation<K, Img<S>> {

        private final int m_numBinsRho;

        private final int m_numBinsTheta;

        private final S m_outType;

        private double[] m_rho;

        private double[] m_theta;

        private double m_dTheta;

        private double m_dRho;

        private T m_threshold;

        public HoughLine(S outType, T threshold, int numBinsRho,
                        int numBinsTheta) {
                m_outType = outType;
                m_numBinsRho = numBinsRho;
                m_numBinsTheta = numBinsTheta;
                m_threshold = threshold;
        }

        @Override
        public Img<S> compute(K op, Img<S> res) {

                init(op);
                final long[] dims = new long[res.numDimensions()];
                res.dimensions(dims);

                final RandomAccess<S> resAccess = res.randomAccess();
                final Cursor<T> cursor = op.cursor();
                final long[] position = new long[op.numDimensions()];
                final double minTheta = -Math.PI / 2;
                final double minRho = -Util.computeLength(Util
                                .intervalDimensions(op));

                for (int t = 0; t < m_numBinsTheta; ++t) {
                        m_theta[t] = m_dTheta * t + minTheta;
                }
                for (int r = 0; r < m_numBinsRho; ++r) {
                        m_rho[r] = m_dRho * r + minRho;
                }

                while (cursor.hasNext()) {
                        double fRho;
                        int r;
                        int[] voteLoc = new int[2];

                        cursor.fwd();
                        cursor.localize(position);

                        for (int t = 0; t < m_numBinsTheta; ++t) {
                                if (cursor.get().compareTo(m_threshold) > 0) {
                                        fRho = Math.cos(m_theta[t])
                                                        * position[0]
                                                        + Math.sin(m_theta[t])
                                                        * position[1];

                                        r = Math.round((float) ((fRho - minRho) / m_dRho));
                                        voteLoc[0] = r;
                                        voteLoc[1] = t;
                                        try {
                                                placeVote(voteLoc, resAccess);
                                        } catch (Exception e) {
                                                // System.out.println(fRho);
                                                // System.err.println("Tried to place vote at "
                                                // + r +
                                                // " "
                                                // + t + " for theta " +
                                                // m_theta[t] + ", and rho "
                                                // + fRho);
                                        }
                                }
                        }
                }
                return res;

        }

        private void init(K op) {
                m_dRho = 2 * Util.computeLength(Util.intervalDimensions(op))
                                / (double) m_numBinsRho;
                m_threshold = op.firstElement().createVariable();
                m_dTheta = Math.PI / m_numBinsTheta;
                m_theta = new double[m_numBinsTheta];
                m_rho = new double[m_numBinsRho];

        }

        /**
         * Place a vote of value 1.
         *
         * @param loc
         *                the integer array indicating the location where the
         *                vote is to be placed in voteSpace.
         * @return whether the vote was successful. This here particular method
         *         should always return true.
         */
        protected void placeVote(final int[] loc, RandomAccess<S> ra) {
                ra.setPosition(loc);
                m_outType.setOne();
                ra.get().add(m_outType);
        }

        public double[] getTranslatedPos(int[] pos) {
                return new double[] { m_rho[pos[0]], m_theta[pos[1]] };

        }

        @Override
        public UnaryOutputOperation<K, Img<S>> copy() {
                return new HoughLine<T, S, K>(m_outType.copy(), m_threshold,
                                m_numBinsRho, m_numBinsTheta);
        }

        @Override
        public Img<S> createEmptyOutput(K in) {
                return new ArrayImgFactory<S>().create(new long[] {
                                m_numBinsRho, m_numBinsTheta },
                                m_outType.createVariable());
        }

        @Override
        public Img<S> compute(K in) {
                return compute(in, createEmptyOutput(in));
        }
}
