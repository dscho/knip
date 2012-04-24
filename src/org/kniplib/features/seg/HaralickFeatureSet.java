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
 *   20 Sep 2010 (hornm): created
 */
package org.kniplib.features.seg;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

import org.kniplib.data.statistics.SecondOrderStatistics;
import org.kniplib.features.FeatureSet;
import org.kniplib.features.FeatureTargetListener;
import org.kniplib.features.ObjectCalcAndCache;
import org.kniplib.features.SharesObjects;

/**
 * 
 * @author dietzc, hornm, University of Konstanz
 */
public class HaralickFeatureSet<T extends RealType<T>> implements FeatureSet,
                SharesObjects {

        private final int m_distance;

        private final int m_nrGrayLevels;

        private SecondOrderStatistics<T> m_sos;

        private final MatrixOrientation m_matrixOrientation;

        private ObjectCalcAndCache m_ocac;

        /**
         * Names of the available features.
         */
        public static final String[] FEATURES = { "ASM", "Contrast",
                        "Correlation", "Variance", "IDFM", "SumAverage",
                        "SumVariance", "SumEntropy", "Entropy",
                        "DifferenceVariance", "DifferenceEntropy", "ICM1",
                        "ICM2" };

        private IterableInterval<T> m_interval;

        /**
         * @param nrGrayLevels
         * @param distance
         * @param target
         */
        public HaralickFeatureSet(int nrGrayLevels, int distance,
                        MatrixOrientation orientation) {
                super();
                m_nrGrayLevels = nrGrayLevels;
                m_distance = distance;
                m_matrixOrientation = orientation;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double value(int id) {

                m_sos = m_ocac.secondOrderStatistics(m_interval,
                                m_nrGrayLevels, m_distance, m_matrixOrientation);

                switch (id) {
                case 0:
                        return m_sos.getASM();
                case 1:
                        return m_sos.getContrast();
                case 2:
                        return m_sos.getCorrelation();
                case 3:
                        return m_sos.getVariance();
                case 4:
                        return m_sos.getIDFM();
                case 5:
                        return m_sos.getSumAverage();
                case 6:
                        return m_sos.getSumVariance();
                case 7:
                        return m_sos.getSumEntropy();
                case 8:
                        return m_sos.getEntropy();
                case 9:
                        return m_sos.getDifferenceVariance();
                case 10:
                        return m_sos.getDifferenceEntropy();
                case 11:
                        return m_sos.getICM1();
                case 12:
                        return m_sos.getICM2();
                default:
                        return 0;
                }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String name(int id) {
                return FEATURES[id];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int numFeatures() {
                return FEATURES.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String featureSetId() {
                return "Haralick Feature Factory";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void enable(int id) {
                // nothing to do here

        }

        @FeatureTargetListener
        public void iiUpdated(IterableInterval<T> interval) {
                m_interval = interval;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<?>[] getSharedObjectClasses() {
                return new Class[] { ObjectCalcAndCache.class };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setSharedObjectInstances(Object[] instances) {
                m_ocac = (ObjectCalcAndCache) instances[0];

        }

}
