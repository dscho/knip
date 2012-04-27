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
 *   30 Nov 2010 (hornm): created
 */
package org.kniplib.ops.fft;

import net.imglib2.algorithm.fft.FourierConvolution;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ops.img.ExtendDimensionality;

/**
 *
 * @author hornm, University of Konstanz
 */
public class ImgLibImageConvolution<T extends RealType<T>, K extends RealType<K> & NativeType<K>>
                extends ImageConvolution<T, K, Img<T>, Img<K>> {

        protected Img<K> m_kernel;

        private Img<T> m_lastImg = null;

        private FourierConvolution<? extends RealType<?>, K> m_fc = null;

        private int m_numThreads = -1;

        public ImgLibImageConvolution(final Img<K> kernel) {
                setKernel(kernel);
        }

        public ImgLibImageConvolution(final int numThreads) {
                m_numThreads = numThreads;
        }

        public ImgLibImageConvolution(final int numThreads, final Img<K> kernel) {
                this(kernel);
                m_numThreads = numThreads;
        }

        @Override
        public final void setKernel(final Img<K> kernel) {
                m_kernel = kernel;

                // TODO: Why disabled? Unessacary?
                // FilterTools.normalize(m_kernel);
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public final Img<T> compute(final Img<T> op, final Img<T> r) {
                throw new UnsupportedOperationException(
                                "This methid is not supported for now. Will be changed as soon as FourierTransform is an Operation");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setHint(final String name, final String value) {
                //
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getHint(final String name) {
                return "ImgLib2 convolution";
        }

        @Override
        public ImgLibImageConvolution<T, K> copy() {
                return new ImgLibImageConvolution<T, K>(m_numThreads,
                                m_kernel.copy());
        }

        // TODO: This is a hack caused by the img convolution framework
        // which doesn't provide the possibility that a result is filled
        @Override
        public Img<T> createEmptyOutput(final Img<T> in) {
                throw new UnsupportedOperationException(
                                "This methid is not supported for now. Will be changed as soon as FourierTransform is an Operation");
        }

        // TODO: This is a hack caused by the img convolution framework
        // which doesn't provide the possibility that a result is filled
        @Override
        public Img<T> compute(final Img<T> op) {

                final Img<K> kernel = new ExtendDimensionality<K>(
                                op.numDimensions()).compute(m_kernel);

                if (m_lastImg != op) {
                        m_lastImg = op;
                        try {
                                m_fc = new FourierConvolution<T, K>(m_lastImg,
                                                kernel);
                                if (m_numThreads != -1) {
                                        m_fc.setNumThreads(m_numThreads);
                                }
                        } catch (final IncompatibleTypeException e) {
                                throw new RuntimeException(e);
                        }
                }
                m_fc.replaceKernel(kernel);
                m_fc.process();

                return (Img<T>) m_fc.getResult();
        }
}
