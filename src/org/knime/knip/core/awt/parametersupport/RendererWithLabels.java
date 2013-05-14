package org.knime.knip.core.awt.parametersupport;

import java.util.Set;

import net.imglib2.labeling.LabelingMapping;

import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter.Operator;

public interface RendererWithLabels<L extends Comparable<L>> {

    public void setActiveLabels(Set<String> activeLabels);

    public void setOperator(Operator operator);

    public void setLabelMapping(LabelingMapping<L> labelMapping);

    public void setRenderingWithLabelStrings(boolean withNumbers);
}
