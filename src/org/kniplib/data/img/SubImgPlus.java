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
 *   29 Apr 2011 (hornm): created
 */
package org.kniplib.data.img;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Interval;
import net.imglib2.img.ImgPlus;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.Type;

/**
 * Helper class to create a sub image.
 * 
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 */
public class SubImgPlus<T extends Type<T>> extends ImgPlus<T> {

        /**
         * @see SubImg
         * 
         * 
         * @TODO: Metadata ggf. erweitern
         */
        public SubImgPlus(ImgPlus<T> srcImg, Interval interval) {
                this(srcImg, interval, false);
        }

        /**
         * @see SubImg
         * 
         * @TODO: Metadata ggf. erweitern
         */
        public SubImgPlus(ImgPlus<T> srcImg, Interval interval,
                        boolean keepDimsWithSizeOne) {
                super(new SubImg<T>(srcImg, interval, keepDimsWithSizeOne),
                                srcImg);

                int d = 0;
                List<Integer> selectedDims = new ArrayList<Integer>();
                for (int i = 0; i < interval.numDimensions(); i++) {
                        if (interval.dimension(i) > 1 || keepDimsWithSizeOne) {
                                setAxis(srcImg.axis(i), d);
                                setCalibration(srcImg.calibration(i), d);
                                selectedDims.add(i);

                                d++;
                        }
                }

        }

}
