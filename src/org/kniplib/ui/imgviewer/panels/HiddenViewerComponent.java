package org.kniplib.ui.imgviewer.panels;

import java.awt.Component;

import org.kniplib.ui.imgviewer.ViewerComponent;

public abstract class HiddenViewerComponent extends ViewerComponent {

        /**
	 * 
	 */
        private static final long serialVersionUID = 1L;

        public HiddenViewerComponent() {
                super("", true);
        }

        @Override
        public String getPosition() {
                return "HIDDEN";
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }
}
