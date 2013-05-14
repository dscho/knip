package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Component;

import org.knime.knip.core.ui.imgviewer.ViewerComponent;

public abstract class HiddenViewerComponent extends ViewerComponent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public HiddenViewerComponent() {
        super("", true);
    }

    @Override
    public Position getPosition() {
        return Position.HIDDEN;
    }

    @Override
    public void setParent(final Component parent) {
        // Nothing to do here
    }
}
