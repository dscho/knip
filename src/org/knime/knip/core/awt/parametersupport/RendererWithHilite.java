package org.knime.knip.core.awt.parametersupport;

import java.util.Set;

public interface RendererWithHilite {

        public void setHilitedLabels(Set<String> hilitedLabels);

        public void setHiliteMode(boolean isHiliteMode);
}
