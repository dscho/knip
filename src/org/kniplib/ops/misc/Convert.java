package org.kniplib.ops.misc;

import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;

import org.kniplib.types.NativeTypes;

public final class Convert<I extends RealType<I>, O extends RealType<O>>
                implements UnaryOutputOperation<I, O> {

        private static final int MODE_CLIP = 0;

        private static final int MODE_SCALE = 1;

        private static final int MODE_OVERFLOW = 2;

        private final O m_type;

        private final int m_mode;

        private final double m_in_min;

        private final double m_out_max;

        private final double m_out_min;

        private final double m_factor;

        /**
         * Convert to the new type.
         * 
         * @param type
         *                The new type.
         */
        public Convert(final O type) {
                this(type, null);
        }

        // public Convert(final NativeTypes type) {
        // this(type, null);
        // }

        public Convert(final O type, final boolean clip) {
                m_mode = clip ? MODE_CLIP : MODE_OVERFLOW;
                m_type = type;
                m_out_max = type.getMaxValue();
                m_out_min = type.getMinValue();
                m_in_min = 0;
                m_factor = 0;
        }

        // public Convert(final NativeTypes type, final boolean clip) {
        // this((O) type.getTypeInstance(), clip);
        // }

        /**
         * Convert to the new type. Scale values with respect to the old type
         * range.
         * 
         * @param type
         *                The new type.
         * @param scaleFrom
         *                The old type.
         */
        public Convert(final O type, final I scaleFrom) {
                m_type = type;
                m_out_max = m_type.getMaxValue();
                m_out_min = m_type.getMinValue();
                if (scaleFrom != null) {
                        m_mode = MODE_SCALE;
                        m_in_min = scaleFrom.getMinValue();
                        m_factor = (scaleFrom.getMaxValue() - m_in_min)
                                        / (type.getMaxValue() - m_out_min);
                } else {
                        m_mode = MODE_OVERFLOW;
                        m_in_min = 0;
                        m_factor = 0;
                }
        }

        /**
         * Converts to the new type and scales the values according to the given
         * factor.
         * 
         * @param type
         * @param min
         * @param factor
         */
        public Convert(final O type, double factor, double inMin, double outMin) {
                m_type = type;
                m_out_max = 0;
                m_out_min = outMin;
                m_in_min = inMin;
                m_factor = factor;
                m_mode = MODE_SCALE;

        }

        public Convert(final NativeTypes type, final I scaleFrom) {
                this((O) type.getTypeInstance(), scaleFrom);
        }

        @Override
        public final O createEmptyOutput(final I op) {
                return m_type.createVariable();
        }

        @Override
        public final O compute(final I op, final O r) {
                switch (m_mode) {
                case MODE_CLIP:
                        double v = op.getRealDouble();
                        if (v > m_out_max) {
                                r.setReal(m_out_max);
                        } else if (v < m_out_min) {
                                r.setReal(m_out_min);
                        } else {
                                r.setReal(v);
                        }
                        break;
                case MODE_SCALE:
                        r.setReal((op.getRealDouble() - m_in_min) / m_factor
                                        + m_out_min);
                        break;
                case MODE_OVERFLOW:
                default:
                        r.setReal(op.getRealDouble());
                        break;
                }

                return r;
        }

        @Override
        public UnaryOutputOperation<I, O> copy() {
                return new Convert<I, O>(m_type, m_factor, m_in_min, m_out_min);
        }

        @Override
        public O compute(I in) {
                return compute(in, createEmptyOutput(in));
        }
}
