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
 *   29 Jun 2011 (hornm): created
 */
package org.kniplib.awt.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.Type;

/**
 * 
 * @author dietzc, hornm, schonenbergerf University of Konstanz
 * @param <T>
 */
public class DetailedImgRenderer<T extends Type<T>, II extends IterableInterval<T> & RandomAccessible<T>>
                implements ImgRenderer<T, II> {

        /* for source images below that size, no details will be shown */
        private static final Dimension MIN_SIZE = new Dimension(150, 150);

        private final ImgRenderer<T, II> m_imgRenderer;

        /**
         * @param imgRenderer
         */
        public DetailedImgRenderer(ImgRenderer<T, II> imgRenderer) {
                m_imgRenderer = imgRenderer;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BufferedImage render(II img, int dimX, int dimY, long[] pos,
                        double scale) {
                return render(img, null, null, null, null, dimX, dimY, pos,
                                scale);
        }

        /**
         * @param img
         * @param axes
         *                can be <code>null</code>
         * @param name
         *                can be <code>null</code>
         * @param orgDims
         * @param dimX
         * @param dimY
         * @param pos
         * @param scale
         * @return
         */
        public BufferedImage render(II img, CalibratedSpace axes, Named name,
                        Sourced source, long[] orgDims, int dimX, int dimY,
                        long[] pos, double scale) {

                if (orgDims == null) {
                        orgDims = new long[img.numDimensions()];
                        img.dimensions(orgDims);
                }

                // create information string
                int numDim = orgDims.length;
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < numDim; i++) {
                        if (axes != null) {
                                sb.append("Size " + axes.axis(i).getLabel()
                                                + "=" + orgDims[i] + "\n");
                        } else {
                                sb.append("Size " + i + "=" + orgDims[i] + "\n");
                        }
                }
                sb.append("Pixel Type="
                                + img.firstElement().getClass().getSimpleName()
                                + "\n");

                Img<T> tmpImg = null;

                if (img instanceof SubImg) {
                        tmpImg = ((SubImg<T>) img).getImg();
                } else if (img instanceof ImgPlus) {
                        tmpImg = ((ImgPlus<T>) img).getImg();
                }

                while (true) {
                        if (tmpImg instanceof SubImg) {
                                tmpImg = ((SubImg<T>) tmpImg).getImg();
                        } else if (tmpImg instanceof ImgPlus) {
                                tmpImg = ((ImgPlus<T>) tmpImg).getImg();
                        } else {
                                sb.append("Image Type="
                                                + img.getClass()
                                                                .getSimpleName()
                                                + "\n");
                                break;
                        }

                        break;
                }
                if (name != null) {
                        sb.append("Image Name=" + name.getName());
                }

                if (source != null) {
                        sb.append("Image Source=" + source.getSource());
                }
                int lineHeight = 15;
                int posX = 10;
                String[] tmp = sb.toString().split("\n");

                // render image and created information string
                BufferedImage srcBufImg = m_imgRenderer.render(img, dimX, dimY,
                                pos, scale);

                if (srcBufImg.getWidth() < MIN_SIZE.width
                                || srcBufImg.getHeight() < MIN_SIZE.height) {
                        return srcBufImg;
                }

                BufferedImage res = new BufferedImage(
                                (int) (img.dimension(dimX) * scale),
                                (int) (img.dimension(dimY) * scale),
                                BufferedImage.TYPE_INT_RGB);

                Graphics g = res.createGraphics();
                g.drawImage(srcBufImg, 0, 0, null);
                g.setXORMode(Color.black);
                for (int i = 0; i < tmp.length; i++) {
                        g.drawString(tmp[i], posX, srcBufImg.getHeight()
                                        - (tmp.length - i) * lineHeight);
                }
                return res;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
                return m_imgRenderer.toString() + " (detailed)";
        }
}
