package org.knime.knip.core.ui.imgviewer.events;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.knime.knip.core.data.labeling.LabelFilter;
import org.knime.knip.core.ui.event.KNIPEvent;

public class NameSetbasedLabelFilter<L extends Comparable<L>> implements
                LabelFilter<L>, Externalizable, KNIPEvent {

        private HashSet<String> m_filterSet;
        private boolean m_includeMatches;

        public NameSetbasedLabelFilter(final boolean includeMatches) {
                m_filterSet = new HashSet<String>();
                m_includeMatches = includeMatches;
        }

        public NameSetbasedLabelFilter(final HashSet<String> filterSet,
                        final boolean includeMatches) {
                m_filterSet = filterSet;
                m_includeMatches = includeMatches;
        }


        public void addFilter(final String filter) {
                m_filterSet.add(filter);
        }

        public void setFilterSet(final HashSet<String> filterSet) {
                m_filterSet = filterSet;
        }

        public int sizeOfFilterSet() {
                return m_filterSet.size();
        }

        @Override
        public ExecutionPriority getExecutionOrder() {
                return ExecutionPriority.NORMAL;
        }


        @Override
        public <E extends KNIPEvent> boolean isRedundant(final E thatEvent) {
                return this.equals(thatEvent);
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
                out.writeObject(m_filterSet);
                out.writeBoolean(m_includeMatches);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException,
                        ClassNotFoundException {
                m_filterSet = (HashSet<String>) in.readObject();
                m_includeMatches = in.readBoolean();
        }

        @Override
        public Collection<L> filterLabeling(final Collection<L> labels) {
                final Collection<L> ret = new LinkedList<L>();

                if (m_includeMatches) {
                        for (final L label : labels) {
                                if (m_filterSet.contains(labels.toString())) {
                                        ret.add(label);
                                }
                        }
                } else {
                        for (final L label : labels) {
                                if (!m_filterSet.contains(label.toString())) {
                                        ret.add(label);
                                }
                        }
                }


                return ret;
        }


        @Override
        public void clear() {
                m_filterSet.clear();
        }

}
