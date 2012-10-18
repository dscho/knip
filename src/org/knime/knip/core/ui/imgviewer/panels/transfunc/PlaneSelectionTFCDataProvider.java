package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import java.util.Arrays;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;

public class PlaneSelectionTFCDataProvider<T extends RealType<T>, I extends RandomAccessibleInterval<T>>
                extends AbstractTFCDataProvider<T, I, Integer> {


        private long[] m_pos = new long[] {0, 0};
        private int[] m_indices = new int[] {0, 1};
        private Interval m_src = new FinalInterval(new long[2], new long[2]);
        private Interval m_histogramInterval = new FinalInterval(new long[2], new long[2]);

        public PlaneSelectionTFCDataProvider() {
                this(new TransferFunctionControlPanel());
        }

        public PlaneSelectionTFCDataProvider(
                        final TransferFunctionControlPanel panel) {
                super(panel);
        }

        @EventListener
        public void onPlaneSelectionChg(final PlaneSelectionEvent event) {

                // update the values
                m_indices[0] = event.getPlaneDimIndex1();
                m_indices[1] = event.getPlaneDimIndex2();

                m_pos = event.getPlanePos();
                Integer key = hash(m_pos, event.getDimIndices(), m_src);
                m_histogramInterval = event.getInterval(m_src);

                super.setMementoToTFC(key);
        }

        @Override
        protected final Interval currentHistogramInterval() {
                return m_histogramInterval;
        }

        @Override
        protected final Integer updateKey(final Interval src) {
                m_src = src;

                if (m_src.numDimensions() != m_pos.length) {
                        m_pos = new long[src.numDimensions()];
                        Arrays.fill(m_pos, 0);
                }

                return hash(m_pos, m_indices, src);
        }


        private int hash(final long[] pos, final int[] indices,
                        final Interval src) {

                // set the two indices to values that can not occur in
                // normal settings
                pos[indices[0]] = -1000;
                pos[indices[1]] = -1000;

                // create the hash code
                int hash = 31;

                for (long i : pos) {
                        hash = hash * 31 + (int) i;
                }

                hash += 31 * src.hashCode();

                return hash;
        }
}
