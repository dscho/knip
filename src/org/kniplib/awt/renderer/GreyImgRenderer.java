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
 *   6 May 2011 (hornm): created
 */
package org.kniplib.awt.renderer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.IntervalIndexer;

import org.kniplib.awt.AWTImageTools;
import org.kniplib.io.serialization.PlanarImgContainerSamplerImpl;

/**
 * 
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 * 
 * 
 * @param <T>
 *                {@link RealType} of the rendered {@link Img}
 */
public class GreyImgRenderer<T extends RealType<T>> extends
                RealImgRenderer<T, Img<T>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public BufferedImage render(Img<T> img, int dimX, int dimY, long[] pos,
                        double scale, double min, double normalizationFactor) {

                long[] newDim = new long[2];
                Img<T> srcImg = RealImgRenderer.<T> findSourceImg(img);
                T type = srcImg.firstElement().createVariable();

                // speed-ups for ArrayImg and PlanarImg (currently only of the
                // scale
                // factor is 1.0)
                if ((srcImg instanceof PlanarImg || srcImg instanceof ArrayImg)
                                && dimX == 0
                                && dimY == 1
                                && (type instanceof ShortType
                                                || type instanceof UnsignedShortType
                                                || type instanceof ByteType || type instanceof UnsignedByteType)
                                && scale == 1.0) {

                        int offset;

                        ArrayDataAccess da;

                        if (srcImg instanceof ArrayImg) {
                                da = (ArrayDataAccess) ((ArrayImg) srcImg)
                                                .update(null);
                                offset = 0;
                                long[] tmpPos = pos.clone();
                                long[] dims = new long[srcImg.numDimensions()];
                                tmpPos[dimX] = 0;
                                tmpPos[dimY] = 0;
                                srcImg.dimensions(dims);
                                offset = (int) IntervalIndexer.positionToIndex(
                                                tmpPos, dims);

                        } else {
                                offset = 0;
                                int planeIdx;
                                if (pos.length > 2) {
                                        long[] tmpPos = new long[pos.length - 2];
                                        long[] tmpDim = new long[pos.length - 2];
                                        for (int i = 0; i < tmpDim.length; i++) {
                                                tmpPos[i] = pos[i + 2];
                                                tmpDim[i] = srcImg
                                                                .dimension(i + 2);
                                        }
                                        planeIdx = (int) IntervalIndexer
                                                        .positionToIndex(
                                                                        tmpPos,
                                                                        tmpDim);
                                } else {
                                        planeIdx = 0;
                                }
                                da = ((PlanarImg) srcImg)
                                                .update(new PlanarImgContainerSamplerImpl(
                                                                planeIdx));
                        }

                        Object buf = da.getCurrentStorageArray();

                        BufferedImage res = null;

                        if (type instanceof ByteType) {
                                res = constructImage((byte[]) buf,
                                                img.dimension(dimX),
                                                img.dimension(dimY), offset,
                                                min, normalizationFactor, true);
                        } else if (type instanceof UnsignedByteType) {
                                res = constructImage((byte[]) buf,
                                                img.dimension(dimX),
                                                img.dimension(dimY), offset,
                                                min, normalizationFactor, false);
                        } else if (type instanceof ShortType) {
                                res = constructImage((short[]) buf,
                                                img.dimension(dimX),
                                                img.dimension(dimY), offset,
                                                min, normalizationFactor, true);
                        } else if (type instanceof UnsignedShortType) {
                                res = constructImage((short[]) buf,
                                                img.dimension(dimX),
                                                img.dimension(dimY), offset,
                                                min, normalizationFactor, false);
                        }

                        return res;

                }

                // default rendering
                byte[] slice = extractSliceByte(img, dimX, dimY, pos, newDim,
                                scale, min, normalizationFactor);
                DataBuffer buffer = new DataBufferByte(slice, slice.length);
                int c = 1; // number of channels
                return AWTImageTools.constructImage(c, DataBuffer.TYPE_BYTE,
                                (int) newDim[0], (int) newDim[1], true, false,
                                buffer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
                return "Real Image Renderer";
        }

        private static final BufferedImage constructImage(byte[] buf,
                        long width, long height, int offset, double min,
                        double factor, boolean signed) {

                byte[] slice = new byte[(int) (width * height)];
                System.arraycopy(buf, offset, slice, 0, slice.length);

                if (signed) {
                        for (int i = 0; i < slice.length; i++) {
                                slice[i] = (byte) (slice[i] - 0x80);
                        }
                        min += 0x80;

                }

                if (factor != 1) {
                        int max = 2 * Byte.MAX_VALUE + 1;
                        for (int i = 0; i < slice.length; i++) {
                                slice[i] = (byte) Math
                                                .min(max,
                                                                Math.max(0,
                                                                                (Math.round((((byte) (slice[i] + 0x80)) + 0x80 - min)
                                                                                                * factor))));

                        }
                }

                DataBuffer buffer = new DataBufferByte(slice, slice.length);
                int c = 1; // number of channels
                return AWTImageTools.constructImage(c, DataBuffer.TYPE_BYTE,
                                (int) width, (int) height, true, false, buffer);
        }

        private static final BufferedImage constructImage(short[] buf,
                        long width, long height, int offset, double min,
                        double factor, boolean signed) {

                short[] slice = new short[(int) (width * height)];
                System.arraycopy(buf, offset, slice, 0, slice.length);

                if (signed) {
                        for (int i = 0; i < slice.length; i++) {
                                slice[i] = (short) (slice[i] - 0x8000);
                        }
                        min += 0x8000;
                }

                if (factor != 1) {
                        int max = 2 * Short.MAX_VALUE + 1;
                        for (int i = 0; i < slice.length; i++) {
                                slice[i] = (short) Math
                                                .min(max,
                                                                Math.max(0,
                                                                                (Math.round((((short) (slice[i] + 0x8000)) + 0x8000 - min)
                                                                                                * factor))));

                        }
                }

                DataBuffer buffer = new DataBufferUShort(slice, slice.length);
                int c = 1; // number of channels
                return AWTImageTools.constructImage(c, DataBuffer.TYPE_USHORT,
                                (int) width, (int) height, true, false, buffer);
        }
}
