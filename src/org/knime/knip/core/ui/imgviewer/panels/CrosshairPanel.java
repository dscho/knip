package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.Random;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;

import org.knime.knip.core.ui.event.EventService;

import org.knime.knip.core.ui.imgviewer.events.AddCrosshairEvent;
import org.knime.knip.core.ui.imgviewer.events.CrosshairSettingsChgEvent;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;

/**
 * @author muethingc
 */
public class CrosshairPanel extends ViewerComponent {

        private EventService m_eventService;

        private final JSlider m_scrollThickness = new JSlider(JSlider.HORIZONTAL, 1, 15, 2);
        private final JSlider m_scrollAlpha = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);

        private final JButton m_addButton = new JButton("Add crosshair");

        private final Random m_rand = new Random();

        public CrosshairPanel() {
                this("Crosshairs", false);
        }

        public CrosshairPanel(String title, boolean isBorderHidden) {
                super(title, isBorderHidden);

                JLabel sizeLabel = new JLabel("Size");
                JLabel alphaLabel = new JLabel("Alpha");

                GroupLayout layout = new GroupLayout(this);
                setLayout(layout);

                GroupLayout.ParallelGroup horizontal0 = layout.createParallelGroup()
                    .addComponent(m_addButton);
                GroupLayout.ParallelGroup horizontal1 = layout.createParallelGroup()
                    .addComponent(sizeLabel)
                    .addComponent(m_scrollThickness);
                GroupLayout.ParallelGroup horizontal2 = layout.createParallelGroup()
                    .addComponent(alphaLabel)
                    .addComponent(m_scrollAlpha);

                GroupLayout.SequentialGroup vertical = layout.createSequentialGroup()
                    .addGroup(horizontal0)
                    .addGroup(horizontal1)
                    .addGroup(horizontal2);

                GroupLayout.ParallelGroup vertical0 = layout.createParallelGroup()
                    .addComponent(sizeLabel)
                    .addComponent(alphaLabel);

                GroupLayout.ParallelGroup vertical1 = layout.createParallelGroup()
                    .addComponent(m_addButton)
                    .addComponent(m_scrollThickness)
                    .addComponent(m_scrollAlpha);

                GroupLayout.SequentialGroup horizontal = layout.createSequentialGroup()
                    .addGroup(vertical0)
                    .addGroup(vertical1);

                layout.setHorizontalGroup(horizontal);
                layout.setVerticalGroup(vertical);

                m_addButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                Color c = new Color(m_rand.nextFloat(), m_rand.nextFloat(), m_rand.nextFloat());
                                c = c.brighter();
                                m_eventService.publish(new AddCrosshairEvent(c, c));
                        }
                });

                m_scrollThickness.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                                fireSettingsChgEvent();
                        }
                });

                m_scrollAlpha.addChangeListener(new ChangeListener() {

                        @Override
                        public void stateChanged(ChangeEvent e) {
                                fireSettingsChgEvent();
                        }
                });

        }

        private void fireSettingsChgEvent() {
                float alpha = (float) m_scrollAlpha.getValue() / (float) m_scrollAlpha.getMaximum();
                m_eventService.publish(new CrosshairSettingsChgEvent(m_scrollThickness.getValue(), alpha));
        }

        @Override
        public void setEventService(EventService eventService) {
                if (eventService == null) {
                    m_eventService = new EventService();
                } else {
                    m_eventService = eventService;
                }

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
