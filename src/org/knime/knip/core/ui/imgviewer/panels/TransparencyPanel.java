package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.TransparencyPanelValueChgEvent;

public class TransparencyPanel extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private EventService m_eventService;

        private final JSlider m_slider;
        private final JLabel m_sliderValue;

        public TransparencyPanel() {
                super("Transparency", false);
                setMinimumSize(new Dimension(180, 50));
                setPreferredSize(new Dimension(180,
                                this.getPreferredSize().height));
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

                m_sliderValue = new JLabel("128");
                m_slider = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
                m_slider.setPreferredSize(new Dimension(120, 17));
                m_slider.setMaximumSize(new Dimension(120, 17));
                m_slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                                m_eventService.publish(new TransparencyPanelValueChgEvent(
                                                m_slider.getValue()));
                                m_eventService.publish(new ImgRedrawEvent());
                                m_sliderValue.setText("" + m_slider.getValue());
                        }
                });


                add(createComponentPanel());
        }

        private JPanel createComponentPanel() {
                JPanel ret = new JPanel();
                ret.setLayout(new GridBagLayout());

                GridBagConstraints gc = new GridBagConstraints();
                int x = 0;
                int y = 0;

                // all
                gc.fill = GridBagConstraints.HORIZONTAL;

                // first col
                gc.anchor = GridBagConstraints.LINE_START;
                gc.weightx = 1.0;

                gc.gridy = y;
                ret.add(m_slider, gc);

                x++;
                gc.gridx = x;
                gc.gridy = y;
                ret.add(m_sliderValue, gc);

                return ret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
                return Position.SOUTH;
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
