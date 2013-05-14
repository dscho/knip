package org.knime.knip.core.ops.filters;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class DirectionalGradient<T extends RealType<T>, K extends RandomAccessibleInterval<T> & IterableInterval<T>>
        implements UnaryOperation<K, K> {

    // TODO: Can be extended to diagonalA and diagonalB
    public enum GradientDirection {
        HORIZONTAL, VERTICAL;
    }

    private final int[] m_dims;

    private final boolean m_invert;

    /**
     * @param direction
     * @param invert inverts the gradient calculation, if false, the difference is calculated as left-right, else
     *            right-left
     */
    public DirectionalGradient(final GradientDirection direction, final boolean invert) {
        m_invert = invert;

        m_dims = new int[2];

        switch (direction) {
            case HORIZONTAL:
                m_dims[0] = 1;
                m_dims[1] = 0;
                break;
            case VERTICAL:
                m_dims[1] = 1;
                m_dims[0] = 0;
                break;
            default:
                break;
        }

    }

    @Override
    public K compute(final K op, final K r) {
        if (op.numDimensions() != 2) {
            throw new IllegalArgumentException("Operation can only be performed on 2 dimensional images");
        }

        final double max = op.firstElement().getMaxValue();
        final double min = op.firstElement().getMinValue();

        final RandomAccess<T> opLeftRndAccess = Views.extendMirrorDouble(op).randomAccess();
        final RandomAccess<T> opRightRndAccess = Views.extendMirrorDouble(op).randomAccess();
        final RandomAccess<T> resAccess = r.randomAccess();

        double diff;
        for (int y = 0; y < op.dimension(m_dims[0]); y++) {
            opLeftRndAccess.setPosition(y, m_dims[0]);
            opRightRndAccess.setPosition(y, m_dims[0]);
            resAccess.setPosition(y, m_dims[0]);

            opLeftRndAccess.setPosition(-1, m_dims[1]);
            opRightRndAccess.setPosition(1, m_dims[1]);
            resAccess.setPosition(0, m_dims[1]);

            for (int x = 0; x < op.dimension(m_dims[1]); x++) {

                if (m_invert) {
                    diff = (opRightRndAccess.get().getRealDouble() - opLeftRndAccess.get().getRealDouble()) + min;
                } else {
                    diff = (opLeftRndAccess.get().getRealDouble() - opRightRndAccess.get().getRealDouble()) + min;
                }

                resAccess.get().setReal(Math.max(min, Math.min(max, diff)));

                opLeftRndAccess.fwd(m_dims[1]);
                opRightRndAccess.fwd(m_dims[1]);
                resAccess.fwd(m_dims[1]);
            }
        }
        return r;
    }

    @Override
    public UnaryOperation<K, K> copy() {
        return new DirectionalGradient<T, K>(GradientDirection.HORIZONTAL, m_invert);
    }
}
