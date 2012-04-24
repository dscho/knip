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
 *   22 Jul 2011 (hornm): created
 */
package org.kniplib.ops.metadata;

import net.imglib2.meta.Metadata;
import net.imglib2.ops.UnaryOutputOperation;

import org.kniplib.data.img.FinalMetadata;

/**
 * 
 * @author hornm, University of Konstanz
 */
public class CopyAxesLabels implements UnaryOutputOperation<Metadata, Metadata> {

        private final int[] m_axisIndicesBeeingRemoved;

        private final int m_numNewDimensions;

        public CopyAxesLabels(int numNewDimensions,
                        int... axisIndicesBeeingRemoved) {
                m_axisIndicesBeeingRemoved = axisIndicesBeeingRemoved;
                m_numNewDimensions = numNewDimensions;

        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        @Override
        public Metadata compute(Metadata op, Metadata r) {
                int remIdx = 0;
                int newIdx = 0;
                for (int d = 0; d < m_numNewDimensions
                                && remIdx < m_axisIndicesBeeingRemoved.length; d++) {
                        if (d == m_axisIndicesBeeingRemoved[remIdx]) {
                                r.setAxis(op.axis(d + 1), newIdx);
                                remIdx++;
                        } else {
                                r.setAxis(op.axis(d), d);
                                newIdx++;
                        }
                }

                return r;
        }

        @Override
        public Metadata createEmptyOutput(Metadata dataHint) {
                return new FinalMetadata(dataHint.getName(),
                                dataHint.numDimensions());
        }

        @Override
        public UnaryOutputOperation<Metadata, Metadata> copy() {
                return new CopyAxesLabels(m_numNewDimensions,
                                m_axisIndicesBeeingRemoved.clone());
        }

        @Override
        public Metadata compute(Metadata in) {
                return compute(in, createEmptyOutput(in));
        }

}
