package org.kniplib.ops.iterable;

import java.util.Iterator;

import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class Sum<I extends RealType<I>> implements
                UnaryOutputOperation<Iterable<I>, DoubleType> {

        @Override
        public DoubleType compute(Iterable<I> op, DoubleType r) {
                final Iterator<I> it = op.iterator();
                double sum = 0;
                while (it.hasNext()) {
                        sum += it.next().getRealDouble();
                }
                r.setReal(sum);

                return r;
        }

        @Override
        public UnaryOutputOperation<Iterable<I>, DoubleType> copy() {
                return new Sum<I>();
        }

        @Override
        public DoubleType createEmptyOutput(Iterable<I> in) {
                return new DoubleType();
        }

        @Override
        public DoubleType compute(Iterable<I> in) {
                return compute(in, createEmptyOutput(in));
        }
}
