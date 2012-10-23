package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.operation.iterableinterval.unary.OpsHistogram;
import net.imglib2.ops.operation.subset.views.SubsetViews;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.Real2ColorByLookupTableRenderer;
import org.knime.knip.core.awt.lookup.LookupTable;
import org.knime.knip.core.awt.lookup.RealLookupTable;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.RendererSelectionChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewClosedEvent;

/**
 * Class that wraps the panel and connects it to the knip event service.
 */
public abstract class AbstractTFCDataProvider<T extends RealType<T>, I extends RandomAccessibleInterval<T>, KEY>
                extends ViewerComponent implements
                TransferFunctionControlDataProvider<T, I> {

        private class ActionAdapter implements ActionListener {
                @Override
                public void actionPerformed(final ActionEvent e) {
                        switch (e.getID()) {
                        case TransferFunctionControlPanel.ID_APPLY:
                                fireTransferFunctionChgEvent();
                                break;
                        case TransferFunctionControlPanel.ID_NORMALIZE:
                                fireTransferFunctionChgEvent();
                                break;
                        case TransferFunctionControlPanel.ID_ONLYONE:
                                m_onlyOne = m_tfc.isOnlyOneFunc();
                                break;
                        default:
                                throw new RuntimeException(
                                                "No action implemented for id "
                                                                + e.getID());
                        }
                }
        }

        private static final int NUM_BINS = 250;

        protected EventService m_eventService;

        protected TransferFunctionControlPanel m_tfc;

        private RandomAccessibleInterval<T> m_src;

        private int m_numBins = NUM_BINS;

        private final Map<KEY, TransferFunctionControlPanel.Memento> m_mementos = new HashMap<KEY, TransferFunctionControlPanel.Memento>();
        private final Map<KEY, HistogramWithNormalization> m_histData = new HashMap<KEY, HistogramWithNormalization>();

        private boolean m_onlyOne = true;

        private TransferFunctionControlPanel.Memento m_currentMemento;
        private HistogramWithNormalization m_currentHistogram = new HistogramWithNormalization(
                        new int[] { 0, 1 }, 0, 1);

        /**
         * Set up a new instance and wrap the passed panel.
         *
         * @param panel
         *                the panel that should be wrapped
         */
        AbstractTFCDataProvider(final TransferFunctionControlPanel panel) {
                super("Transfer Function", false);

                if (panel == null)
                        throw new NullPointerException();

                m_currentMemento = createStartingMemento(panel);

                m_tfc = panel;
                m_tfc.setState(m_currentMemento);
                m_tfc.setOnlyOneFunc(m_onlyOne);
                m_tfc.addActionListener(new ActionAdapter());
                m_tfc.setOnlyOneFunc(m_onlyOne);

                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                add(m_tfc);
        }

        /**
         * Use this to calculate a new histogram for a given interval on the
         * current source data.
         */
        private HistogramWithNormalization calcNewHistogram(final Interval interval) {
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

                        double val = cur.get().getRealDouble();
                        hist.incByValue(val);
                }

                return new HistogramWithNormalization(hist.hist(), sample.getMinValue(), sample.getMaxValue());
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

                TransferFunctionControlPanel.Memento newMemento;
                HistogramWithNormalization hist = getHistogramData(key);

                if (m_onlyOne) {
                        newMemento = m_tfc.createMemento(m_currentMemento,
                                        hist);
                } else {
                        newMemento = m_mementos.get(key);

                        if (newMemento == null) {
                                newMemento = m_tfc.createMemento(hist);

                                m_mementos.put(key, newMemento);
                        }
                }

                m_currentMemento = newMemento;
                m_tfc.setState(m_currentMemento);
                m_currentHistogram = hist;

                fireTransferFunctionChgEvent();
        }

        private HistogramWithNormalization getHistogramData(final KEY key) {
                HistogramWithNormalization hist = m_histData.get(key);

                if (hist == null) {
                        hist = calcNewHistogram(currentHistogramInterval());
                        m_histData.put(key, hist);
                }

                return hist;
        }

        @EventListener
        public final void onImgUpdated(
                        final IntervalWithMetadataChgEvent<I> event) {

                /*
                 * because of the way the AWTImageProvider reacts to new images
                 * (simply choosing a new Renderer from a list and keeping the
                 * old one if the current renderer is also on the list) and the
                 * fact that I cannot add my renderer to this list (the lookup
                 * table renderer is only suitable if you have a source for a
                 * lookup table, clearly not the case if only the simple image
                 * enhance is used), I need to issue a renderer changed request
                 * each time the image changes.
                 *
                 * Moreover this needs to be after the AWTImageProvider has
                 * processed this request, so we need to do this after the
                 * current AWTEvent has been finished processing.
                 */
                SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                                m_eventService.publish(new RendererSelectionChgEvent(
                                                new Real2ColorByLookupTableRenderer<T>()));
                                m_eventService.publish(new ImgRedrawEvent());
                        }
                });

                m_src = event.getInterval();
                setMementoToTFC(updateKey(m_src));
        }

        @EventListener
        public void onClose(final ViewClosedEvent event) {
                m_src = null;
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

                m_eventService.subscribe(this);
        }

        @Override
        public final TransferFunctionControlPanel getControl() {
                return m_tfc;
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

        private void fireTransferFunctionChgEvent() {
                Histogram hist = m_currentHistogram;

                if (m_tfc.isNormalize()) {
                        hist = m_currentHistogram.getNormalizedHistogram();
                }

                LookupTable<T, ARGBType> table = new RealLookupTable<T>(hist.getMinValue(),
                                hist.getMaxValue(), m_tfc.getCurrentBundle());
                m_eventService.publish(new LookupTableChgEvent<T, ARGBType>(
                                table));
                m_eventService.publish(new ImgRedrawEvent());
        }

        @Override
        public void setParent(Component parent) {
                // ignore
        }

        @Override
        public Position getPosition() {
                return Position.SOUTH;
        }

        @Override
        public void reset() {
                // ignore
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                // ignore
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                // ignore
        }

}
