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
 *   20 Jun 2011 (dietzc, hornm): created
 */
package org.kniplib.ops.img;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.subimg.SubImg;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.Type;

import org.kniplib.data.img.SubImgPlus;
import org.kniplib.data.img.SubLabeling;
import org.kniplib.ops.misc.IntervalsFromDimSelection;

/**
 * Applies a given Operation to each interval separately.
 * 
 * @author dietzc University of Konstanz
 */
public final class IterateUnaryOperation<T extends Type<T>, V extends Type<V>, S extends RandomAccessibleInterval<T> & IterableInterval<T>, U extends RandomAccessibleInterval<V> & IterableInterval<V>>
                implements UnaryOperation<S, U> {

        private UnaryOperation<S, U> m_op;

        private int[] m_selectedDims;

        private IntervalsFromDimSelection m_intervalOp;

        public IterateUnaryOperation(UnaryOperation<S, U> op, int[] selectedDims) {
                m_op = op;
                m_selectedDims = selectedDims;
                m_intervalOp = new IntervalsFromDimSelection();

        }

        /**
         * {@inheritDoc}
         */
        public final U compute(final S in, final U out) {

                Interval[] res = m_intervalOp.compute(m_selectedDims,
                                new Interval[] { in });

                for (Interval interval : res) {
                        m_op.compute(createSubTypeIn(interval, in),
                                        createSubTypeOut(interval, out));
                }

                return out;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private synchronized S createSubTypeIn(final Interval i, final S in) {
                if (in instanceof Labeling) {
                        return (S) new SubLabeling((NativeImgLabeling) in, i);
                }
                if (in instanceof ImgPlus) {
                        return (S) new SubImgPlus<T>((ImgPlus) in, i);
                }

                if (in instanceof Img) {
                        return (S) new SubImg<T>((Img) in, i, false);
                }
                throw new IllegalArgumentException(
                                "Not implemented yet (IntervalWiseOperation)");
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private synchronized U createSubTypeOut(final Interval i, final U in) {
                if (in instanceof Labeling) {
                        return (U) new SubLabeling((NativeImgLabeling) in, i);
                }
                if (in instanceof ImgPlus) {
                        return (U) new SubImgPlus<T>((ImgPlus) in, i);
                }

                if (in instanceof Img) {
                        return (U) new SubImg<T>((Img) in, i, false);
                }
                throw new IllegalArgumentException(
                                "Not implemented yet (IntervalWiseOperation)");
        }

        @Override
        public UnaryOperation<S, U> copy() {
                return new IterateUnaryOperation<T, V, S, U>(m_op.copy(),
                                m_selectedDims);
        }
}
