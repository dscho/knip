package org.kniplib.algorithm.types;

public enum NeighborhoodType {

        EIGHT(-1, 3), SIXTEEN(-2, 5), THIRTYTWO(-3, 7);

        private int m_offset;

        private int m_extend;

        private NeighborhoodType(int offset, int extend) {
                m_offset = offset;
                m_extend = extend;
        }

        public final int getOffset() {
                return m_offset;
        }

        public final int getExtend() {
                return m_extend;
        }

}
