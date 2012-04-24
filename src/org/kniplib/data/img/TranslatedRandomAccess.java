package org.kniplib.data.img;

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.Sampler;
import net.imglib2.type.Type;

public class TranslatedRandomAccess<T extends Type<T>> implements
                RandomAccess<T> {

        private final RandomAccess<T> m_randomAccess;

        private final long[] m_translation;

        private int[] m_selectedDims;

        private long[] m_startPos;

        public TranslatedRandomAccess(RandomAccess<T> randomAccess,
                        long[] translation, int[] selectedDims, long[] startPos) {

                if (selectedDims == null) {
                        selectedDims = new int[translation.length];
                        for (int d = 0; d < selectedDims.length; d++)
                                selectedDims[d] = d;
                }

                m_selectedDims = selectedDims;
                m_randomAccess = randomAccess;
                m_translation = translation;
                m_startPos = startPos;
                randomAccess.setPosition(startPos);
        }

        public TranslatedRandomAccess(RandomAccess<T> randomAccess,
                        long[] translation, long[] startPos) {
                this(randomAccess, translation, null, startPos);
        }

        @Override
        public void localize(int[] position) {
                for (int p = 0; p < position.length; p++) {
                        position[p] = (int) (m_randomAccess
                                        .getIntPosition(m_selectedDims[p]) + m_translation[m_selectedDims[p]]);
                }
        }

        @Override
        public void localize(long[] position) {
                for (int p = 0; p < position.length; p++) {
                        position[p] = m_randomAccess
                                        .getLongPosition(m_selectedDims[p])
                                        + m_translation[m_selectedDims[p]];
                }
        }

        @Override
        public int getIntPosition(int d) {
                return (int) (m_randomAccess.getLongPosition(m_selectedDims[d]) + m_translation[m_selectedDims[d]]);
        }

        @Override
        public long getLongPosition(int d) {
                return m_randomAccess.getLongPosition(m_selectedDims[d])
                                + m_translation[m_selectedDims[d]];
        }

        @Override
        public void localize(float[] position) {

                for (int p = 0; p < position.length; p++) {
                        position[p] = m_randomAccess
                                        .getFloatPosition(m_selectedDims[p])
                                        + m_translation[m_selectedDims[p]];
                }
        }

        @Override
        public void localize(double[] position) {

                for (int p = 0; p < position.length; p++) {
                        position[p] = m_randomAccess
                                        .getDoublePosition(m_selectedDims[p])
                                        + m_translation[m_selectedDims[p]];
                }
        }

        @Override
        public float getFloatPosition(int d) {
                return (m_randomAccess.getFloatPosition(m_selectedDims[d]) + m_translation[m_selectedDims[d]]);
        }

        @Override
        public double getDoublePosition(int d) {
                return (m_randomAccess.getDoublePosition(m_selectedDims[d]) + m_translation[m_selectedDims[d]]);
        }

        @Override
        public int numDimensions() {
                return m_selectedDims.length;
        }

        @Override
        public void fwd(int d) {
                m_randomAccess.fwd(m_selectedDims[d]);

        }

        @Override
        public void bck(int d) {
                m_randomAccess.bck(m_selectedDims[d]);

        }

        @Override
        public void move(int distance, int d) {
                m_randomAccess.move(distance, m_selectedDims[d]);
        }

        @Override
        public void move(long distance, int d) {
                m_randomAccess.move(distance, m_selectedDims[d]);
        }

        @Override
        public void move(Localizable localizable) {
                for (int d = 0; d < localizable.numDimensions(); d++) {
                        m_randomAccess.move(localizable.getLongPosition(d),
                                        m_selectedDims[d]);
                }
        }

        @Override
        public void move(int[] distance) {
                for (int d = 0; d < distance.length; d++) {
                        m_randomAccess.move(distance[d], m_selectedDims[d]);
                }
        }

        @Override
        public void move(long[] distance) {
                for (int d = 0; d < distance.length; d++) {
                        m_randomAccess.move(distance[d], m_selectedDims[d]);
                }
        }

        @Override
        public void setPosition(Localizable localizable) {
                for (int d = 0; d < localizable.numDimensions(); d++) {
                        m_randomAccess.setPosition(
                                        localizable.getIntPosition(d)
                                                        - m_translation[m_selectedDims[d]],
                                        m_selectedDims[d]);
                }
        }

        @Override
        public void setPosition(int[] position) {
                for (int d = 0; d < position.length; d++) {
                        m_randomAccess.setPosition(position[d]
                                        - m_translation[m_selectedDims[d]],
                                        m_selectedDims[d]);
                }
        }

        @Override
        public void setPosition(long[] position) {

                for (int d = 0; d < position.length; d++) {
                        m_randomAccess.setPosition(position[d]
                                        - m_translation[m_selectedDims[d]],
                                        m_selectedDims[d]);
                }
        }

        @Override
        public void setPosition(int position, int d) {
                m_randomAccess.setPosition(position
                                - m_translation[m_selectedDims[d]],
                                m_selectedDims[d]);
        }

        @Override
        public void setPosition(long position, int d) {
                m_randomAccess.setPosition(position
                                - m_translation[m_selectedDims[d]],
                                m_selectedDims[d]);
        }

        @Override
        public T get() {
                return m_randomAccess.get();
        }

        @Override
        public Sampler<T> copy() {
                return m_randomAccess.copy();
        }

        @Override
        public RandomAccess<T> copyRandomAccess() {
                return new TranslatedRandomAccess<T>(
                                m_randomAccess.copyRandomAccess(),
                                m_translation, m_selectedDims, m_startPos);
        }
}
