package org.knime.knip.core.ui.imgviewer.events;

import org.knime.knip.core.ui.event.KNIPEvent;

public class SetCachingEvent implements KNIPEvent {

    private final boolean m_cache;

    public SetCachingEvent(final boolean cache) {
        m_cache = cache;
    }

    public boolean caching() {
        return m_cache;
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
