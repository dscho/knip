package org.knime.knip.core.ui.imgviewer.events;

import org.knime.knip.core.ui.event.KNIPEvent;

/**
 * @author muethingc
 */
public class CrosshairSettingsChgEvent implements KNIPEvent {

        private final int m_thickness;
        private final float m_alpha;

        public CrosshairSettingsChgEvent(final int thickness, final float alpha) {
            m_thickness = thickness;
            m_alpha = alpha;
        }

        public int getThickness() {
            return m_thickness;
        }

        public float getAlpha() {
            return m_alpha;
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
