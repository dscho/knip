package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.TransparencyPanelValueChgEvent;

public class TransparencyPanel extends ViewerComponent {

    private static final long serialVersionUID = 1L;

    private EventService m_eventService;

    private JSlider m_slider;

    private JLabel m_sliderValue;

    private final boolean m_showLabel;

    public TransparencyPanel() {
        super("Transparency", false);
        m_showLabel = false;
        construct();
    }

    public TransparencyPanel(final boolean isBorderHidden) {
        super("Transparency", isBorderHidden);
        m_showLabel = isBorderHidden;
        construct();
    }

    private void construct() {
        setMinimumSize(new Dimension(180, 40));
        setPreferredSize(new Dimension(180, 40));

        m_sliderValue = new JLabel("128");
        m_slider = new JSlider(SwingConstants.HORIZONTAL, 0, 255, 128);
        m_slider.setPreferredSize(new Dimension(130, 17));
        m_slider.setMaximumSize(new Dimension(180, m_slider.getMaximumSize().height));
        m_slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_eventService.publish(new TransparencyPanelValueChgEvent(m_slider.getValue()));
                m_eventService.publish(new ImgRedrawEvent());
                m_sliderValue.setText("" + m_slider.getValue());
            }
        });

        addComponents();
    }

    private void addComponents() {
        setLayout(new GridBagLayout());

        final GridBagConstraints gc = new GridBagConstraints();
        int x = 0;
        int y = 0;

        // all
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // label row
        if (m_showLabel) {
            gc.weightx = 1.0;
            gc.gridx = x;
            gc.gridwidth = 2;
            gc.gridy = y;
            gc.insets = new Insets(0, 5, 5, 0);
            add(new JLabel("Transparency"), gc);

            y++;
            gc.gridwidth = 1;

        }

        // content row
        gc.weightx = 0.0;
        gc.gridx = x;
        gc.gridy = y;
        gc.insets = new Insets(0, 5, 0, 0);
        add(m_slider, gc);

        x++;
        gc.insets = new Insets(0, 10, 0, 0);
        gc.weightx = 1.0;
        gc.gridx = x;
        gc.gridy = y;
        add(m_sliderValue, gc);
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

    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        out.writeInt(m_slider.getValue());

    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
        m_slider.setValue(in.readInt());
    }
}
