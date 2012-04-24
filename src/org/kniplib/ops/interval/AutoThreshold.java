package org.kniplib.ops.interval;

import net.imglib2.IterableInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.image.UnaryRelationAssigment;
import net.imglib2.ops.relation.unary.RealGreaterThanConstant;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.kniplib.algorithm.types.ThresholdingType;
import org.kniplib.data.statistics.KNIPLibHistogram;
import org.kniplib.ops.iterable.MakeHistogram;
import org.kniplib.ops.misc.FindThreshold;

public final class AutoThreshold<T extends RealType<T>, I extends IterableInterval<T>, K extends IterableInterval<BitType>>
                implements UnaryOperation<I, K> {

        private final ThresholdingType m_thresholdType;

        public AutoThreshold(ThresholdingType thresholdType) {
                m_thresholdType = thresholdType;
        }

        @Override
        public K compute(I op, K r) {
                MakeHistogram<T> makeHistogram = new MakeHistogram<T>();

                KNIPLibHistogram hist = new MakeHistogram<T>().compute(op,
                                makeHistogram.createEmptyOutput(op));
                T thresh = op.firstElement().createVariable();
                thresh.setReal(new FindThreshold<T>(m_thresholdType).compute(
                                hist, new DoubleType()).getRealDouble());
                new UnaryRelationAssigment<T>(new RealGreaterThanConstant<T>(
                                thresh)).compute(op, r);
                return r;
        }

        @Override
        public UnaryOperation<I, K> copy() {
                return new AutoThreshold<T, I, K>(m_thresholdType);
        }
}
