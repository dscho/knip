package org.knime.knip.core.ui.imgviewer.events;

import java.awt.Color;

import org.knime.knip.core.ui.event.KNIPEvent;

/**
 * @author muethingc
 */
public class AddCrosshairEvent implements KNIPEvent {

        private final Color m_xColor;
        private final Color m_yColor;

        public AddCrosshairEvent(final Color xColor, final Color yColor) {
            m_xColor = xColor;
            m_yColor = yColor;
        }

        public Color getXColor() {
            return m_xColor;
        }

        public Color getYColor() {
            return m_yColor;
        }

        @Override
        public ExecutionPriority getExecutionOrder() {
                return ExecutionPriority.NORMAL;
        }

        @Override
        public <E extends KNIPEvent> boolean isRedundant(E thatEvent) {
                return false;
        }

}
