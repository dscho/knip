package org.knime.knip.core.ui.imgviewer;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.knime.knip.core.ui.event.EventServiceClient;

/**
 * Generic component class of a viewer.
 * 
 * @author dietzc, University of Konstanz
 * 
 * @param <T>
 */
public abstract class ViewerComponent extends JPanel implements EventServiceClient {

    private static final long serialVersionUID = 1L;

    protected enum Position {
        CENTER, NORTH, SOUTH, WEST, EAST, HIDDEN;
    }

    /**
     * @param title a unique title for this option panel
     * @param isBorderHidden if true, a border is drawn arround the component
     */
    public ViewerComponent(final String title, final boolean isBorderHidden) {

        if (!isBorderHidden) {
            setTitle(title);
        } else {
            setBorder(BorderFactory.createEmptyBorder());
        }
    }

    /**
     * Set the title for the border of this component.
     * 
     * @param title the title
     */
    public void setTitle(final String title) {
        setBorder(BorderFactory.createTitledBorder(title));
    }

    /**
     * Returns the position in the BorderLayout. Possible values are {@link BorderLayout#NORTH},
     * {@link BorderLayout#SOUTH}, {@link BorderLayout#WEST},{@link BorderLayout#EAST}, {@link BorderLayout#CENTER}
     * ,HIDDEN
     * 
     * A component with hidden position will not be rendered
     * 
     * @return position in {@link BorderLayout} as string
     */
    public abstract Position getPosition();

    /**
     * Serialization
     * 
     * @param out
     * @throws IOException
     */
    public abstract void saveComponentConfiguration(ObjectOutput out) throws IOException;

    /**
     * Deserialization
     */
    public abstract void loadComponentConfiguration(ObjectInput in) throws IOException, ClassNotFoundException;

}
