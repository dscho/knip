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
 *   13 May 2011 (hornm): created
 */
package org.kniplib.ops.img;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.ops.image.UnaryOperationAssignment;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ops.misc.Convert;

/**
 * Converts the pixel-types of images.
 * 
 * @author hornm, dietzc, University of Konstanz
 */
public class ImgConvert<I extends RealType<I>, O extends RealType<O>>
                implements UnaryOutputOperation<Img<I>, Img<O>> {

        private UnaryOperationAssignment<I, O> m_map;

        private O m_type;

        private I m_scaleFrom;

        /**
         * Convert to the new type. Result will be an {@link ArrayImg}.
         * 
         * @param type
         *                The new type.
         */
        public ImgConvert(final O type) {
                this(null, type);
        }

        /**
         * Convert to the new type. Scale values with respect to the old type
         * range.
         * 
         * @param type
         *                The new type.
         * @param scaleFrom
         *                The old type.
         * @param imgFac
         *                the image factory to produce the image
         */
        public ImgConvert(final I scaleFrom, final O type) {
                m_type = type;
                m_scaleFrom = scaleFrom;
                m_map = new UnaryOperationAssignment<I, O>(new Convert<I, O>(
                                type, scaleFrom));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<O> createEmptyOutput(Img<I> op) {
                try {
                        long[] dims = new long[op.numDimensions()];
                        op.dimensions(dims);
                        return op.factory().imgFactory(m_type)
                                        .create(dims, m_type.createVariable());
                } catch (IncompatibleTypeException e) {
                        e.printStackTrace();
                        return null;
                }

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<O> compute(Img<I> op, Img<O> r) {
                m_map.compute(op, r);
                return r;
        }

        @Override
        public UnaryOutputOperation<Img<I>, Img<O>> copy() {
                return new ImgConvert<I, O>(m_scaleFrom.copy(), m_type.copy());
        }

        @Override
        public Img<O> compute(Img<I> in) {
                return compute(in, createEmptyOutput(in));
        }
}
