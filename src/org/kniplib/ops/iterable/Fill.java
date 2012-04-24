package org.kniplib.ops.iterable;

import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.Type;

public class Fill<T extends Type<T>> implements
                UnaryOperation<Iterable<T>, Iterable<T>> {

        private final T m_value;

        public Fill(final T value) {
                m_value = value;
        }

        /**
         * @param in
         *                Not touched at all. Can be <code>null</code>.
         * @param out
         *                Is filled with the value.
         */
        @Override
        public Iterable<T> compute(final Iterable<T> in, final Iterable<T> out) {
                for (final T t : out) {
                        t.set(m_value);
                }
                return out;
        }

        @Override
        public UnaryOperation<Iterable<T>, Iterable<T>> copy() {
                return new Fill<T>(m_value.copy());
        }
}
