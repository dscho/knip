package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import org.knime.knip.core.ui.event.KNIPEvent;

public class NormalizationPerformedEvent implements KNIPEvent {

    private final boolean m_normalized;

    public NormalizationPerformedEvent(final boolean value) {
        m_normalized = value;
    }

    public final boolean normalize() {
        return m_normalized;
    }

    @Override
    public ExecutionPriority getExecutionOrder() {
        return ExecutionPriority.NORMAL;
    }

    @Override
    public <E extends KNIPEvent> boolean isRedundant(final E thatEvent) {
        return false;
    }

}
