package org.knime.knip.core.ui.imgviewer.events;

import java.awt.Color;

import org.knime.knip.core.ui.event.KNIPEvent;

public class LabelColoringChangeEvent implements KNIPEvent {

        private final Color m_boundingBoxColor;
        private final int m_colorMapNr;

        public LabelColoringChangeEvent(final Color boundingBoxColor, final int colorMapNr) {
                m_boundingBoxColor = boundingBoxColor;
                m_colorMapNr = colorMapNr;

        }

        public int getColorMapNr() {
                return m_colorMapNr;
        }

        public Color getBoundingBoxColor() {
                return m_boundingBoxColor;
        }

        @Override
        public ExecutionPriority getExecutionOrder() {
                return ExecutionPriority.NORMAL;
        }

        @Override
        public <E extends KNIPEvent> boolean isRedundant(final E thatEvent) {
                return this.equals(thatEvent);
        }

}
