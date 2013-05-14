package org.knime.knip.core.ui.imgviewer.events;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;

import org.knime.knip.core.ui.event.KNIPEvent;

/**
 * @author dietzc, hornm, schoenenbergerf (University of Konstanz)
 * 
 */
public class PlaneSelectionEvent implements Externalizable, KNIPEvent {

    private long[] m_pos;

    private final StringBuffer m_buffer;

    private int[] m_indices;

    /**
     *
     */
    public PlaneSelectionEvent() {
        m_buffer = new StringBuffer();
    }

    /**
     * @param dimIndex1
     * @param dimIndex2
     * @param pos
     */
    public PlaneSelectionEvent(final int dimIndex1, final int dimIndex2, final long[] pos) {
        m_indices = new int[]{dimIndex1, dimIndex2};
        m_buffer = new StringBuffer();
        m_pos = pos.clone();
    }

    @Override
    public ExecutionPriority getExecutionOrder() {
        return ExecutionPriority.NORMAL;
    }

    /**
     * implements object equality {@inheritDoc}
     */
    @Override
    public <E extends KNIPEvent> boolean isRedundant(final E thatEvent) {
        return this.equals(thatEvent);
    }

    /**
     * @return
     */
    public int numDimensions() {
        return m_pos.length;
    }

    /**
     * @return
     */
    public int getPlaneDimIndex1() {
        return m_indices[0];
    }

    /**
     * @return
     */
    public int getPlaneDimIndex2() {
        return m_indices[1];
    }

    /**
     * @return
     */
    public long[] getPlanePos() {
        return m_pos.clone();
    }

    /**
     * @param pos1
     * @param pos2
     * @return the plane position, whereas the dimensions of the planes are replaced by <code>pos1</code> and
     *         <code>pos2</code>
     */
    public long[] getPlanePos(final long pos1, final long pos2) {
        final long[] res = m_pos.clone();
        res[m_indices[0]] = pos1;
        res[m_indices[1]] = pos2;
        return res;
    }

    /**
     * @param dim
     * @return
     */
    public long getPlanePosAt(final int dim) {
        return m_pos[dim];
    }

    /**
     * @return
     */
    public int[] getDimIndices() {
        return m_indices;
    }

    /**
     * @param i
     * @return
     */
    public FinalInterval getInterval(final Interval i) {
        final long[] dims = new long[i.numDimensions()];
        i.dimensions(dims);

        final long[] min = m_pos.clone();
        final long[] max = m_pos.clone();

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
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
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
    public void writeExternal(final ObjectOutput out) throws IOException {
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
