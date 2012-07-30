package org.knime.knip.core.ops.morph;

import net.imglib2.img.Img;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.logic.BitType;

import org.knime.knip.core.types.ConnectedType;
import org.knime.knip.core.util.BinaryOps;

public final class Erode implements UnaryOperation<Img<BitType>, Img<BitType>> {

        private final int m_neighbourhoodCount;

        private final ConnectedType m_type;

        private final BinaryOps<Img<BitType>> m_binOps;

        /**
         * @param type
         * @param neighbourhoodCount
         * @param iterations
         *                number of iterations, at least 1
         */
        public Erode(ConnectedType type, final int neighbourhoodCount) {
                m_neighbourhoodCount = neighbourhoodCount;
                m_type = type;
                m_binOps = new BinaryOps<Img<BitType>>();
        }

        @Override
        public Img<BitType> compute(Img<BitType> op, Img<BitType> r) {
                m_binOps.erode(m_type, r, op, m_neighbourhoodCount);
                return r;

        }

        @Override
        public UnaryOperation<Img<BitType>, Img<BitType>> copy() {
                return new Erode(m_type, m_neighbourhoodCount);
        }
}
