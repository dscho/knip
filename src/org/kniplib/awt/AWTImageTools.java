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
 *   10 Feb 2010 (hornm): created
 */
package org.kniplib.awt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

import org.kniplib.awt.renderer.GreyImgRenderer;
import org.kniplib.awt.renderer.ImagePlaneProducer;

/**
 * 
 * Methods to convert data (e.g. an imglib image or a histogram) to a
 * java.awt.BufferedImage
 * 
 * @author hornm, University of Konstanz
 */
public final class AWTImageTools {

        private AWTImageTools() {
                // to hide the constructor of this utility class
        }

        /** Creates an image with the given DataBuffer. */
        public static BufferedImage constructImage(int c, int type, int w,
                        int h, boolean interleaved, boolean banded,
                        DataBuffer buffer) {
                if (c > 4) {
                        throw new IllegalArgumentException(
                                        "Cannot construct image with " + c
                                                        + " channels");
                }

                SampleModel model;
                if (c > 2 && type == DataBuffer.TYPE_INT
                                && buffer.getNumBanks() == 1) {
                        int[] bitMasks = new int[c];
                        for (int i = 0; i < c; i++) {
                                bitMasks[i] = 0xff << ((c - i - 1) * 8);
                        }
                        model = new SinglePixelPackedSampleModel(
                                        DataBuffer.TYPE_INT, w, h, bitMasks);
                } else if (banded)
                        model = new BandedSampleModel(type, w, h, c);
                else if (interleaved) {
                        int[] bandOffsets = new int[c];
                        for (int i = 0; i < c; i++)
                                bandOffsets[i] = i;
                        model = new PixelInterleavedSampleModel(type, w, h, c,
                                        c * w, bandOffsets);
                } else {
                        int[] bandOffsets = new int[c];
                        for (int i = 0; i < c; i++)
                                bandOffsets[i] = i * w * h;
                        model = new ComponentSampleModel(type, w, h, 1, w,
                                        bandOffsets);
                }

                WritableRaster raster = Raster.createWritableRaster(model,
                                buffer, null);

                BufferedImage b = null;

                if (c == 1 && type == DataBuffer.TYPE_BYTE) {
                        b = new BufferedImage(w, h,
                                        BufferedImage.TYPE_BYTE_GRAY);

                        b.setData(raster);
                } else if (c == 1 && type == DataBuffer.TYPE_USHORT) {
                        b = new BufferedImage(w, h,
                                        BufferedImage.TYPE_USHORT_GRAY);
                        b.setData(raster);
                } else if (c > 2 && type == DataBuffer.TYPE_INT
                                && buffer.getNumBanks() == 1) {

                        GraphicsEnvironment env = GraphicsEnvironment
                                        .getLocalGraphicsEnvironment();
                        GraphicsDevice device = env.getDefaultScreenDevice();
                        GraphicsConfiguration config = device
                                        .getDefaultConfiguration();
                        b = config.createCompatibleImage(w, h);
                        b.setData(raster);
                        // if (c == 3) {
                        // b = new BufferedImage(w, h,
                        // BufferedImage.TYPE_INT_RGB);
                        // } else if (c == 4) {
                        // b = new BufferedImage(w, h,
                        // BufferedImage.TYPE_INT_ARGB);
                        // }
                        //
                        // if (b != null)
                        // b.setData(raster);
                }

                return b;
        }

        /**
         * Draws the histogram of the {@link ImagePlane} onto a
         * {@link BufferedImage}.
         * 
         * @param ip
         * @param width
         *                the width of the image containing the histogram
         * @param height
         *                the height of the image containing the histogram
         * @return a java-image with the histogram painted on it
         */
        public static BufferedImage drawHistogram(final int[] hist,
                        final int height) {

                int max = 0;
                for (int i = 0; i < hist.length; i++) {
                        max = Math.max(max, hist[i]);
                }
                return drawHistogram(hist, hist.length, height, max, true);
        }

        /**
         * Draws the histogram of the {@link ImagePlane} onto a
         * {@link BufferedImage}.
         * 
         * @param ip
         * @param width
         *                the width of the image containing the histogram
         * @param height
         *                the height of the image containing the histogram
         * @return a java-image with the histogram painted on it
         */
        public static BufferedImage drawHistogram(final int[] hist, int width,
                        final int height, int max, boolean log) {
                // int width = hist.length;
                int margin = 20;

                BufferedImage histImg = new BufferedImage(width, height
                                + margin, BufferedImage.TYPE_BYTE_GRAY);
                Graphics g = histImg.getGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height + margin);
                g.setColor(Color.black);
                int binWidth = width / hist.length;
                double heightScale = (double) height / max;
                double heightScaleLog = height / Math.log(max);
                for (int i = 0; i < hist.length; i++) {
                        // g.drawLine(i, (int) Math.round(height - (hist[i] *
                        // heightScale)),
                        // i, height);

                        if (log) {
                                g.setColor(Color.GRAY);
                                if (hist[i] > 0) {
                                        g.fillRect(i * binWidth,
                                                        (int) Math.round(height
                                                                        - (Math.log(hist[i]) * heightScaleLog)),
                                                        binWidth, height);
                                }
                        }

                        g.setColor(Color.BLACK);
                        g.fillRect(i * binWidth, (int) Math.round(height
                                        - (hist[i] * heightScale)), binWidth,
                                        height);

                        if (hist.length <= 256) {
                                g.setColor(new Color(i, i, i));
                                g.drawLine(i, height + 5, i, height + margin
                                                - 1);
                        }
                }

