package org.knime.knip.core.ops.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;

import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter;

/**
 * Dependencies of labels to each other are computed. e.g. if one LabelingType
 * contains two labels A and B, then A has reflexive relation to B.
 *
 * The node can be used in two modes:
 *
 * a. Intersection mode: A must appear at least once together with B two have a
 * relation to B.
 *
 * b. Complete mode: A must always appear with B two have a relation to B.
 *
 * Two filters are helping to reduce the amount of labels, for which the
 * relations are computed. 1. The left one filters the labels on the left left
 * side of the relation 2. The right one filters the labels on the right side of
 * the relation. Both filters use the given Rules {@link RulebasedLabelFilter}.
 *
 * @author dietzc, hornm
 **/
public class LabelingDependency<L extends Comparable<L>> implements
UnaryOutputOperation<Labeling<L>, Map<L, List<L>>> {

    private final RulebasedLabelFilter<L> m_leftFilter;

    private final RulebasedLabelFilter<L> m_rightFilter;

    private final boolean m_intersectionMode;

    public LabelingDependency(final RulebasedLabelFilter<L> leftFilter,
                              final RulebasedLabelFilter<L> rightFilter,
                              final boolean intersectionMode) {
        m_leftFilter = leftFilter;
        m_rightFilter = rightFilter;
        m_intersectionMode = intersectionMode;
    }

    @Override
    public Map<L, List<L>> compute(final Labeling<L> op, final Map<L, List<L>> r) {

        final HashMap<L, HashMap<L, Integer>> labelMap = new HashMap<L, HashMap<L, Integer>>();
        final HashMap<L, Integer> sizeMap = new HashMap<L, Integer>();

        final Cursor<LabelingType<L>> cursor = op.cursor();

        while (cursor.hasNext()) {
            cursor.fwd();

            if (cursor.get().getLabeling().isEmpty()) {
                continue;
            }

            for (final L outerL : m_leftFilter.filterLabeling(cursor
                                                              .get().getLabeling())) {

                if (!labelMap.containsKey(outerL)) {
                    labelMap.put(outerL,
                                 new HashMap<L, Integer>());
                    sizeMap.put(outerL, 0);
                }

                for (final L innerL : m_rightFilter
                        .filterLabeling(cursor.get()
                                        .getLabeling())) {
                    if (outerL.equals(innerL)) {
                        continue;
                    }

                    if (!labelMap.get(outerL).containsKey(
                                                          innerL)) {
                        labelMap.get(outerL).put(
                                                 innerL, 0);
                    }

                    labelMap.get(outerL)
                    .put(innerL,
                         labelMap.get(outerL)
                         .get(innerL) + 1);
                }

                if (!m_intersectionMode) {
                    sizeMap.put(outerL,
                                sizeMap.get(outerL) + 1);
                }
            }
        }

        for (final L l : labelMap.keySet()) {
            final List<L> members = new ArrayList<L>();
            if (sizeMap.get(l) > 0) {
                for (final L groupMember : labelMap.get(l).keySet()) {
                    if (labelMap.get(l).get(groupMember)
                            .equals(sizeMap.get(l))) {
                        members.add(groupMember);
                    }
                }

            } else {
                for (final L groupMember : labelMap.get(l).keySet()) {
                    members.add(groupMember);
                }
            }

            if ((members.size() > 0)
                    || (m_rightFilter.getRules().size() == 0)) {
                r.put(l, members);
            }
        }
        return r;

    }

    @Override
    public UnaryOutputOperation<Labeling<L>, Map<L, List<L>>> copy() {
        return new LabelingDependency<L>(m_leftFilter.copy(),
                m_leftFilter.copy(), m_intersectionMode);
    }

    @Override
    public UnaryObjectFactory<Labeling<L>, Map<L, List<L>>> bufferFactory() {
        return new UnaryObjectFactory<Labeling<L>, Map<L, List<L>>>() {

            @Override
            public Map<L, List<L>> instantiate(final Labeling<L> a) {
                return new HashMap<L, List<L>>();
            }
        };
    }
}
