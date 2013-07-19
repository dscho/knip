package org.knime.knip.core.ui.imgviewer.annotator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorToolChgEvent;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorFreeFormTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorFreeLineTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorNoTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorPointTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorPolygonTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorRectangleTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorSelectionTool;
import org.knime.knip.core.ui.imgviewer.annotator.tools.AnnotatorSplineTool;

public class AnnotatorToolbar extends ViewerComponent {

    private static final int BUTTON_WIDHT = 150;

    private static final int BUTTON_HEIGHT = 25;

    private static final long serialVersionUID = 1L;

    private EventService m_eventService;

    public AnnotatorToolbar(final AnnotatorTool<?>... tools) {
        super("Toolbox", false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        final ButtonGroup group = new ButtonGroup();

        for (final AnnotatorTool<?> tool : tools) {
            final JToggleButton jtb = new JToggleButton(tool.getName());
            jtb.setMinimumSize(new Dimension(140, 30));
            jtb.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(final ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        m_eventService.publish(new AnnotatorToolChgEvent(tool));
                    }

                }
            });

            setButtonIcon(jtb, "icons/" + tool.getIconPath());
            jtb.setActionCommand(tool.toString());
            group.add(jtb);
            jtb.setMaximumSize(new Dimension(BUTTON_WIDHT, BUTTON_HEIGHT));
            jtb.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(jtb);
        }
    }

    private final void setButtonIcon(final AbstractButton jb, final String path) {
        final URL icon =
                getClass().getClassLoader().getResource(getClass().getPackage().getName().replace('.', '/') + "/"
                                                                + path);
        jb.setHorizontalAlignment(SwingConstants.LEFT);
        if (icon != null) {
            jb.setIcon(new ImageIcon(icon));
        }
    }

    @Override
    public void setEventService(final EventService eventService) {
        m_eventService = eventService;
        eventService.subscribe(this);
    }

    @Override
    public Position getPosition() {
        return Position.EAST;
    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        // Nothing to save here
    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException {
        // Nothing to load here
    }

    public static ViewerComponent createStandardToolbar() {
        return new AnnotatorToolbar(new AnnotatorNoTool("pan"), new AnnotatorSelectionTool(), new AnnotatorPointTool(),
                new AnnotatorRectangleTool(), new AnnotatorPolygonTool(), new AnnotatorSplineTool(),
                new AnnotatorFreeFormTool(), new AnnotatorFreeLineTool());
    }
}
