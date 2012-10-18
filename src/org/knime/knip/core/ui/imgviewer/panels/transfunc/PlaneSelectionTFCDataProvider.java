package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.transfunc.TransferFunctionBundle;

public class PlaneSelectionTFCDataProvider<T extends RealType<T>, I extends RandomAccessibleInterval<T>> extends AbstractTFCDataProvider<T, I> {

        /**
         * Class to wrap the postional array into, because using arrays
         * as keys for hashmaps sadly does not work.
         */
        private class Position {
                private final long[] m_pos;

                public Position(final long[] pos, final int[] indices) {
                        m_pos = pos;
                        m_pos[indices[0]] = -1000;
                        m_pos[indices[1]] = -1000;
                }

                @Override
                public boolean equals(final Object o) {
                        // simply ignore that o might not be of type
                        // Position, this is an internal class and
                        // nothing bad will happen
                        return hashCode() == o.hashCode();
                }

                @Override
                public int hashCode() {
                        int hash = 0;

                        for (long i : m_pos) {
                                hash = hash * 31 + (int) i;
                        }

                        return hash;
                }
        }

        private final Map<Position, TransferFunctionControlPanel.Memento> m_map = new HashMap<Position, TransferFunctionControlPanel.Memento>();


        public PlaneSelectionTFCDataProvider() {
                this(new TransferFunctionControlPanel());
        }


        public PlaneSelectionTFCDataProvider(final TransferFunctionControlPanel panel) {
                super(panel);
        }

        @EventListener
        public void onPlaneSelectionChg(final PlaneSelectionEvent event) {

                Position key = new Position(event.getPlanePos(), event.getDimIndices());
                TransferFunctionControlPanel.Memento memento = m_map.get(key);

                // if no such memento exists, create a new one
                if (memento == null) {
                        int[] histogram = super.calcNewHistogram(event.getInterval(super.getSrcInterval()));

                        List<TransferFunctionBundle> bundles = new ArrayList<TransferFunctionBundle>();
                        bundles.add(TransferFunctionBundle.newRGBBundle());
                        bundles.add(TransferFunctionBundle.newGBundle());

                        memento = m_tfc.createMemento(bundles, histogram);

                        m_map.put(key, memento);
                }

                m_tfc.setState(memento);
                m_tfc.repaint();
        }
}
