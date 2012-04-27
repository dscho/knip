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
 *   2 Dec 2010 (hornm): created
 */
package org.kniplib.ops.fft;

import net.imglib2.Cursor;
import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.RandomAccess;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.ops.image.UnaryOperationAssignment;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.RealVector;
import org.apache.commons.math.linear.SingularValueDecomposition;
import org.kniplib.ops.img.ExtendDimensionality;
import org.kniplib.ops.img.ImgUtils;
import org.kniplib.ops.misc.Convert;
import org.kniplib.tools.ApacheMathTools;
import org.kniplib.tools.FilterTools;
import org.kniplib.tools.ImgBasedRealMatrix;
import org.kniplib.types.TypeConversionTypes;

/**
 *
 * @author hornm, schoenen, dietzc University of Konstanz
 */
public class DirectImageConvolution<T extends RealType<T>, K extends RealType<K> & NativeType<K>>
                extends ImageConvolution<T, K, Img<T>, Img<K>> {

        private Img<DoubleType>[] m_kernels;

        private boolean m_normalize;

        private Img<K> m_kernel;

        public DirectImageConvolution() {
                super();
        }

        public DirectImageConvolution(final Img<K> kernel,
                        final boolean normalizeKernel) {
                m_normalize = normalizeKernel;
                setKernel(kernel);
        }

        public DirectImageConvolution(final Img<K> kernel) {
                this(kernel, true);
        }

        @Override
        public void setKernel(final Img<K> kernel) {
                m_kernel = kernel;
                try {
                        m_kernels = decomposeKernel(kernel);
                } catch (final IncompatibleTypeException e) {
                        throw new RuntimeException(e);
                }
                if (m_normalize) {
                        for (final Img<DoubleType> k : m_kernels) {
                                FilterTools.normalize(k);
                        }
                }
        }

        /**
         * {@inheritDoc}
         *
         * @return
         */
        @Override
        public Img<T> compute(final Img<T> op, final Img<T> r) {
                final ExtendDimensionality<DoubleType> ext = new ExtendDimensionality<DoubleType>(
                                op.numDimensions());
                Img<FloatType> tmp = convolve(op, ext.compute(m_kernels[0],
                                ext.createEmptyOutput(m_kernels[0])));
                for (int i = 1; i < m_kernels.length; i++) {
                        tmp = convolve(tmp, ext.compute(m_kernels[i],
                                        ext.createEmptyOutput(m_kernels[i])));
                }
                new UnaryOperationAssignment<FloatType, T>(
                                new Convert<FloatType, T>(new FloatType(), op
                                                .firstElement()
                                                .createVariable(),
                                                TypeConversionTypes.DIRECTCLIP))
                                .compute(tmp, r);
                return r;
        }

        private static final <R extends RealType<R>> Img<FloatType> convolve(
                        final Img<R> img, final Img<DoubleType> kernel) {
                final R min = img.firstElement().createVariable();
                min.setReal(min.getMinValue());
                final RandomAccess<R> srcRA = new ExtendedRandomAccessibleInterval<R, Img<R>>(
                                img,
                                new OutOfBoundsConstantValueFactory<R, Img<R>>(
                                                min)).randomAccess();

                final Cursor<DoubleType> kernelC = kernel.localizingCursor();

                Img<FloatType> res = null;
                try {
                        res = img.factory().imgFactory(new FloatType())
                                        .create(img, new FloatType());
                } catch (final IncompatibleTypeException e) {
                        //
                }

                final Cursor<FloatType> resC = res.localizingCursor();
                final long[] pos = new long[img.numDimensions()];
                final long[] kernelRadius = new long[kernel.numDimensions()];
                for (int i = 0; i < kernelRadius.length; i++) {
                        kernelRadius[i] = kernel.dimension(i) / 2;
                }
                while (resC.hasNext()) {
                        resC.fwd();
                        resC.localize(pos);
                        resC.get().set(convolve(srcRA, kernelC, pos,
                                        kernelRadius));
                }
                return res;
        }

        @SuppressWarnings("unchecked")
        private static synchronized <K extends RealType<K>> Img<DoubleType>[] decomposeKernel(
                        final Img<K> kernel) throws IncompatibleTypeException {
                if (kernel.numDimensions() != 2) {
                        final Img<DoubleType> res = kernel.factory()
                                        .imgFactory(new DoubleType())
                                        .create(kernel, new DoubleType());
                        new UnaryOperationAssignment<K, DoubleType>(
                                        new Convert<K, DoubleType>(
                                                        kernel.firstElement()
                                                                        .createVariable(),
                                                        new DoubleType(),
                                                        TypeConversionTypes.DIRECT))
                                        .compute(kernel, res);
                        return new Img[] { res };
                }
                Img<DoubleType> vkernel;
                Img<DoubleType> ukernel;
                try {
                        final RealMatrix mKernel = new ImgBasedRealMatrix<K>(
                                        kernel);
                        final SingularValueDecomposition svd = new SingularValueDecomposition(
                                        mKernel);
                        if (svd.getRank() > 1) {
                                final Img<DoubleType> res = kernel
                                                .factory()
                                                .imgFactory(new DoubleType())
                                                .create(kernel,
                                                                new DoubleType());
                                // Not separable
                                // System.out.println("Take non-one rank kernel "
                                // +
                                // svd.getRank());
                                new UnaryOperationAssignment<K, DoubleType>(
                                                new Convert<K, DoubleType>(
                                                                kernel.firstElement()
                                                                                .createVariable(),
                                                                new DoubleType(),
                                                                TypeConversionTypes.DIRECT))
                                                .compute(kernel, res);
                                return new Img[] { res };
                        }

                        final RealVector v = svd.getV().getColumnVector(0);
                        final RealVector u = svd.getU().getColumnVector(0);
                        final double s = -Math.sqrt(svd.getS().getEntry(0, 0));
                        v.mapMultiplyToSelf(s);
                        u.mapMultiplyToSelf(s);
                        vkernel = null;
                        ukernel = null;
                        try {
                                // V -> horizontal
                                vkernel = ApacheMathTools
                                                .vectorToImage(v,
                                                                new DoubleType(),
                                                                1,
                                                                kernel.factory()
                                                                                .imgFactory(new DoubleType()));
                                // U -> vertical
                                ukernel = ApacheMathTools
                                                .vectorToImage(u,
                                                                new DoubleType(),
                                                                2,
                                                                kernel.factory()
                                                                                .imgFactory(new DoubleType()));
                        } catch (final IncompatibleTypeException e) {
                                //
                        }
                        return new Img[] { vkernel, ukernel };
                } catch (final MathIllegalArgumentException e) {//
                } catch (final IllegalStateException e) {//
                }

                final Img<FloatType> res = kernel.factory()
                                .imgFactory(new FloatType())
                                .create(kernel, new FloatType());
                new UnaryOperationAssignment<K, FloatType>(
                                new Convert<K, FloatType>(kernel.firstElement()
                                                .createVariable(),
                                                new FloatType(),
                                                TypeConversionTypes.DIRECT))
                                .compute(kernel, res);
                return new Img[] { res };
        }

        /**
         * Straightforward convolution. For small kernels faster than the
         * convolution in the frequency domain for small images.
         *
         * @param img
         *                the image in the spatial domain
         *
         * @param kerC
         *                the kernel in the spatial domain
         *
         * @param pos
         *                the position to apply the kernel
         *
         * @return
         */
        public static final <R extends RealType<R>> float convolve(
                        final RandomAccess<R> srcRA,
                        final Cursor<DoubleType> kerC, final long[] pos,
                        final long[] kernelRadii) {
                srcRA.setPosition(pos);
                float val = 0;
                kerC.reset();
                while (kerC.hasNext()) {
                        kerC.fwd();
                        for (int i = 0; i < kernelRadii.length; i++) {
                                srcRA.setPosition(
                                                pos[i]
                                                                + kerC.getLongPosition(i)
                                                                - kernelRadii[i],
                                                i);
                        }
                        val += srcRA.get().getRealFloat()
                                        * kerC.get().getRealFloat();
                }
                return val;
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
                return name;
        }

        @Override
        public DirectImageConvolution<T, K> copy() {
                return new DirectImageConvolution<T, K>(m_kernel, m_normalize);
        }

        @Override
        public Img<T> createEmptyOutput(final Img<T> in) {
                return ImgUtils.createEmptyImg(in);
        }

        @Override
        public Img<T> compute(final Img<T> in) {
                return compute(in, createEmptyOutput(in));
        }
}
