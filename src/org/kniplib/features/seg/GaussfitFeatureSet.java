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

import org.kniplib.data.fit.Fitter2DFunction;
import org.kniplib.data.fit.Gaussian2D;
import org.kniplib.features.FeatureSet;
import org.kniplib.features.FeatureTargetListener;
import org.kniplib.ops.interval.Fitter2D;

/**
 *
 * @author tcriess, University of Konstanz
 */
public class GaussfitFeatureSet<T extends RealType<T>> implements
                FeatureSet {

        private static final int MAXEVAL = 200000;

        /**
         * feature names
         */
        public static final String[] FEATURES = new String[] { "Center Dim 0",
                        "Center Dim 1", "Amplitude" };

        private IterableInterval<T> m_interval;

        private Fitter2D<T> m_fitter;

        private double[] m_params = null;

        private final Fitter2DFunction<T> m_function;

        private boolean m_updated = false;

        /**
         * @param target
         */
        public GaussfitFeatureSet() {
                super();
                m_function = new Gaussian2D<T>();
        }

        @FeatureTargetListener
        public void iiUpdated(IterableInterval<T> interval) {
                if (interval.numDimensions() < 2) {
                        throw new IllegalArgumentException(
                                        "Gauss fit feature needs at least 2 dimensions");
                }
                m_interval = interval;
                m_params = null;
                m_updated = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double value(int id) {

                if (m_updated) {
                        double[] init = m_function.init(m_interval);
                        m_fitter = new Fitter2D<T>(m_function, init, MAXEVAL);
                        m_params = m_fitter.compute(m_interval);
                        if (!Double.isNaN(m_params[0])) {
                                // check if center is inside the interval -> set
                                // to NaN if it is
                                // outside
                                if (m_params[4] < m_interval.min(0)
                                                || m_params[4] > m_interval
                                                                .max(0)
                                                || m_params[5] < m_interval
                                                                .min(1)
                                                || m_params[5] > m_interval
                                                                .max(1)) {
                                        for (int i = 0; i < m_params.length; i++) {
                                                m_params[i] = Double.NaN;
                                                // m_params[i] = init[i];
                                        }
                                }
                        }
                        m_updated = false;
                }

                switch (id) {
                case 0:
                        // x
                        return m_params.length > 4 ? m_params[4] : 0;
                case 1:
                        // y
                        return m_params.length > 5 ? m_params[5] : 0;
                case 2:
                        // Amplitude
                        return m_params.length > 0 ? m_params[0] : 0;

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
                return "Gauss Fitting Feature Factory";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void enable(int id) {
                // nothing to do here

        }

}
