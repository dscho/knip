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
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.ops.image.UnaryOperationAssignment;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ops.misc.Convert;
import org.kniplib.types.ImgConversionTypes;
import org.kniplib.types.TypeConversionTypes;

/**
 * Converts the pixel-types of images.
 *
 * @author hornm, dietzc, University of Konstanz
 */
public class ImgConvert<I extends RealType<I>, O extends RealType<O>>
                implements UnaryOutputOperation<Img<I>, Img<O>> {

        private final O m_outType;

        private final I m_inType;

        private final ImgConversionTypes m_conversionType;

        /**
         * Convert to the new type. Scale values with respect to the old type
         * range.
         *
         * @param outType
         *                The new type.
         * @param inType
         *                The old type.
         * @param imgFac
         *                the image factory to produce the image
         */
        public ImgConvert(final I inType, final O outType,
                        ImgConversionTypes type) {
                m_outType = outType;
                m_conversionType = type;
                m_inType = inType;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<O> createEmptyOutput(Img<I> op) {
                try {
                        long[] dims = new long[op.numDimensions()];
                        op.dimensions(dims);
                        return op.factory()
                                        .imgFactory(m_outType)
                                        .create(dims,
                                                        m_outType.createVariable());
                } catch (IncompatibleTypeException e) {
                        e.printStackTrace();
                        return null;
                }

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<O> compute(Img<I> img, Img<O> r) {

                double[] normPar;
                Convert<I, O> convertOp = null;
                switch (m_conversionType) {
                case DIRECT:
                        convertOp = new Convert<I, O>(m_inType, m_outType,
                                        TypeConversionTypes.DIRECT);
                        break;
                case DIRECTCLIP:
                        convertOp = new Convert<I, O>(m_inType, m_outType,
                                        TypeConversionTypes.DIRECTCLIP);
                        break;
                case NORMALIZEDIRECT:
                        normPar = new ImgNormalize<I, Img<I>>()
                                        .getNormalizationProperties(img, 0);

                        convertOp = new Convert<I, O>(m_inType, m_outType,
                                        TypeConversionTypes.SCALE);

                        convertOp.setFactor(convertOp.getFactor() / normPar[0]);
                        convertOp.setInMin(normPar[1]);
                        break;
                case NORMALIZESCALE:
                        normPar = new ImgNormalize<I, Img<I>>()
                                        .getNormalizationProperties(img, 0);

                        convertOp = new Convert<I, O>(m_inType, m_outType,
                                        TypeConversionTypes.SCALE);
                        convertOp.setFactor(convertOp.getFactor() / normPar[0]);
                        break;
                case NORMALIZEDIRECTCLIP:
                        normPar = new ImgNormalize<I, Img<I>>()
                                        .getNormalizationProperties(img, 0);
                        convertOp = new Convert<I, O>(m_inType, m_outType,
                                        TypeConversionTypes.SCALECLIP);
                        convertOp.setFactor(convertOp.getFactor() / normPar[0]);
                        convertOp.setInMin(normPar[1]);
                        break;
                case SCALE:
                        convertOp = new Convert<I, O>(m_inType, m_outType,
                                        TypeConversionTypes.SCALE);

                }

                UnaryOperationAssignment<I, O> map = new UnaryOperationAssignment<I, O>(
                                convertOp);
                map.compute(img, r);
                return r;
        }

        @Override
        public UnaryOutputOperation<Img<I>, Img<O>> copy() {
                return new ImgConvert<I, O>(m_inType.copy(), m_outType.copy(),
                                m_conversionType);
        }

        @Override
        public Img<O> compute(Img<I> in) {
                return compute(in, createEmptyOutput(in));
        }
}
