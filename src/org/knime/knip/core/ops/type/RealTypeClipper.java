package org.knime.knip.core.ops.type;

import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;

public class RealTypeClipper<T extends RealType<T>> implements UnaryOutputOperation<T, T> {

    private final double m_max;

    private final double m_min;

    public RealTypeClipper(final double min, final double max) {
        this.m_min = min;
        this.m_max = max;
    }

    @Override
    public T compute(final T input, final T output) {
        final double in = input.getRealDouble();
        if (in < m_min) {
            output.setReal(m_min);
        } else if (in > m_max) {
            output.setReal(m_max);
        } else {
            output.set(input);
        }

        return output;
    }

    @Override
    public UnaryOutputOperation<T, T> copy() {
        return new RealTypeClipper<T>(m_min, m_max);
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