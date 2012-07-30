package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelColoringChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.TransparencyPanelValueChgEvent;

public class TransparencyColorSelectionPanel extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private EventService m_eventService;

        private final JSlider m_slider;
        private final JLabel m_sliderValue;
        private final JButton m_boundingBoxColor;
        private final JButton m_resetColor;
        // private final JLabel m_trans;

        // ColorChooser Dialog
        private final JButton m_colorOK;
        private final JButton m_colorCancel;
        private static JColorChooser m_cchoose;
        private final JFrame m_ColorChooser = new JFrame();

        private final JPanel m_buttonPane = new JPanel();
        private final JPanel m_colorPane = new JPanel();

        public TransparencyColorSelectionPanel() {
                super("Color Options", false);
                setMinimumSize(new Dimension(240, 140));
                setPreferredSize(new Dimension(240, 140));
                setMaximumSize(new Dimension(240, 140));
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

                // m_trans = new JLabel("Adjust Transparency");
                m_sliderValue = new JLabel("128");
                m_slider = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
                m_slider.setPreferredSize(new Dimension(180, 17));
                m_slider.setMaximumSize(new Dimension(180, 17));
                m_slider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                                m_eventService.publish(new TransparencyPanelValueChgEvent(
                                                m_slider.getValue()));
                                m_eventService.publish(new ImgRedrawEvent());
                                m_sliderValue.setText("" + m_slider.getValue());
                        }
                });

                // Buttons for changing BoundingBox color and reset color
                m_boundingBoxColor = new JButton(new ImageIcon(getClass()
                                .getResource("ColorIcon.png")));

                m_resetColor = new JButton(new ImageIcon(getClass()
                                .getResource("ColorIcon.png")));

                // ColorChooser Buttons
                m_colorOK = new JButton("OK");
                m_colorCancel = new JButton("Cancel");
                m_colorOK.setSize(5, 2);
                m_colorCancel.setSize(6, 2);

                // Settings for JColorChooser
                m_cchoose = new JColorChooser();
                m_cchoose.setPreviewPanel(new JPanel());
                m_cchoose.removeChooserPanel(m_cchoose.getChooserPanels()[0]);
                m_cchoose.removeChooserPanel(m_cchoose.getChooserPanels()[1]);

                m_colorPane.add(m_cchoose);
                m_buttonPane.add(m_colorOK);
                m_buttonPane.add(m_colorCancel);
                m_colorPane.add(m_buttonPane);

                m_ColorChooser.getContentPane().add(m_colorPane);
                m_ColorChooser.setSize(450, 300);
                m_ColorChooser.setTitle("Choose BoundingBox Color");

                // add(m_trans);
                // add(m_slider);
                // add(m_sliderValue);
                //
                // add(m_boundingBoxColor);
                // add(m_resetColor);

                m_boundingBoxColor
                                .addActionListener(new java.awt.event.ActionListener() {
                                        @Override
                                        public void actionPerformed(
                                                        java.awt.event.ActionEvent evt) {
                                                m_ColorChooser.setVisible(true);
                                        }
                                });

                m_colorCancel.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                m_ColorChooser.setVisible(false);
                        }
                });

                m_colorOK.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                Color newColor = m_cchoose.getColor();
                                SegmentColorTable.setBoundingBoxColor(newColor);

                                m_ColorChooser.setVisible(false);
                                m_eventService.publish(new LabelColoringChangeEvent(
                                                newColor,
                                                SegmentColorTable
                                                                .getColorMapNr()));
                                m_eventService.publish(new ImgRedrawEvent());
                        }
                });

                m_resetColor.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                SegmentColorTable.resetColorMap();
                                m_eventService.publish(new LabelColoringChangeEvent(
                                                SegmentColorTable
                                                                .getBoundingBoxColor(),
                                                SegmentColorTable
                                                                .getColorMapNr()));
                                m_eventService.publish(new ImgRedrawEvent());
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

                gc.gridx = x;
                gc.gridy = y;
                gc.insets = new Insets(0, 5, 0, 5);
                ret.add(new JLabel("Transparency"), gc);

                y++;
                gc.gridy = y;
                ret.add(m_slider, gc);

                y++;
                gc.gridy = y;
                gc.insets = new Insets(20, 5, 0, 5);
                ret.add(new JLabel("Set BoundingBox Color"), gc);

                y++;
                gc.gridy = y;
                gc.insets = new Insets(0, 5, 0, 5);
                ret.add(new JLabel("Reset Color"), gc);

                // 2nd col
                gc.anchor = GridBagConstraints.CENTER;
                gc.weightx = 0.0;

                y = 1; // leave one free for the transparency label
                x++;
                gc.gridx = x;
                gc.gridy = y;
                ret.add(m_sliderValue, gc);

                y++;
                gc.gridy = y;
                gc.insets = new Insets(20, 5, 0, 5);
                ret.add(m_boundingBoxColor, gc);

                y++;
                gc.gridy = y;
                gc.insets = new Insets(0, 5, 0, 5);
                ret.add(m_resetColor, gc);

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
