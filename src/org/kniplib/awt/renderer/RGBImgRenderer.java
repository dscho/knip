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

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 * @param <T>
 * @param <I>
 */
public class RGBImgRenderer<T extends RealType<T>, I extends Img<T>> extends
                RealImgRenderer<T, I> {

        private int m_rgbDim = -1;

        /**
	 * 
	 */
        public RGBImgRenderer() {
                //
        }

        /**
         * @param rgbDim
         */
        public RGBImgRenderer(int rgbDim) {
                m_rgbDim = rgbDim;

        }

        /**
         * {@inheritDoc}
         */
        public BufferedImage render(I img, int dimX, int dimY, long[] pos,
                        double scale, double min, double normFactor) {

                int rgbDim = -1;

                if (m_rgbDim == -1) {
                        for (int i = 0; i < img.numDimensions(); i++) {
                                if (img.dimension(i) > 1
                                                && img.dimension(i) < 4) {
                                        rgbDim = i;
                                        break;
                                }
                        }
                } else {
                        rgbDim = m_rgbDim;
                }

                if (rgbDim == -1 || img.dimension(rgbDim) > 3
                                || img.dimension(rgbDim) == 1 || rgbDim == dimX
                                || rgbDim == dimY) {
                        return new GreyImgRenderer<T>().render(img, dimX, dimY,
                                        pos, scale, min, normFactor);
                }

                int numRGBDims = (int) img.dimension(rgbDim);

                // long[] newDim = new long[2];

                BufferedImage[] awtImages = new BufferedImage[numRGBDims];
                long[] tmp = pos.clone();
                GreyImgRenderer<T> sliceRend = new GreyImgRenderer<T>();
                for (int i = 0; i < numRGBDims; i++) {
                        tmp[rgbDim] = i;

                        // byte[] slice = extractSliceByte(img, dimX, dimY, tmp,
                        // newDim,
                        // scale, min, normFactor);
                        //
                        // awtImages[i] = AWTImageTools.constructImage(1,
                        // DataBuffer.TYPE_BYTE, (int) newDim[0], (int)
                        // newDim[1],
                        // true, false, new DataBufferByte(slice,
                        // slice.length));
                        awtImages[i] = sliceRend.render(img, dimX, dimY, tmp,
                                        scale, min, normFactor);

                }

                return loci.formats.gui.AWTImageTools.mergeChannels(awtImages);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
                return "RGB Image renderer (RGB-Dim:" + m_rgbDim + ")";
        }

}
