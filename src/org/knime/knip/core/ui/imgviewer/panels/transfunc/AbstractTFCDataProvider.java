package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.iterableinterval.unary.OpsHistogram;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.transfunc.TransferFunctionBundle;

/**
 * Class that wraps the panel and connects it to the knip event service.
 */
public abstract class AbstractTFCDataProvider<T extends RealType<T>, I extends RandomAccessibleInterval<T>, KEY>
                implements TransferFunctionControlDataProvider<T, I> {

        private static final int NUM_BINS = 250;

        protected EventService m_eventService;

        protected TransferFunctionControlPanel m_tfc;

        private RandomAccessibleInterval<T> m_src;

        private int m_numBins = NUM_BINS;

        private final Map<KEY, TransferFunctionControlPanel.Memento> m_mementos = new HashMap<KEY, TransferFunctionControlPanel.Memento>();

        private TransferFunctionControlPanel.Memento m_currentMemento;
        private KEY m_currentKey;

        /**
         * Set up a new instance and wrap the passed panel.
         *
         * @param panel
         *                the panel that should be wrapped
         */
        public AbstractTFCDataProvider(final TransferFunctionControlPanel panel) {
                if (panel == null)
                        throw new NullPointerException();

                m_currentMemento = createStartingMemento(panel);
                setPanel(panel);
        }

        /**
         * Use this to calculate a new histogram for a given interval on the
         * current source data.
         */
        protected final int[] calcNewHistogram(final Interval interval) {
                assert m_src != null;
                assert interval != null;

                // find min value
                Cursor<T> cur = SubsetViews.iterableSubsetView(m_src, interval)
                                .cursor();
                cur.fwd();
                T sample = cur.get().createVariable();
                cur.reset();

                // create the histogram
                OpsHistogram hist = new OpsHistogram(m_numBins, sample);
                while (cur.hasNext()) {
                        cur.fwd();
                        hist.incByValue(cur.get().getRealDouble());
                }

                return hist.hist();
        }


        /**
         * This method is called everytime the src changes and must return the
         * key that corresponds to the current settings.<br>
         * 
         * @return the key to store the first memento.
         */
        protected abstract KEY updateKey(final Interval src);

        protected abstract Interval currentHistogramInterval();

        /**
         * Use this if the concrete base class has intercepted an event that
         * needs to set a new Memento.<br>
         *
         * @param key
         *                the key to look up or to save the new memento under
         * @param interval
         *                the interval to use for calculating the histogram if
         *                the key is not yet saved in the map of mementos
         */
        protected final void setMementoToTFC(final KEY key) {

                m_currentKey = key;
                m_currentMemento = m_mementos.get(m_currentKey);

                if (m_currentMemento == null) {
                        int[] histogram = calcNewHistogram(currentHistogramInterval());

                        m_currentMemento = m_tfc.createMemento(histogram);

                        m_mementos.put(m_currentKey, m_currentMemento);
                }

                m_tfc.setState(m_currentMemento);
        }

        @EventListener
        public final void onImgUpdated(
                        final IntervalWithMetadataChgEvent<I> event) {
                m_src = event.getInterval();

                m_currentKey = updateKey(m_src);
                m_currentMemento = m_mementos.get(m_currentKey);

                if (m_currentMemento == null) {
                        m_currentMemento = createStartingMementoWithHistogram();
                        m_mementos.put(m_currentKey, m_currentMemento);
                }

                m_tfc.setState(m_currentMemento);
        }

        @Override
        public final void setNumberBins(final int bins) {
                m_numBins = bins < 1 ? 1 : bins;
        }

        @Override
        public final void setEventService(final EventService service) {
                if (service == null) {
                        m_eventService = new EventService();
                } else {
                        m_eventService = service;
                }

                m_tfc.setEventService(m_eventService);
                m_eventService.subscribe(this);
        }

        @Override
        public final void setPanel(final TransferFunctionControlPanel panel) {
                if (panel == null)
                        throw new NullPointerException();

                m_tfc = panel;
                m_tfc.setState(m_currentMemento);
        }

        private TransferFunctionControlPanel.Memento createStartingMementoWithHistogram() {
                assert m_src != null;
                assert m_tfc != null;

                // create the arrays for the histogram interval
                long[] min = new long[m_src.numDimensions()];
                long[] max = new long[m_src.numDimensions()];

                long[] test = new long[m_src.numDimensions()];
                m_src.dimensions(test);

                Arrays.fill(min, 0);
                Arrays.fill(max, 0);

                max[0] = m_src.dimension(0) - 1;
                max[1] = m_src.dimension(1) - 1;

                int[] histogram = calcNewHistogram(new FinalInterval(min, max));

                return m_tfc.createMemento(histogram);
        }

        private List<TransferFunctionBundle> createStartingBundle() {
                List<TransferFunctionBundle> bundles = new ArrayList<TransferFunctionBundle>();
                bundles.add(TransferFunctionBundle.newRGBBundle());
                bundles.add(TransferFunctionBundle.newGBundle());

                return bundles;
        }

        private TransferFunctionControlPanel.Memento createStartingMemento(
                        final TransferFunctionControlPanel panel) {
                assert panel != null;

                return panel.createMemento(createStartingBundle(), null);
        }
}
