package org.knime.knip.core.awt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;

import org.knime.knip.core.awt.converter.LabelingTypeARGBConverter;
import org.knime.knip.core.awt.parametersupport.RendererWithHilite;
import org.knime.knip.core.awt.parametersupport.RendererWithLabels;
import org.knime.knip.core.awt.specializedrendering.Projector2D;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter.Operator;

public class RandomColorLabelingRenderer<L extends Comparable<L>> extends
ProjectingRenderer<LabelingType<L>> implements
RendererWithLabels<L>, RendererWithHilite {

    private static int WHITE_RGB = Color.WHITE.getRGB();

    private LabelingTypeARGBConverter<L> m_converter;
    private Operator m_operator;
    private Set<String> m_activeLabels;
    private LabelingMapping<L> m_labelMapping;
    private Set<String> m_hilitedLabels;
    private boolean m_isHiliteMode;

    private boolean m_rebuildRequired;

    public RandomColorLabelingRenderer() {
        m_rebuildRequired = true;
    }

    @Override
    public void setOperator(final Operator operator) {
        m_operator = operator;
        m_rebuildRequired = true;
    }

    @Override
    public void setActiveLabels(final Set<String> activeLabels) {
        m_activeLabels = activeLabels;
        m_rebuildRequired = true;
    }

    @Override
    public void setLabelMapping(final LabelingMapping<L> labelMapping) {
        m_labelMapping = labelMapping;
        m_rebuildRequired = true;
    }

    @Override
    public void setHilitedLabels(final Set<String> hilitedLabels) {
        m_hilitedLabels = hilitedLabels;
        m_rebuildRequired = true;
    }

    @Override
    public void setHiliteMode(final boolean isHiliteMode) {
        m_isHiliteMode = isHiliteMode;
        m_rebuildRequired = true;
    }

    @Override
    public String toString() {
        return "Random Color Labeling Renderer";
    }

    @Override
    protected Abstract2DProjector<LabelingType<L>, ARGBType> getProjector(
                                                                          final int dimX, final int dimY,
                                                                          final RandomAccessibleInterval<LabelingType<L>> source,
                                                                          final ARGBScreenImage target) {

        if (m_rebuildRequired) {
            m_rebuildRequired = false;
            rebuildLabelConverter();
        }

        return new Projector2D<LabelingType<L>, ARGBType>(dimX, dimY,
                source, target, m_converter);
    }

    // create the converter

    private void rebuildLabelConverter() {
        m_rebuildRequired = false;
        final int labelListIndexSize = m_labelMapping.numLists();
        final HashMap<Integer, Integer> colorTable = new HashMap<Integer, Integer>();

        for (int i = 0; i < labelListIndexSize; i++) {

            final int color = getColorForLabeling(m_activeLabels,
                                                  m_operator, m_hilitedLabels,
                                                  m_isHiliteMode,
                                                  m_labelMapping.listAtIndex(i));
            colorTable.put(i, color);
        }

        m_converter = new LabelingTypeARGBConverter<L>(colorTable);
    }

    private int getColorForLabeling(final Set<String> activeLabels, final Operator op,
                                    final Set<String> hilitedLabels, final boolean isHiliteMode,
                                    final List<L> labeling) {

        if (labeling.size() == 0) {
            return WHITE_RGB;
        }

        // standard case no filtering / highlighting
        if ((activeLabels == null) && (hilitedLabels == null)
                && !isHiliteMode) {
            return SegmentColorTable.getColor(labeling);
        }

        List<L> filteredLabels;
        if (activeLabels != null) {
            filteredLabels = intersection(activeLabels, op,
                                          labeling);
        } else {
            filteredLabels = labeling; // do not filter
        }

        if (filteredLabels.size() == 0) {
            return WHITE_RGB;
        } else {
            // highlight if necessary
            if (checkHilite(filteredLabels, hilitedLabels)) {
                return SegmentColorTable.HILITED_RGB;
            } else {
                return isHiliteMode ? SegmentColorTable.NOTSELECTED_RGB
                        : SegmentColorTable
                        .getColor(labeling);
            }
        }
    }

    private boolean checkHilite(final List<L> labeling, final Set<String> hilitedLabels) {
        if ((hilitedLabels != null) && (hilitedLabels.size() > 0)) {
            for (int i = 0; i < labeling.size(); i++) {
                if (hilitedLabels.contains(labeling.get(i)
                                           .toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<L> intersection(final Set<String> activeLabels, final Operator op,
                                 final List<L> labeling) {

        final List<L> intersected = new ArrayList<L>(4);

        if (op == Operator.OR) {
            for (int i = 0; i < labeling.size(); i++) {
                if (activeLabels.contains(labeling.get(i)
                                          .toString())) {
                    intersected.add(labeling.get(i));
                }
            }
        } else if (op == Operator.AND) {
            if (labeling.containsAll(activeLabels)) {
                intersected.addAll(labeling);
            }
        } else if (op == Operator.XOR) {
            boolean addedOne = false;
            for (int i = 0; i < labeling.size(); i++) {
                if (activeLabels.contains(labeling.get(i)
                                          .toString())) {

                    if (!addedOne) {
                        intersected.add(labeling.get(i));
                        addedOne = true;
                    } else {
                        // only 0,1 or 1,0 should result
                        // in a XOR labeling
                        intersected.clear();
                        break;
                    }
                }
            }
        } else {
            // operator not supported return just the empty list
        }

        return intersected;
    }

    @Override
    public void setRenderingWithLabelStrings(final boolean withNumbers) {
        // nothing to do here
    }

}
