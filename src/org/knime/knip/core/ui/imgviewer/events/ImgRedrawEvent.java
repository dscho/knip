package org.knime.knip.core.ui.imgviewer.events;

import org.knime.knip.core.ui.event.KNIPEvent;


/**
 * a marker event that triggers a redrawing by the providers
 *
 * @author zinsmaie
 *
 */
public class ImgRedrawEvent implements KNIPEvent {

        /**
         * should be executed after parameter changes => low priority
         *
         * {@inheritDoc}
         */
        @Override
        public ExecutionPriority getExecutionOrder() {
                return ExecutionPriority.LOW;
        }

        /**
         * implements class equality (one redraw is enough)
         */
        @Override
        public <E extends KNIPEvent> boolean isRedundant(E thatEvent) {
                return (thatEvent instanceof ImgRedrawEvent);
        }
}
