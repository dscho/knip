package org.knime.knip.core.ops.type;

import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;

public class RealTypeClipper<T extends RealType<T>> implements
                UnaryOutputOperation<T, T> {

        private final double max;
        private final double min;

        public RealTypeClipper(double min, double max) {
                this.min = min;
                this.max = max;
        }

        @Override
        public T compute(T input, T output) {
                double in = input.getRealDouble();
                if (in < min) {
                        output.setReal(min);
                } else if (in > max) {
                        output.setReal(max);
                } else {
                        output.set(input);
                }

                return output;
        }

        @Override
        public UnaryOutputOperation<T, T> copy() {
                return new RealTypeClipper<T>(min, max);
        }

        @Override
        public UnaryObjectFactory<T, T> bufferFactory() {
                return new UnaryObjectFactory<T, T>() {

                        @Override
                        public T instantiate(T a) {
                                return a.createVariable();
                        }
                };
        }

}