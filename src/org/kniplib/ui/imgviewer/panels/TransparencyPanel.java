package org.kniplib.ui.imgviewer.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.kniplib.ui.event.EventService;
import org.kniplib.ui.imgviewer.ViewerComponent;
import org.kniplib.ui.imgviewer.events.TransparencyPanelValueChgEvent;

public class TransparencyPanel extends ViewerComponent {

        /**
	 *
	 */
        private static final long serialVersionUID = 1L;

        private EventService m_eventService;

        private final JSlider m_slider;

        private final JLabel m_sliderValue;

        public TransparencyPanel() {
                super("Adjust transparency", false);
                setMaximumSize(new Dimension(250, getMaximumSize().height));

                m_sliderValue = new JLabel("128");
                m_slider = new JSlider(0, 255, 128);
                m_slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                                m_eventService.publish(new TransparencyPanelValueChgEvent(
                                                m_slider.getValue()));
                                m_sliderValue.setText("" + m_slider.getValue());
                        }
                });

                add(m_slider);
                add(m_sliderValue);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPosition() {
                return BorderLayout.SOUTH;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEventService(EventService eventService) {
                m_eventService = eventService;

        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                out.writeInt(m_slider.getValue());

        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                m_slider.setValue(in.readInt());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
                // Nothing to do here
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }
}
