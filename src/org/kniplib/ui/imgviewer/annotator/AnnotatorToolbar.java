package org.kniplib.ui.imgviewer.annotator;

import java.awt.BorderLayout;
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

import org.kniplib.ui.event.EventService;
import org.kniplib.ui.imgviewer.ViewerComponent;
import org.kniplib.ui.imgviewer.annotator.events.AnnotatorToolChgEvent;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorFreeFormTool;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorFreeLineTool;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorPointTool;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorPolygonTool;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorRectangleTool;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorSelectionTool;
import org.kniplib.ui.imgviewer.annotator.tools.AnnotatorSplineTool;

public class AnnotatorToolbar extends ViewerComponent {

        private static final int BUTTON_WIDHT = 150;

        private static final int BUTTON_HEIGHT = 25;

        private static final long serialVersionUID = 1L;

        private EventService m_eventService;

        public AnnotatorToolbar(AnnotatorTool<?>... tools) {
                super("Toolbox", false);
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                final ButtonGroup group = new ButtonGroup();

                for (final AnnotatorTool<?> tool : tools) {
                        final JToggleButton jtb = new JToggleButton(
                                        tool.getName());
                        jtb.setMinimumSize(new Dimension(140, 30));
                        jtb.addItemListener(new ItemListener() {

                                @Override
                                public void itemStateChanged(ItemEvent e) {
                                        if (e.getStateChange() == ItemEvent.SELECTED) {
                                                m_eventService.publish(new AnnotatorToolChgEvent(
                                                                tool));
                                        }

                                }
                        });

                        setButtonIcon(jtb, "icons/" + tool.getIconPath());
                        jtb.setActionCommand(tool.toString());
                        group.add(jtb);
                        jtb.setMaximumSize(new Dimension(BUTTON_WIDHT,
                                        BUTTON_HEIGHT));
                        jtb.setAlignmentX(Component.CENTER_ALIGNMENT);
                        add(jtb);
                }

                // JButton saveButton = new JButton( "Save" );
                // saveButton.addActionListener( new ActionListener()
                // {
                //
                // @Override
                // public void actionPerformed( ActionEvent e )
                // {
                // m_eventService.publish( EventType.SAVEOVERLAY, new File(
                // "/home/eethyo/lucia" ) );
                // }
                // } );
                // add( saveButton );
                //
                // JButton loadButton = new JButton( "Load" );
                // loadButton.addActionListener( new ActionListener()
                // {
                //
                // @Override
                // public void actionPerformed( ActionEvent e )
                // {
                // m_eventService.publish( EventType.LOADOVERLAY, new File(
                // "/home/eethyo/lucia" ) );
                // }
                // } );
                // add( loadButton );
        }

        private final void setButtonIcon(final AbstractButton jb,
                        final String path) {
                URL icon = getClass().getClassLoader().getResource(
                                getClass().getPackage().getName()
                                                .replace('.', '/')
                                                + "/" + path);
                jb.setHorizontalAlignment(SwingConstants.LEFT);
                if (icon != null) {
                        jb.setIcon(new ImageIcon(icon));
                }
        }

        @Override
        public void setEventService(EventService eventService) {
                m_eventService = eventService;
                eventService.subscribe(this);
        }

        @Override
        public String getPosition() {
                return BorderLayout.EAST;
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                // Nothing to save here
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException {
                // Nothing to load here
        }

        public static ViewerComponent createStandardToolbar() {
                return new AnnotatorToolbar(new AnnotatorSelectionTool(),
                                new AnnotatorPointTool(),
                                new AnnotatorRectangleTool(),
                                new AnnotatorPolygonTool(),
                                new AnnotatorSplineTool(),
                                new AnnotatorFreeFormTool(),
                                new AnnotatorFreeLineTool());
        }

        @Override
        public void reset() {
                // Nothing to reset here
        }

        @Override
        public void setParent(Component parent) {
                //
        }
}
