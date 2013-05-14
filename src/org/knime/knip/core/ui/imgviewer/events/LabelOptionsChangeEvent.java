package org.knime.knip.core.ui.imgviewer.events;

import org.knime.knip.core.ui.event.KNIPEvent;

public class LabelOptionsChangeEvent implements KNIPEvent {

        private final boolean m_renderWithNumbers;

        public LabelOptionsChangeEvent(final boolean renderWithNumbers) {
                m_renderWithNumbers = renderWithNumbers;
        }

        @Override
        public ExecutionPriority getExecutionOrder() {
                return ExecutionPriority.NORMAL;
        }

        @Override
        public <E extends KNIPEvent> boolean isRedundant(final E thatEvent) {
                return this.equals(thatEvent);
        }

        public boolean getRenderWithLabelStrings() {
                return m_renderWithNumbers;
        }

}
