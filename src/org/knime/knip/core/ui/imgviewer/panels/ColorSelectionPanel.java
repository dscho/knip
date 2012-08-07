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

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelColoringChangeEvent;

public class ColorSelectionPanel extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private EventService m_eventService;

        private JButton m_boundingBoxColor;
        private JButton m_resetColor;

        // ColorChooser Dialog
        private JButton m_colorOK;
        private JButton m_colorCancel;
        private JColorChooser m_cchoose;
        private final JFrame m_ColorChooser = new JFrame();

        private final JPanel m_buttonPane = new JPanel();
        private final JPanel m_colorPane = new JPanel();

        public ColorSelectionPanel(boolean isBorderHidden) {
                super("Color Options", isBorderHidden);
                construct();
        }

        public ColorSelectionPanel() {
                super("Color Options", false);
                construct();
        }

        private void construct() {
                setMinimumSize(new Dimension(240, 80));
                setPreferredSize(new Dimension(240, 80));
                setMaximumSize(new Dimension(240, this.getMaximumSize().height));
                setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

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


                gc.gridy = y;
                gc.insets = new Insets(5, 5, 0, 5);
                ret.add(new JLabel("Set BoundingBox Color"), gc);

                y++;
                gc.gridy = y;
                gc.insets = new Insets(0, 5, 0, 5);
                ret.add(new JLabel("Reset Color"), gc);

                // 2nd col
                gc.anchor = GridBagConstraints.CENTER;
                gc.weightx = 0.0;

                y = 0;
                x++;
                gc.gridy = y;
                gc.insets = new Insets(5, 5, 0, 5);
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
                // color codings cannot be saved

        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                // color codings cannot be saved
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
