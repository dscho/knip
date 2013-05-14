package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;

/**
 * composite of a {@link TransparencyPanel} and a {@link LabelOptionPanel}.
 *
 * @author zinsmaie
 *
 */
public class TransparencyColorSelectionPanel extends ViewerComponent {


    private final TransparencyPanel m_transparencyPanel;
    private final LabelOptionPanel m_colorSelectionPanel;

    public TransparencyColorSelectionPanel() {
        super("Color Options", false);

        m_transparencyPanel = new TransparencyPanel(true);
        m_colorSelectionPanel = new LabelOptionPanel(true);

        setLayout(new GridBagLayout());
        final GridBagConstraints gc = new GridBagConstraints();

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.gridy = 0;

        add(m_transparencyPanel, gc);

        gc.gridy = 1;
        add(m_colorSelectionPanel, gc);

        setMinimumSize(new Dimension(250, 150));
        setPreferredSize(new Dimension(250, 150));
        setMaximumSize(new Dimension(250, 150));

    }

    @Override
    public void setEventService(final EventService eventService) {
        m_transparencyPanel.setEventService(eventService);
        m_colorSelectionPanel.setEventService(eventService);
        eventService.subscribe(this);
    }

    @Override
    public void setParent(final Component parent) {
        // Nothing to do here

    }

    @Override
    public Position getPosition() {
        return Position.SOUTH;
    }

    @Override
    public void reset() {
        // Nothing to do here
    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out)
            throws IOException {
        m_transparencyPanel.saveComponentConfiguration(out);
        m_colorSelectionPanel.saveComponentConfiguration(out);
    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in)
            throws IOException, ClassNotFoundException {
        m_transparencyPanel.loadComponentConfiguration(in);
        m_colorSelectionPanel.loadComponentConfiguration(in);
    }
}