                return histImg;
        }

        // ---------------- Some other utility methods ------------------//

        /**
         * Creates a blank image with the given message painted on top (e.g., a
         * loading or error message), matching the given size.
         */
        public static BufferedImage makeBufferedImage(final String message,
                        final int width, final int height) {
                int w = width, h = height;
                if (w < 128)
                        w = 128;
                if (h < 32)
                        h = 32;
                BufferedImage image = new BufferedImage(w, h,
                                BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                Rectangle2D.Float r = (Rectangle2D.Float) g.getFont()
                                .getStringBounds(message,
                                                g.getFontRenderContext());
                g.drawString(message, (w - r.width) / 2, (h - r.height) / 2
                                + r.height);
                g.dispose();
                return image;
        }

        /**
         * Adds a subtitle to the given image.
         * 
         * @param source
         * @param txt
         * @return
         */
        public static BufferedImage makeSubtitledBufferedImage(
                        final BufferedImage source, final String txt) {

                Graphics g = source.getGraphics();
                int w = SwingUtilities.computeStringWidth(g.getFontMetrics(),
                                txt) + 5;
                g.setColor(Color.WHITE);
                g.fillRect(source.getWidth() - w, source.getHeight() - 16,
                                source.getWidth(), source.getHeight());
                g.setColor(Color.black);
                g.drawString(txt, source.getWidth() - w + 4,
                                source.getHeight() - 5);
                g.dispose();

                return source;
        }

        /**
         * Shows the first image plane in the dimension 0,1 in a JFrame.
         * 
         * @param ip
         *                the image plane
         * @param title
         *                a title for the frame
         */
        public static <T extends RealType<T>> JFrame showInFrame(
                        final Img<T> img, final String title) {
                return showInFrame(img, title, 1);
        }

        /**
         * Shows first image plane in the dimension 0,1, scaled with the
         * specified factor in a JFrame.
         * 
         * @param ip
         *                the image plane
         * @param factor
         *                the scaling factor
         */
        public static <T extends RealType<T>> JFrame showInFrame(
                        final Img<T> img, String title, final double factor) {
                return showInFrame(img, 0, 1, new long[img.numDimensions()],
                                title, factor);
        }

        /**
         * Shows the selected ImagePlane in a JFrame.
         * 
         * @param img
         *                the image plane
         * @param factor
         *                the scaling factor
         */
        public static <T extends RealType<T>, I extends Img<T>> JFrame showInFrame(
                        final I img, int dimX, int dimY, long[] pos,
                        String title, final double factor) {

                int w = (int) img.dimension(dimX);
                int h = (int) img.dimension(dimY);
                title = title + " (" + w + "x" + h + ")";

                JLabel label = new JLabel();

                JFrame frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(w, h);
                frame.getContentPane().add(label);

                java.awt.Image awtImage = new GreyImgRenderer<T>().render(img,
                                dimX, dimY, pos, 1.0);
                label.setIcon(new ImageIcon(awtImage.getScaledInstance(
                                (int) Math.round(w * factor),
                                (int) Math.round(h * factor),
                                java.awt.Image.SCALE_DEFAULT)));

                frame.pack();
                frame.setVisible(true);
                return frame;
        }

        /**
         * Shows an AWT-image in a JFrame.
         * 
         * @param img
         *                the image to show.
         */

        public static void showInFrame(final java.awt.Image img) {
                showInFrame(img, "", 1);

        }

        public static void showInFrame(final java.awt.Image img, String title,
                        double factor) {

                int w = img.getWidth(null);
                int h = img.getHeight(null);
                title = title + " (" + w + "x" + h + ")";

                JLabel label = new JLabel();

                JFrame frame = new JFrame(title);
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setSize(w, h);
                frame.getContentPane().add(label);

                label.setIcon(new ImageIcon(img.getScaledInstance(
                                (int) Math.round(w * factor),
                                (int) Math.round(h * factor),
                                java.awt.Image.SCALE_DEFAULT)));

                frame.pack();
                frame.setVisible(true);

        }

        private static ImagePlaneProducer planeProd = null;

        /**
         * Shows the images (the first plane of dimensions 0 and 1) in the same
         * frame. Only the first call of this method creates a new window,
         * subsequent calls will show the images in the created window. The
         * first call also defines the scaling factor and will be ignored in
         * further calls.
         * 
         * @param img
         * @param scaleFactor
         */
        public static <T extends RealType<T>> void showInSameFrame(
                        final Img<T> img, final double scaleFactor) {
                if (planeProd != null) {
                        if (img.dimension(0) == planeProd.getImage().dimension(
                                        0)
                                        && img.dimension(1) == planeProd
                                                        .getImage()
                                                        .dimension(1)) {
                                planeProd.updateConsumers(img);
                                return;
                        }

                        planeProd = null;
                }
                JFrame frame = new JFrame();
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                JLabel label = new JLabel();
                frame.getContentPane().add(label);
                planeProd = new ImagePlaneProducer(img);
                String title = " (" + img.dimension(0) + "x" + img.dimension(1)
                                + ")";
                frame.setTitle(title);
                java.awt.Image awtImage = Toolkit.getDefaultToolkit()
                                .createImage(planeProd);
                label.setIcon(new ImageIcon(
                                awtImage.getScaledInstance((int) Math.round(img
                                                .dimension(0) * scaleFactor),
                                                (int) Math.round(img
                                                                .dimension(1)
                                                                * scaleFactor),
                                                java.awt.Image.SCALE_DEFAULT)));
                frame.setSize((int) img.dimension(0), (int) img.dimension(1));
                frame.pack();
                frame.setVisible(true);
        }

}
