package org.kniplib.ops.img;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.logic.BitType;

import org.kniplib.tools.BinaryOps;
import org.kniplib.types.ConnectedType;

public final class Erode<K extends RandomAccessibleInterval<BitType> & IterableInterval<BitType>>
                implements UnaryOperation<K, K> {

        private final int m_neighbourhoodCount;

        private final ConnectedType m_type;

        public Erode(ConnectedType type, final int neighbourhoodCount) {
                m_neighbourhoodCount = neighbourhoodCount;
                m_type = type;
        }

        @Override
        public K compute(K op, K r) {
                return new BinaryOps<K>().erode(m_type, r, op,
                                m_neighbourhoodCount);
        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new Erode<K>(m_type, m_neighbourhoodCount);
        }
}
