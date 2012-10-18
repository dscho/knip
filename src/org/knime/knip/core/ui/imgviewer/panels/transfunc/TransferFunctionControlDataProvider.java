package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import org.knime.knip.core.ui.event.EventService;

import net.imglib2.RandomAccessibleInterval;

import net.imglib2.type.numeric.RealType;

public interface TransferFunctionControlDataProvider<T extends RealType<T>, I extends RandomAccessibleInterval<T>> {

        /**
         * Set a new EventService for this data provider and the wrapped {@link TransferFunctionControlPanel}.<br>
         *
         * @param service
         *                the new event service
         */
        public void setEventService(final EventService service);

        /**
         * Set how many bins should be used for creating the histogram.<br>
         *
         * @param bins the number of bins, will be set to 1 if bins < 1
         */
        public void setNumberBins(final int bins);

        /**
         * Set the instance that should be wrapped.<br>
         *
         * This also sets the {@link EventService} of the panel to the same that is used by the instance of this interface.
         *
         * @param panle the panle to wrap
         */
        public void setPanel(final TransferFunctionControlPanel panel);
}
