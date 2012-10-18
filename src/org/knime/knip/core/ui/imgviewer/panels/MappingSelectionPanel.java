package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.ImageRenderer;
import org.knime.knip.core.awt.Real2ColorByLookupTableRenderer;
import org.knime.knip.core.awt.Real2GreyRenderer;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.ViewerComponents;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.RendererSelectionChgEvent;
import org.knime.knip.core.ui.imgviewer.events.SetCachingEvent;
import org.knime.knip.core.ui.imgviewer.panels.transfunc.TransferFunctionControlDataProvider;
import org.knime.knip.core.ui.imgviewer.panels.transfunc.TransferFunctionControlPanel;

public class MappingSelectionPanel<T extends RealType<T>, I extends RandomAccessibleInterval<T>> extends ViewerComponent {

        private final static int TRANSFER = 1;

        private EventService m_eventService;

        private final JTabbedPane m_tabPane = new JTabbedPane();

        private TransferFunctionControlDataProvider<T, I> m_provider;
        private final TransferFunctionControlPanel m_tfc = new TransferFunctionControlPanel();

        private final ViewerComponent m_rendererSelection = ViewerComponents.RENDERER_SELECTION.createInstance();
        private final ViewerComponent m_imageEnhance = ViewerComponents.IMAGE_ENHANCE.createInstance();

        private ImageRenderer<T, RandomAccessibleInterval<T>> m_lastRenderer = new Real2GreyRenderer();


        public MappingSelectionPanel() {
                // set up the provider as a dummy for less chances of bugs
                this(new TransferFunctionControlDataProvider<T, I>() {
                        @Override
                        public void setEventService(EventService service) { }

                        @Override
                        public void setNumberBins(int bins) {}

                        @Override
                        public void setPanel(final TransferFunctionControlPanel panel) { }
                });
        }

        public MappingSelectionPanel(final TransferFunctionControlDataProvider<T, I> provider) {
                super("Mapping", true);

                setProvider(provider);

                // add the two mapping options
                JPanel simpleMapping = new JPanel();
                simpleMapping.setLayout(new BoxLayout(simpleMapping, BoxLayout.X_AXIS));
                simpleMapping.add(m_imageEnhance);
                simpleMapping.add(m_rendererSelection);
                simpleMapping.add(Box.createHorizontalGlue());

                m_tabPane.addTab("Simple Renderer", simpleMapping);
                m_tabPane.addTab("Transfer Function", m_tfc);

                m_tabPane.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent event) {
                                ImageRenderer<T, RandomAccessibleInterval<T>> renderer;
                                boolean cache;

                                if (m_tabPane.getSelectedIndex() == TRANSFER) {
                                        renderer = new Real2ColorByLookupTableRenderer<T>();
                                        cache = false;
                                } else {
                                        renderer = m_lastRenderer;
                                        cache = true;
                                }

                                m_eventService.publish(new SetCachingEvent(cache));
                                m_eventService.publish(new RendererSelectionChgEvent(renderer));
                                m_eventService.publish(new ImgRedrawEvent());
                        }
                });

                this.add(m_tabPane);
        }

        /**
         * Set the data provider to use.<br>
         *
         * @param provider the new provider
         */
        public void setProvider(final TransferFunctionControlDataProvider<T, I> provider) {
                if (provider == null) throw new NullPointerException();

                m_provider = provider;
                m_provider.setPanel(m_tfc);
                m_provider.setEventService(m_eventService);
        }

        @SuppressWarnings("unchecked")
        @EventListener
        public void onRendererChg(final RendererSelectionChgEvent event) {
                if ( ! (event.getRenderer() instanceof Real2ColorByLookupTableRenderer) ) {
                        m_lastRenderer = event.getRenderer();
                }
        }

        @Override
        public void setEventService(EventService eventService) {
                if (eventService == null) {
                        m_eventService = new EventService();
                } else {
                        m_eventService = eventService;
                }

                m_provider.setEventService(m_eventService);
                m_tfc.setEventService(m_eventService);
                m_imageEnhance.setEventService(m_eventService);
                m_rendererSelection.setEventService(m_eventService);

                m_eventService.subscribe(this);
        }

        @Override
        public void setParent(Component parent) {
                // not used
        }

        @Override
        public Position getPosition() {
                return Position.SOUTH;
        }

        @Override
        public void reset() {
                // not used
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                // not used
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                // not used
        }

}
