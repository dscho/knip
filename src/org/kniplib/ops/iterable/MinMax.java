package org.kniplib.ops.iterable;

import java.util.Iterator;

import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

public final class MinMax<T extends RealType<T>> implements
                UnaryOutputOperation<Iterable<T>, Pair<T, T>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public Pair<T, T> createEmptyOutput(Iterable<T> op) {
                final T t = op.iterator().next();
                return new Pair<T, T>(t.createVariable(), t.createVariable());
        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        @Override
        public Pair<T, T> compute(Iterable<T> op, Pair<T, T> r) {
                final Iterator<T> it = op.iterator();
                r.a.setReal(r.a.getMaxValue());
                r.b.setReal(r.b.getMinValue());
                while (it.hasNext()) {
                        T i = it.next();
                        if (r.a.compareTo(i) > 0)
                                r.a.set(i);
                        if (r.b.compareTo(i) < 0)
                                r.b.set(i);
                }

                return r;
        }

        @Override
        public UnaryOutputOperation<Iterable<T>, Pair<T, T>> copy() {
                return new MinMax<T>();
        }

        @Override
        public Pair<T, T> compute(Iterable<T> in) {
                return compute(in, createEmptyOutput(in));
        }
}
