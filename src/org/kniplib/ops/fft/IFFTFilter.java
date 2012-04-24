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
package org.kniplib.ops.fft;

import net.imglib2.Cursor;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.real.DoubleType;

import org.apache.commons.math.complex.Complex;
import org.apache.commons.math.transform.FastFourierTransformer;

/**
 * Inverse FFT
 * 
 * @author tcriess, University of Konstanz
 */
public class IFFTFilter<T extends ComplexType<T>> implements
                UnaryOutputOperation<Img<T>, Img<DoubleType>> {

        public final static int MIN_DIMS = 2;

        public final static int MAX_DIMS = 2;

        @SuppressWarnings("rawtypes")
        // @SuppressWarnings("unused")
        // private int[] m_originalsize = null;
        // @SuppressWarnings("unused")
        // private int[] m_originaloffset = null;
        // @SuppressWarnings({ "unused", "rawtypes" })
        // private FourierTransform m_forwardTransform = null;
        private boolean m_rearrange = false;

        public IFFTFilter(boolean rearrange) {
                m_rearrange = rearrange;
        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        @Override
        public Img<DoubleType> compute(Img<T> srcIn, Img<DoubleType> res) {
                long[] size = new long[srcIn.numDimensions()];
                srcIn.dimensions(size);

                int[] pos = new int[srcIn.numDimensions()];
                Complex[][] image = new Complex[(int) size[0]][(int) size[1]];
                Cursor<T> c = srcIn.localizingCursor();
                while (c.hasNext()) {
                        c.fwd();
                        c.localize(pos);
                        image[pos[0]][pos[1]] = new Complex(c.get()
                                        .getRealDouble(), c.get()
                                        .getImaginaryDouble());
                }
                FastFourierTransformer transformer = new FastFourierTransformer();
                Complex[][] output = (Complex[][]) transformer.mdfft(image,
                                false);

                Cursor<DoubleType> c2 = res.localizingCursor();
                if (m_rearrange) {
                        while (c2.hasNext()) {
                                c2.fwd();
                                c2.localize(pos);
                                if (pos[0] < size[0] / 2) {
                                        pos[0] += size[0] / 2;
                                } else {
                                        pos[0] -= size[0] / 2;
                                }
                                if (pos[1] < size[1] / 2) {
                                        pos[1] += size[1] / 2;
                                } else {
                                        pos[1] -= size[1] / 2;
                                }
                                c2.get().set(output[pos[0]][pos[1]].getReal());
                        }
                } else {
                        while (c2.hasNext()) {
                                c2.fwd();
                                c2.localize(pos);
                                c2.get().set(output[pos[0]][pos[1]].getReal());
                        }
                }

                return res;
        }

        @Override
        public UnaryOutputOperation<Img<T>, Img<DoubleType>> copy() {
                return new IFFTFilter<T>(m_rearrange);
        }

        @Override
        public Img<DoubleType> compute(Img<T> in) {
                return compute(in, createEmptyOutput(in));
        }

        @Override
        public Img<DoubleType> createEmptyOutput(Img<T> in) {
                try {
                        long[] dims = new long[in.numDimensions()];
                        in.dimensions(dims);

                        return in.factory().imgFactory(new DoubleType())
                                        .create(dims, new DoubleType());
                } catch (IncompatibleTypeException e) {
                        // TODO Handle exception
                        new RuntimeException(e);
                }
                return null;
        }
}
