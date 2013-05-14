package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventService;

public interface TransferFunctionControlDataProvider<T extends RealType<T>> {

    /**
     * Set a new EventService for this data provider and the wrapped {@link TransferFunctionControlPanel}.<br>
     * 
     * @param service the new event service
     */
    public void setEventService(final EventService service);

    /**
     * Set how many bins should be used for creating the histogram.<br>
     * 
     * @param bins the number of bins, will be set to 1 if bins < 1
     */
    public void setNumberBins(final int bins);

    /**
     * Get the wrapped transfer function control instance.<br>
     * 
     * @return the instance
     */
    public TransferFunctionControlPanel getControl();
}
