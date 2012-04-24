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
 *   1 Aug 2011 (hornm): created
 */
package org.kniplib.data.img;

import net.imglib2.display.ColorTable16;
import net.imglib2.display.ColorTable8;
import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;

/**
 * 
 * @author hornm, University of Konstanz
 */
public final class FinalMetadata implements Metadata {

        private Named m_name = new NamedImpl();

        private Sourced m_source = new SourcedImpl();

        private CalibratedSpace m_cspace;

        /**
         * @param name
         * @param numDims
         */
        public FinalMetadata(String name, int numDims) {
                m_name.setName(name);
                m_cspace = new CalibratedSpaceImpl(numDims);
        }

        /**
         * @param source
         * @param name
         * @param axisLabels
         */
        public FinalMetadata(String name, String source, String... axisLabels) {
                m_name.setName(name);
                m_source.setSource(source);
                m_cspace = new CalibratedSpaceImpl(axisLabels);

        }

        /**
         * @param source
         * @param name
         * @param axes
         * @param calibration
         */
        public FinalMetadata(String name, String source, AxisType[] axes,
                        double[] calibration) {
                m_name.setName(name);
                m_source.setSource(source);
                m_cspace = new CalibratedSpaceImpl(axes, calibration);
        }

        /**
         * @param numDimensions
         * @param name
         * @param calibratedSpace
         * @param axes
         * @param calibration
         */
        public FinalMetadata(String name, String source,
                        CalibratedSpace calibratedSpace) {

                m_name.setName(name);
                m_source.setSource(source);
                m_cspace = new CalibratedSpaceImpl(calibratedSpace);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getName() {
                return m_name.getName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setName(String name) {
                throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getAxisIndex(AxisType axis) {
                return m_cspace.getAxisIndex(axis);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AxisType axis(int d) {
                return m_cspace.axis(d);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void axes(AxisType[] axes) {
                m_cspace.axes(axes);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setAxis(AxisType axis, int d) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double calibration(int d) {
                return m_cspace.calibration(d);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void calibration(double[] cal) {
                m_cspace.calibration(cal);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCalibration(double cal, int d) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getValidBits() {
                return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValidBits(int bits) {
                throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getChannelMinimum(int c) {
                return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setChannelMinimum(int c, double min) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getChannelMaximum(final int c) {
                return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setChannelMaximum(int c, double max) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCompositeChannelCount() {
                return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setCompositeChannelCount(int count) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ColorTable8 getColorTable8(int no) {
                return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setColorTable(ColorTable8 lut, int no) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ColorTable16 getColorTable16(int no) {
                return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setColorTable(ColorTable16 lut, int no) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void initializeColorTables(int count) {
                throw new UnsupportedOperationException();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getColorTableCount() {
                return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int numDimensions() {
                return m_cspace.numDimensions();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSource(String source) {
                throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSource() {
                return m_source.getSource();
        }

}
