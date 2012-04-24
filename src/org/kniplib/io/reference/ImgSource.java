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
 *   2 Mar 2010 (hornm): created
 */
package org.kniplib.io.reference;

import java.awt.image.BufferedImage;

import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.meta.AxisType;
import net.imglib2.type.numeric.RealType;

/**
 * Provides methods to get the actual data (image, metadata) from a specific
 * image reference.
 * 
 * Make sure that the implementing class synchronizes the methods if the image
 * source is used together with the {@link ImgRefCell} etc.
 * 
 * @author hornm, dietzc University of Konstanz
 */
public interface ImgSource {

        /**
         * @param <T>
         * @param imgRef
         *                description of the exact {@link Img} source (URL, ...)
         * @return the complete image
         * @throws Exception
         */
        public <T extends RealType<T>> Img<T> getImg(String imgRef)
                        throws Exception;

        /**
         * Retrieves the sub image at the given interval. In case of slower
         * image sources, the image planes should be created when needed.
         * 
         * @param imgRef
         *                description of the exact {@link Img} source (URL, ...)
         * @param <T>
         * @param interval
         * 
         * 
         * @return the image plane
         * @throws Exception
         *                 the appropriate exception, if the image can't be
         *                 retrieved
         */
        public <T extends RealType<T>> Img<T> getImg(String imgRef,
                        Interval interval) throws Exception;

        /**
         * Retrieves the sub image at the given interval. In case of slower
         * image sources, the image planes should be created when needed.
         * 
         * @param imgRef
         *                description of the exact image source (URL, ...)
         * @param <T>
         * @param interval
         * 
         * 
         * @return the image plane
         * @throws Exception
         *                 the appropriate exception, if the image can't be
         *                 retrieved
         */
        public <T extends RealType<T>> Img<T> getImg(String imgRef,
                        Interval[] interval) throws Exception;

        /**
         * @param <T>
         * @param imgRef
         *                description of the exact image source (URL, ...)
         * @param height
         * @param interval
         * @return a 2-dimension small representation of the original image
         * @throws Exception
         */
        public BufferedImage getThumbnail(String imgRef, int height)
                        throws Exception;

        /**
         * 
         * @param imgRef
         *                description of the exact image source (URL, ...)
         * @return the size of the image object behind the reference
         * @throws Exception
         * 
         */
        public long[] getDimensions(String imgRef) throws Exception;

        /**
         * The Axes of the {@link Img}
         * 
         * @param imgRef
         *                description of the exact image source (URL, ...)
         * @return
         * @throws Exception
         */
        public AxisType[] getAxes(String imgRef) throws Exception;

        /**
         * The Calibration of the {@link Img}
         * 
         * @param m_imgRef
         *                description of the exact image source (URL, ...)
         * @return
         * @throws Exception
         */
        public double[] getCalibration(String m_imgRef) throws Exception;

        /**
         * The name of the img
         * 
         * @param imgRef
         *                description of the exact image source (URL, ...)
         * @return the image file name
         * @throws Exception
         */
        public String getName(String imgRef) throws Exception;

        /**
         * The source path of the img
         * 
         * @param imgRef
         *                description of the exact image source (URL, ...)
         * @return the source path
         * @throws Exception
         */
        public String getSource(String imgRef) throws Exception;

        /**
         * @return a short string, which describes this source.
         */
        public String getDescription();

        /**
         * Closes the source. It should be used to close opened files or
         * connections.
         */
        public void close();

        /**
         * 
         * @return The type T of the img to read
         */
        public <T extends RealType<T>> T getSourceType(String imgRef)
                        throws Exception;

}
