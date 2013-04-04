package org.knime.knip.core.ui.imgviewer.events;

import java.util.Arrays;

import org.knime.knip.core.ui.event.KNIPEvent;

public class CalibrationUpdateEvent implements KNIPEvent {

        private final int[] m_selectedDims;
        private final double[] m_scaleFactors;

        public CalibrationUpdateEvent(double[] scaleFactors, int[] selectedDims) {
                m_selectedDims = selectedDims;
                m_scaleFactors = scaleFactors;
        }

        @Override
        public ExecutionPriority getExecutionOrder() {
                return ExecutionPriority.NORMAL;
        }

        @Override
        public <E extends KNIPEvent> boolean isRedundant(E thatEvent) {
                return this.equals(thatEvent);
        }

        public int[] getSelectedDims() {
                return m_selectedDims;
        }

        public double[] getScaleFactors() {
                return m_scaleFactors;
        }

        @Override
        public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + Arrays.hashCode(m_scaleFactors);
                result = prime * result + Arrays.hashCode(m_selectedDims);
                return result;
        }

        @Override
        public boolean equals(Object obj) {
                if (this == obj)
                        return true;
                if (obj == null)
                        return false;
                if (getClass() != obj.getClass())
                        return false;
                CalibrationUpdateEvent other = (CalibrationUpdateEvent) obj;
                if (!Arrays.equals(m_scaleFactors, other.m_scaleFactors))
                        return false;
                if (!Arrays.equals(m_selectedDims, other.m_selectedDims))
                        return false;
                return true;
        }

}
