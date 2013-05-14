package org.knime.knip.core.ops.interval;

import net.imglib2.IterableInterval;
import net.imglib2.ops.img.UnaryRelationAssigment;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterableinterval.unary.MakeHistogram;
import net.imglib2.ops.operation.iterableinterval.unary.OpsHistogram;
import net.imglib2.ops.relation.real.unary.RealGreaterThanConstant;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.knime.knip.core.algorithm.types.ThresholdingType;
import org.knime.knip.core.ops.misc.FindThreshold;

public final class AutoThreshold<T extends RealType<T>, I extends IterableInterval<T>, K extends IterableInterval<BitType>>
implements UnaryOperation<I, K> {

    private final ThresholdingType m_thresholdType;

    public AutoThreshold(final ThresholdingType thresholdType) {
        m_thresholdType = thresholdType;
    }

    @Override
    public K compute(final I op, final K r) {

        final OpsHistogram hist = Operations.compute(new MakeHistogram<T>(), op);
        final T thresh = op.firstElement().createVariable();
        thresh.setReal(new FindThreshold<T>(m_thresholdType).compute(hist, new DoubleType()).getRealDouble());
        new UnaryRelationAssigment<T>(new RealGreaterThanConstant<T>(thresh)).compute(op, r);
        return r;
    }

    @Override
    public UnaryOperation<I, K> copy() {
        return new AutoThreshold<T, I, K>(m_thresholdType);
    }
}
