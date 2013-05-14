package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelColoringChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelOptionsChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewClosedEvent;

public class LabelOptionPanel extends ViewerComponent {

    private class OKAdapter implements ActionListener {
        @Override
        public void actionPerformed(final java.awt.event.ActionEvent evt) {
            final Color newColor = LabelOptionPanel.this.m_colorChooser
                    .getColor();
            SegmentColorTable.setBoundingBoxColor(newColor);

            m_eventService.publish(new LabelOptionsChangeEvent(true));

            m_eventService.publish(new LabelColoringChangeEvent(
                                                                newColor, SegmentColorTable
                                                                .getColorMapNr()));
            m_eventService.publish(new ImgRedrawEvent());
        }
    }

    private static final long serialVersionUID = 1L;

    private EventService m_eventService;

    private JButton m_boundingBoxColor;
    private JButton m_resetColor;

    // ColorChooser Dialog
    private final JColorChooser m_colorChooser = new JColorChooser();
    private JDialog m_colorDialog;
    private final JCheckBox m_renderLabelString = new JCheckBox();

    private final OKAdapter m_adapter;

    public LabelOptionPanel(final boolean isBorderHidden) {
        super("Color Options", isBorderHidden);
        this.m_adapter = new OKAdapter();

        construct();
    }

    public LabelOptionPanel() {
        this(false);
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

        m_boundingBoxColor
        .addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(
                                        final java.awt.event.ActionEvent evt) {
                if (m_colorDialog == null) {
                    m_colorDialog = JColorChooser
                            .createDialog(LabelOptionPanel.this,
                                          "Choose Bounding Box Color",
                                          false,
                                          m_colorChooser,
                                          m_adapter,
                                          null);
                }
                m_colorDialog.setVisible(true);
            }
        });

        m_resetColor.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(
                                        final java.awt.event.ActionEvent evt) {
                SegmentColorTable.resetColorMap();
                m_eventService.publish(new LabelColoringChangeEvent(
                                                                    SegmentColorTable
                                                                    .getBoundingBoxColor(),
                                                                    SegmentColorTable
                                                                    .getColorMapNr()));
                m_eventService.publish(new ImgRedrawEvent());
            }
        });

        m_renderLabelString.setSelected(false);
        m_renderLabelString.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                m_eventService.publish(new LabelOptionsChangeEvent(
                                                                   m_renderLabelString
                                                                   .isSelected()));
                m_eventService.publish(new ImgRedrawEvent());
            }
        });

        add(createComponentPanel());
    }

    private JPanel createComponentPanel() {
        final JPanel ret = new JPanel();
        ret.setLayout(new GridBagLayout());

        final GridBagConstraints gc = new GridBagConstraints();
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
        ret.add(new JLabel("Change Random Label Colors"), gc);

        y++;
        gc.gridy = y;
        gc.insets = new Insets(5, 5, 0, 5);
        ret.add(new JLabel("Show Bounding Box Names"), gc);

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

        y++;
        gc.gridy = y;
        gc.insets = new Insets(5, 5, 0, 5);
        ret.add(m_renderLabelString, gc);

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
    public void setEventService(final EventService eventService) {
        m_eventService = eventService;
        eventService.subscribe(this);
    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out)
            throws IOException {
        // color codings cannot be saved

    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in)
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
    public void setParent(final Component parent) {
        // Nothing to do here
    }

    @EventListener
    public void onClose(final ViewClosedEvent e) {
        if (m_colorDialog != null) {
            m_colorDialog.dispose();
            m_colorDialog = null;
        }
    }
}
