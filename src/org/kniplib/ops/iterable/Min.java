package org.kniplib.ops.iterable;

import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.numeric.RealType;

public class Min<T extends RealType<T>> implements
                UnaryOperation<Iterable<T>, T> {

        @Override
        public T compute(Iterable<T> input, T output) {
                double min = Double.MAX_VALUE;
                for (T in : input) {
                        if (in.getRealDouble() < min)
                                min = in.getRealDouble();
                }

                output.setReal(min);

                return output;
        }

        @Override
        public UnaryOperation<Iterable<T>, T> copy() {
                return new Min<T>();
        }

}
