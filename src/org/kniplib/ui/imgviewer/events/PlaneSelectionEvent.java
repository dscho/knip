package org.kniplib.ui.imgviewer.events;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.kniplib.ui.event.KNIPEvent;


/**
 * @author dietzc, hornm, schoenenbergerf (University of Konstanz)
 *
 */
public class PlaneSelectionEvent implements Externalizable, KNIPEvent {

        private long[] m_pos;

        private final StringBuffer m_buffer;

        private int[] m_indices;

        public PlaneSelectionEvent() {
                m_buffer = new StringBuffer();
        }

        public PlaneSelectionEvent(int dimIndex1, int dimIndex2, long[] pos) {
                m_indices = new int[] { dimIndex1, dimIndex2 };
                m_pos = pos;
                m_buffer = new StringBuffer();

        }

        public int numDimensions() {
                return m_pos.length;
        }

        public int getPlaneDimIndex1() {
                return m_indices[0];
        }

        public int getPlaneDimIndex2() {
                return m_indices[1];
        }

        public long[] getPlanePos() {
                return m_pos.clone();
        }

        /**
         * @param pos1
         * @param pos2
         * @return the plane position, whereas the dimensions of the planes are
         *         replaced by <code>pos1</code> and <code>pos2</code>
         */
        public long[] getPlanePos(long pos1, long pos2) {
                long[] res = m_pos.clone();
                res[m_indices[0]] = pos1;
                res[m_indices[1]] = pos2;
                return res;
        }

        public long getPlanePosAt(int dim) {
                return m_pos[dim];
        }

        public int[] getDimIndices() {
                return m_indices;
        }

        public FinalInterval getInterval(Interval i) {
                long[] dims = new long[i.numDimensions()];
                i.dimensions(dims);

                long[] min = m_pos.clone();
                long[] max = m_pos.clone();

                min[m_indices[0]] = 0;
                min[m_indices[1]] = 0;

                max[m_indices[0]] = dims[m_indices[0]] - 1;
                max[m_indices[1]] = dims[m_indices[1]] - 1;

                return new FinalInterval(min, max);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
                // TODO: Efficency
                m_buffer.setLength(0);
                m_buffer.append(getPlaneDimIndex1());
                m_buffer.append(getPlaneDimIndex2());
                for (int i = 0; i < numDimensions(); i++) {
                        m_buffer.append((m_pos[i]) ^ ((m_pos[i]) >>> 32));
                }
                return m_buffer.toString().hashCode();
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                        ClassNotFoundException {
                int num = in.readInt();
                m_indices = new int[num];
                for (int i = 0; i < num; i++) {
                        m_indices[i] = in.readInt();
                }

                num = in.readInt();
                m_pos = new long[num];

                for (int i = 0; i < m_pos.length; i++) {
                        m_pos[i] = in.readLong();
                }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                out.writeInt(m_indices.length);
                for (int i = 0; i < m_indices.length; i++) {
                        out.writeInt(m_indices[i]);
                }

                out.writeInt(m_pos.length);

                for (int i = 0; i < m_pos.length; i++) {
                        out.writeLong(m_pos[i]);
                }
        }
}
