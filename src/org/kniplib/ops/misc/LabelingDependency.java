package org.kniplib.ops.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.UnaryOutputOperation;

import org.kniplib.ui.imgviewer.events.RulebasedLabelFilter;

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

        private RulebasedLabelFilter<L> m_leftFilter;

        private RulebasedLabelFilter<L> m_rightFilter;

        private boolean m_intersectionMode;

        public LabelingDependency(RulebasedLabelFilter<L> leftFilter,
                        RulebasedLabelFilter<L> rightFilter,
                        boolean intersectionMode) {
                m_leftFilter = leftFilter;
                m_rightFilter = rightFilter;
                m_intersectionMode = intersectionMode;
        }

        @Override
        public HashMap<L, List<L>> createEmptyOutput(Labeling<L> op) {
                return new HashMap<L, List<L>>();
        }

        @Override
        public Map<L, List<L>> compute(Labeling<L> op, Map<L, List<L>> r) {

                HashMap<L, HashMap<L, Integer>> labelMap = new HashMap<L, HashMap<L, Integer>>();
                HashMap<L, Integer> sizeMap = new HashMap<L, Integer>();

                Cursor<LabelingType<L>> cursor = op.cursor();

                while (cursor.hasNext()) {
                        cursor.fwd();

                        if (cursor.get().getLabeling().isEmpty())
                                continue;

                        for (L outerL : m_leftFilter.filterLabeling(cursor
                                        .get().getLabeling())) {

                                if (!labelMap.containsKey(outerL)) {
                                        labelMap.put(outerL,
                                                        new HashMap<L, Integer>());
                                        sizeMap.put(outerL, 0);
                                }

                                for (L innerL : m_rightFilter
                                                .filterLabeling(cursor.get()
                                                                .getLabeling())) {
                                        if (outerL.equals(innerL))
                                                continue;

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

                                if (!m_intersectionMode)
                                        sizeMap.put(outerL,
                                                        sizeMap.get(outerL) + 1);
                        }
                }

                for (L l : labelMap.keySet()) {
                        List<L> members = new ArrayList<L>();
                        if (sizeMap.get(l) > 0) {
                                for (L groupMember : labelMap.get(l).keySet()) {
                                        if (labelMap.get(l).get(groupMember)
                                                        .equals(sizeMap.get(l))) {
                                                members.add(groupMember);
                                        }
                                }

                        } else {
                                for (L groupMember : labelMap.get(l).keySet()) {
                                        members.add(groupMember);
                                }
                        }

                        if (members.size() > 0
                                        || m_rightFilter.getRules().size() == 0)
                                r.put(l, members);
                }
                return r;

        }

        public Map<L, List<L>> compute(Labeling<L> lab) {
                return compute(lab, createEmptyOutput(lab));
        }

        @Override
        public UnaryOutputOperation<Labeling<L>, Map<L, List<L>>> copy() {
                return new LabelingDependency<L>(m_leftFilter.copy(),
                                m_leftFilter.copy(), m_intersectionMode);
        }
}
