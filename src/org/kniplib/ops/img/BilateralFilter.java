/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 * 
 * History
 *   30 Dec 2010 (hornm): created
 */
package org.kniplib.ops.img;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.subimg.SubImg;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IterableRandomAccessibleInterval;

/**
 * Image projection.
 * 
 * @author tcriess, University of Konstanz
 */
public class BilateralFilter<T extends RealType<T>, K extends RandomAccessibleInterval<T> & IterableInterval<T>>
                implements UnaryOperation<K, K> {

        public final static int MIN_DIMS = 2;

        public final static int MAX_DIMS = 2;

        private double m_sigma_r = 15;

        private double m_sigma_s = 5;

        private int m_radius = 10;

        private int[] m_selectedDims;

        public BilateralFilter(double sigma_r, double sigma_s, int radius,
                        int[] selectedDims) {
                m_sigma_r = sigma_r;
                m_sigma_s = sigma_s;
                m_radius = radius;
                m_selectedDims = selectedDims.clone();
        }

        private static double gauss(double x, double sigma) {
                double mu = 0.0;
                return (1 / (sigma * Math.sqrt(2 * Math.PI)))
                                * Math.exp(-0.5 * (x - mu) * (x - mu)
                                                / (sigma * sigma));
        }

        /**
         * {@inheritDoc}
         */
        public K compute(K srcIn, K res) {

                long[] size = new long[srcIn.numDimensions()];
                srcIn.dimensions(size);

                RandomAccess<T> cr = res.randomAccess();
                Cursor<T> cp = srcIn.localizingCursor();
                int[] p = new int[srcIn.numDimensions()];
                int[] q = new int[srcIn.numDimensions()];
                long[] mi = new long[srcIn.numDimensions()];
                long[] ma = new long[srcIn.numDimensions()];
                long mma1 = srcIn.max(0);
                long mma2 = srcIn.max(1);
                IterableRandomAccessibleInterval<T> si;
                Cursor<T> cq;
                while (cp.hasNext()) {
                        cp.fwd();
                        cp.localize(p);
                        double d;
                        // Cursor<T> cq = srcIn.localizingCursor();
                        cp.localize(mi);
                        cp.localize(ma);
                        mi[m_selectedDims[0]] = Math.max(0,
                                        mi[m_selectedDims[0]] - m_radius);
                        mi[m_selectedDims[1]] = Math.max(0,
                                        mi[m_selectedDims[1]] - m_radius);
                        ma[m_selectedDims[0]] = Math.min(mma1,
                                        mi[m_selectedDims[0]] + m_radius);
                        ma[m_selectedDims[1]] = Math.min(mma2,
                                        mi[m_selectedDims[1]] + m_radius);
                        Interval in = new FinalInterval(mi, ma);
                        si = new IterableRandomAccessibleInterval<T>(
                                        SubImg.getView(srcIn, in, false));
                        cq = si.localizingCursor();
                        double s, v = 0.0;
                        double w = 0.0;
                        while (cq.hasNext()) {
                                cq.fwd();
                                cq.localize(q);
                                // d = 0.0;
                                d = (p[m_selectedDims[0]]
                                                - q[m_selectedDims[0]] - mi[m_selectedDims[0]])
                                                * (p[m_selectedDims[0]]
                                                                - q[m_selectedDims[0]] - mi[m_selectedDims[0]])
                                                + (p[m_selectedDims[1]]
                                                                - q[m_selectedDims[1]] - mi[m_selectedDims[1]])
                                                * (p[m_selectedDims[1]]
                                                                - q[m_selectedDims[1]] - mi[m_selectedDims[1]]);
                                // for(int i=0; i<q.length; i++) {
                                // d += (p[i]-q[i]-mi[i])*(p[i]-q[i]-mi[i]);
                                // }
                                d = Math.sqrt(d);
                                s = gauss(d, m_sigma_s);

                                d = Math.abs(cp.get().getRealDouble()
                                                - cq.get().getRealDouble());
                                s *= gauss(d, m_sigma_r);

                                v += s * cq.get().getRealDouble();
                                w += s;
                        }
                        cr.setPosition(p);
                        cr.get().setReal(v / w);
                }
                return res;

        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new BilateralFilter<T, K>(m_sigma_r, m_sigma_s,
                                m_radius, m_selectedDims);
        }
}
