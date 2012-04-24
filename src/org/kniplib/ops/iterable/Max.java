package org.kniplib.ops.iterable;

import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

public class Max<T extends RealType<T>> implements
                UnaryOutputOperation<Iterable<T>, DoubleType> {

        @Override
        public DoubleType compute(Iterable<T> input, DoubleType output) {
                double max = Double.MIN_VALUE;
                for (T in : input) {
                        if (in.getRealDouble() > max)
                                max = in.getRealDouble();
                }

                output.setReal(max);
                return output;
        }

        @Override
        public UnaryOutputOperation<Iterable<T>, DoubleType> copy() {
                return new Max<T>();
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
