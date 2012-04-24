/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003, 2010
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
 *
 */
package org.kniplib.ops.img;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

public class ColorFiller<T extends RealType<T>, I extends RandomAccessible<T> & Interval>
                implements UnaryOperation<I, I> {

        public enum ComparisonType {
                GREATER_THAN, LOWER_THAN, EQUALS
        }

        public enum DirectionType {
                UP, DOWN, RIGHT, LEFT
        }

        final static private double EPS = 0.01;

        private double m_color;

        private long m_width, m_height;

        private String m_comparison, m_direction;

        public ColorFiller(double color, String comparison, String direction) {
                this.m_color = color;
                this.m_comparison = comparison;
                this.m_direction = direction;
        }

        public I compute(I op, I r) {
                RandomAccess<T> access = r.randomAccess();

                m_width = r.dimension(0);
                m_height = r.dimension(1);

                long outer = -1;
                long inner = -1;
                boolean filling = false;

                while (hasNextOuter(outer)) {
                        outer++;

                        access.setPosition(outer, dimension(false));
                        filling = false;
                        inner = initiateInner();

                        while (hasNextInner(inner)) {
                                inner = fwd(inner);
                                access.setPosition(inner, dimension(true));

                                if (filling == false) {
                                        if (fitsComparison(access.get()
                                                        .getRealDouble())) {
                                                filling = true;
                                        }
                                } else {
                                        access.get().setReal(m_color);
                                }
                        }
                }

                return r;
        }

        protected boolean fitsComparison(double pixCol) {
                if (m_comparison == "GREATER_THAN")
                        return pixCol > m_color;
                if (m_comparison == "SMALLER_THAN")
                        return pixCol < m_color;

                return Math.abs(pixCol - m_color) < EPS;
        }

        protected int dimension(boolean inner) {
                return (!inner && (m_direction == "UP" || m_direction == "DOWN"))
                                || (inner && (m_direction == "RIGHT" || m_direction == "LEFT")) ? 0
                                : 1;
        }

        protected boolean hasNextOuter(long p) {
                if (m_direction == "UP" || m_direction == "DOWN")
                        return p < m_width - 1;
                return p < m_height - 1;
        }

        protected boolean hasNextInner(long p) {
                if (m_direction == "UP" || m_direction == "LEFT")
                        return p > 0;
                if (m_direction == "DOWN")
                        return p < m_height - 1;
                return p < m_width - 1;
        }

        protected long initiateInner() {
                if (m_direction == "UP")
                        return m_height;
                if (m_direction == "LEFT")
                        return m_width;
                return -1;
        }

        protected long fwd(long p) {
                if (m_direction == "DOWN" || m_direction == "RIGHT")
                        return ++p;
                return --p;

        }

        @Override
        public UnaryOperation<I, I> copy() {
                return new ColorFiller<T, I>(m_color, m_comparison, m_direction);
        }
}
