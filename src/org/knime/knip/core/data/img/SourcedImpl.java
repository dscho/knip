package org.knime.knip.core.data.img;

import net.imglib2.meta.Sourced;

public class SourcedImpl implements Sourced {

        private String m_source = "";

        public SourcedImpl() {
        }

        public SourcedImpl(String source) {
                m_source = source;
        }

        public SourcedImpl(Sourced sourced) {
                m_source = sourced.getSource();
        }

        @Override
        public void setSource(String source) {
                m_source = source;
        }

        @Override
        public String getSource() {
                return m_source;
        }

}
