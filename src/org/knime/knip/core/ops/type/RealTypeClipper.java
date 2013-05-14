package org.knime.knip.core.ops.type;

import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;

public class RealTypeClipper<T extends RealType<T>> implements
UnaryOutputOperation<T, T> {

    private final double max;
    private final double min;

    public RealTypeClipper(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public T compute(final T input, final T output) {
        final double in = input.getRealDouble();
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
            public T instantiate(final T a) {
                return a.createVariable();
            }
        };
    }

}