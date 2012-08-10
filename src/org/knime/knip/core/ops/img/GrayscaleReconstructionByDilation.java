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
 */
package org.knime.knip.core.ops.img;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.types.ConnectedType;

/**
 * @author muethingc, University of Konstanz
 */
public class GrayscaleReconstructionByDilation<T extends RealType<T>, V extends RealType<V>, MASK extends RandomAccessibleInterval<T>, MARKER extends RandomAccessibleInterval<V>> extends AbstractGrayscaleReconstruction<T, V, MASK, MARKER> {

    public GrayscaleReconstructionByDilation(final ConnectedType connection) {
        super(connection);
    }

        public GrayscaleReconstructionByDilation(
                        final GrayscaleReconstructionByDilation<T, V, MASK, MARKER> copy) {
                super(copy);
        }

    @Override
    protected final boolean checkPixelFromQueue(final V p, final V q, final T i) {
        double pd = p.getRealDouble();
        double qd = q.getRealDouble();
        double id = i.getRealDouble();

        if (qd < pd && qd != id) return true;
        else return false;
    }

    @Override
    protected final V morphOp(final V a, final V b) {
        if (a.getRealDouble() > b.getRealDouble()) return a;
        else return b;
    }

    @Override
    protected final V pointwiseOp(final V a, final T b) {
        if (a.getRealDouble() < b.getRealDouble()) {
            return a;
        } else {
            V r = a.createVariable();
            r.setReal(b.getRealDouble());
            return r;
        }
    }

    @Override
    protected final boolean checkPixelAddToQueue(final V p, final V q, final T i) {
        double pd = p.getRealDouble();
        double qd = q.getRealDouble();
        double id = i.getRealDouble();

        if (qd < pd && qd < id) return true;
        else return false;
    }

    @Override
    protected V getVMinValue(final V var) {
        var.setReal(var.getMinValue());
        return var;
    }

    @Override
    protected T getTMinValue(final T var) {
        var.setReal(var.getMinValue());
        return var;
    }

	@Override
	public UnaryOperation<MASK, MARKER> copy() {
                return new GrayscaleReconstructionByDilation<T, V, MASK, MARKER>(
                                this);
	}
}