package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import org.knime.knip.core.ui.event.KNIPEvent;

public class NormalizationChgEvent implements KNIPEvent {  

    private final boolean m_normalize;
    
    public NormalizationChgEvent(final boolean value) {
        m_normalize = value;
    }
    
    public final boolean normalize() {
        return m_normalize;
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
