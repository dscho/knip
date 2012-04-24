package org.kniplib.data.img;

import net.imglib2.Cursor;
import net.imglib2.Sampler;
import net.imglib2.type.Type;

public class TranslatedCursor<T extends Type<T>> implements Cursor<T> {

        private Cursor<T> m_cursor;

        private long[] m_translation;

        private int[] m_selectedDims;

        public TranslatedCursor(Cursor<T> c, long[] translation,
                        int[] selectedDims) {

                if (selectedDims == null) {
                        selectedDims = new int[translation.length];
                        for (int d = 0; d < selectedDims.length; d++)
                                selectedDims[d] = d;
                }

                m_selectedDims = selectedDims;
                m_cursor = c;
                m_translation = translation;
        }

        public TranslatedCursor(Cursor<T> c, long[] translation) {
                this(c, translation, null);
        }

        @Override
        public void localize(int[] position) {
                for (int p = 0; p < position.length; p++) {
                        position[p] = (int) (m_cursor
                                        .getIntPosition(m_selectedDims[p]) + m_translation[m_selectedDims[p]]);
                }
        }

        @Override
        public void localize(long[] position) {
                for (int p = 0; p < position.length; p++) {
                        position[p] = m_cursor
                                        .getLongPosition(m_selectedDims[p])
                                        + m_translation[m_selectedDims[p]];
                }
        }

        @Override
        public int getIntPosition(int d) {
                return (int) (m_cursor.getLongPosition(m_selectedDims[d]) + m_translation[m_selectedDims[d]]);
        }

        @Override
        public long getLongPosition(int d) {
                return m_cursor.getLongPosition(m_selectedDims[d])
                                + m_translation[m_selectedDims[d]];
        }

        @Override
        public void localize(float[] position) {

                for (int p = 0; p < position.length; p++) {
                        position[p] = m_cursor
                                        .getFloatPosition(m_selectedDims[p])
                                        + m_translation[m_selectedDims[p]];
                }
        }

        @Override
        public void localize(double[] position) {

                for (int p = 0; p < position.length; p++) {
                        position[p] = m_cursor
                                        .getDoublePosition(m_selectedDims[p])
                                        + m_translation[m_selectedDims[p]];
                }
        }

        @Override
        public float getFloatPosition(int d) {
                return (m_cursor.getFloatPosition(m_selectedDims[d]) + m_translation[m_selectedDims[d]]);
        }

        @Override
        public double getDoublePosition(int d) {
                return (m_cursor.getDoublePosition(m_selectedDims[d]) + m_translation[m_selectedDims[d]]);
        }

        @Override
        public int numDimensions() {
                return m_selectedDims.length;
        }

        @Override
        public T get() {
                return m_cursor.get();
        }

        @Override
        public Sampler<T> copy() {
                return m_cursor.copy();
        }

        @Override
        public void jumpFwd(long steps) {
                m_cursor.jumpFwd(steps);
        }

        @Override
        public void fwd() {
                m_cursor.fwd();
        }

        @Override
        public void reset() {
                m_cursor.reset();
        }

        @Override
        public boolean hasNext() {
                return m_cursor.hasNext();
        }

        @Override
        public T next() {
                return m_cursor.next();
        }

        @Override
        public void remove() {
                m_cursor.remove();
        }

        @Override
        public Cursor<T> copyCursor() {
                return new TranslatedCursor<T>(m_cursor.copyCursor(),
                                m_translation, m_selectedDims);
        }

}
