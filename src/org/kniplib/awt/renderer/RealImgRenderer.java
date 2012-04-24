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
 */
package org.kniplib.awt.renderer;

import java.awt.image.BufferedImage;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.labeling.Labeling;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ui.imgviewer.events.NormalizationParametersChgEvent;

/**
 * 
 * Abstract renderer class for {@link Img} of {@link RealType}
 * 
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 * @param <T>
 * @param <I>
 */
public abstract class RealImgRenderer<T extends RealType<T>, I extends Img<T>>
                implements ImgRenderer<T, I> {

        /**
         * {@inheritDoc}
         */
        @Override
        public BufferedImage render(I img, int dimX, int dimY, long[] pos,
                        double scale) {
                return render(img, dimX, dimY, pos, scale, img.firstElement()
                                .getMinValue(), 1);

        }

        /**
         * Rendering a {@link Labeling} under consideration of
         * {@link NormalizationParametersChgEvent}
         * 
         * {@link #render(Img, int, int, long[], double)}
         * 
         * @param normalize
         *                true, if the image should be normalized
         * @param saturation
         *                the percentage of pixels in the lower and upper domain
         *                to be ignored in the normalization, will be ignored if
         *                normalize=false
         * 
         */
        public abstract BufferedImage render(I img, int dimX, int dimY,
                        long[] pos, double scale, double min,
                        double normalizationFactor);

        /**
         * Extracts a plane as a byte array.
         */
        protected static final <T extends RealType<T>> byte[] extractSliceByte(
                        final Img<T> img, final int dimX, final int dimY,
                        final long[] dimensionPositions, long[] newDim,
                        double scale, double min, double normalizationFactor) {

                newDim[0] = Math.max(Math.round(img.dimension(dimX) * scale), 1);
                newDim[1] = Math.max(Math.round(img.dimension(dimY) * scale), 1);

                // store the slice image
                byte[] sliceImg = new byte[(int) (newDim[0] * newDim[1])];

                RandomAccess<T> ra = img.randomAccess();
                ra.setPosition(dimensionPositions);
                int i = 0;
                while (i < sliceImg.length) {

                        sliceImg[i] = (byte) Math.round((normRealType(ra.get(),
                                        min, normalizationFactor) * 255));
                        i++;

                        ra.setPosition((long) Math.min(img.max(dimX), i
                                        % newDim[0] / scale), dimX);
                        if (i % newDim[0] == 0) {
                                ra.setPosition(0, dimX);
                                ra.setPosition((long) Math.min(img.max(dimY), i
                                                / newDim[0] / scale), dimY);
                        }
                }

                return sliceImg;
        }

        private static final <T extends RealType<T>> double normRealType(
                        T type, double localMin, double normalizationFactor) {

                double value = ((type.getRealDouble() - localMin) * normalizationFactor);

                // normalize to be between 0 and 1
                value = value / (type.getMaxValue() - type.getMinValue());

                if (value < 0)
                        value = 0;
                else if (value > 1)
                        value = 1;

                return value;
        }

        protected static final <T extends RealType<T>> Img<T> findSourceImg(
                        Img<T> img) {

                while (img instanceof ImgPlus) {
                        img = ((ImgPlus<T>) img).getImg();
                }

                return img;
        }
}
