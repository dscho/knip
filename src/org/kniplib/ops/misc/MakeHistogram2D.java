package org.kniplib.ops.misc;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.ops.BinaryOutputOperation;
import net.imglib2.type.numeric.RealType;

import org.kniplib.data.statistics.Histogram2D;
import org.kniplib.exceptions.ImageNotCompatibleException;

public final class MakeHistogram2D<T extends RealType<T>>
                implements
                BinaryOutputOperation<IterableInterval<T>, IterableInterval<T>, Histogram2D> {

        @Override
        public final Histogram2D createEmptyOutput(
                        final IterableInterval<T> op0,
                        final IterableInterval<T> op1) {
                return new Histogram2D(op0.iterator().next().createVariable());
        }

        @Override
        public final Histogram2D compute(final IterableInterval<T> op0,
                        final IterableInterval<T> op1, final Histogram2D r) {
                if (!op0.equalIterationOrder(op1)) {
                        throw new ImageNotCompatibleException(
                                        "Intervals are not compatible!");
                }
                Cursor<T> opc0 = op0.cursor();
                Cursor<T> opc1 = op1.cursor();
                r.clear();
                while (opc0.hasNext()) {
                        opc0.next();
                        opc1.next();
                        r.incByValue(opc0.get().getRealDouble(), opc1.get()
                                        .getRealDouble());
                }
                return r;
        }

        public Histogram2D compute(IterableInterval<T> in1,
                        IterableInterval<T> in2) {
                return compute(in1, in2, createEmptyOutput(in1, in1));
        }

        @Override
        public BinaryOutputOperation<IterableInterval<T>, IterableInterval<T>, Histogram2D> copy() {
                return new MakeHistogram2D<T>();
        }
}
