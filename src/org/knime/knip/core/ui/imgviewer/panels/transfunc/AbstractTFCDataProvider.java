package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.iterableinterval.unary.OpsHistogram;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;


/**
 * Class that wraps the panel and connects it to the knip event service.
 */
public class AbstractTFCDataProvider<T extends RealType<T>, I extends RandomAccessibleInterval<T>> implements
                TransferFunctionControlDataProvider<T, I> {

        private static final int NUM_BINS = 250;

        protected EventService m_eventService;

        protected TransferFunctionControlPanel m_tfc;

        private RandomAccessibleInterval<T> m_src;

        private int m_numBins = NUM_BINS;


        /**
         * Set up a new instance and wrap the passed panel.
         *
         * @param panel the panel that should be wrapped
         */
        public AbstractTFCDataProvider(final TransferFunctionControlPanel panel) {
                setPanel(panel);
        }


        /**
         * Use this to calculate a new histogram for a given interval on
         * the current source data.
         */
        protected int[] calcNewHistogram(final Interval interval) {
                assert m_src != null;
                assert interval != null;

                // find min value
                Cursor<T> cur = SubsetViews.iterableSubsetView(m_src, interval).cursor();
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


        protected Interval getSrcInterval() {
                return m_src;
        }


        @EventListener
        public void onImgUpdated(final IntervalWithMetadataChgEvent<I> event) {
                m_src = event.getInterval();
        }

        @Override
        public void setNumberBins(final int bins) {
                m_numBins = bins < 1 ? 1 : bins;
        }

        @Override
        public void setEventService(final EventService service) {
                if (service == null) {
                        m_eventService = new EventService();
                } else {
                        m_eventService = service;
                }

                m_tfc.setEventService(m_eventService);
                m_eventService.subscribe(this);
        }

        @Override
        public void setPanel(final TransferFunctionControlPanel panel) {
                if (panel == null) throw new NullPointerException();

                m_tfc = panel;
        }
}
