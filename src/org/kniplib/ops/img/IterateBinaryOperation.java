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
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.BinaryOperation;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.Type;

import org.kniplib.data.img.SubImgPlus;
import org.kniplib.data.img.SubLabeling;
import org.kniplib.ops.misc.IntervalsFromDimSelection;

/**
 * Applies a given Operation to each interval separately.
 * 
 * @author dietzc University of Konstanz
 */
public final class IterateBinaryOperation<T extends Type<T>, V extends Type<V>, O extends Type<O>, S extends RandomAccessibleInterval<T> & IterableInterval<T>, U extends RandomAccessibleInterval<V> & IterableInterval<V>, R extends RandomAccessibleInterval<O> & IterableInterval<O>>
                implements BinaryOperation<S, U, R> {

        private BinaryOperation<S, U, R> m_op;

        private int[] m_selectedDims;

        private IntervalsFromDimSelection m_intervalOp;

        public IterateBinaryOperation(BinaryOperation<S, U, R> op,
                        int[] selectedDims) {
                m_op = op;
                m_selectedDims = selectedDims;
                m_intervalOp = new IntervalsFromDimSelection();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final R compute(final S in0, U in1, R out) {

                Interval[] res = m_intervalOp.compute(m_selectedDims,
                                new Interval[] { in0 });

                for (Interval interval : res) {
                        m_op.compute(createSubType(in0, interval),
                                        createSubType(in1, interval),
                                        createSubType(out, interval));
                }

                return out;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static synchronized <K extends Interval> K createSubType(
                        final K any, final Interval i) {
                if (any instanceof Labeling) {
                        return (K) new SubLabeling((NativeImgLabeling) any, i);
                }
                if (any instanceof ImgPlus) {
                        return (K) new SubImgPlus((ImgPlus) any, i);
                }

                if (any instanceof Img) {
                        return (K) new SubImg((Img) any, i, false);
                }
                throw new IllegalArgumentException(
                                "Not implemented yet (IntervalWiseOperation)");
        }

        @Override
        public BinaryOperation<S, U, R> copy() {
                return new IterateBinaryOperation<T, V, O, S, U, R>(
                                m_op.copy(), m_selectedDims);
        }
}
