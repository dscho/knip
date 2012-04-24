package org.kniplib.ops.iterable;

import java.util.Iterator;

import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class Mean<T extends RealType<T>> implements
                UnaryOutputOperation<Iterable<T>, DoubleType> {

        @Override
        public DoubleType compute(Iterable<T> op, DoubleType r) {
                final Iterator<T> it = op.iterator();
                double sum = 0;
                double ctr = 0;
                while (it.hasNext()) {
                        sum += it.next().getRealDouble();
                        ctr++;
                }
                r.setReal(sum / ctr);

                return r;
        }

        @Override
        public UnaryOutputOperation<Iterable<T>, DoubleType> copy() {
                return new Mean<T>();
        }

        @Override
        public DoubleType createEmptyOutput(Iterable<T> in) {
                return new DoubleType();
        }

        @Override
        public DoubleType compute(Iterable<T> in) {
                return compute(in, createEmptyOutput(in));
        }
}
