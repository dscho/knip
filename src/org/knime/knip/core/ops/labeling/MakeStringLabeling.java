package org.knime.knip.core.ops.labeling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imglib2.Cursor;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.UnaryOperation;

public class MakeStringLabeling<L extends Comparable<L>> implements
                UnaryOperation<Labeling<L>, Labeling<String>> {

        @Override
        public Labeling<String> compute(Labeling<L> op, Labeling<String> res) {

                // String based Labeling is generated
                long[] dims = new long[op.numDimensions()];
                op.dimensions(dims);

                LabelingMapping<L> srcMapping = op.firstElement().getMapping();

                int size = 0;
                try {
                        for (size = 0; size < Integer.MAX_VALUE; size++) {
                                srcMapping.listAtIndex(size);
                        }
                } catch (IndexOutOfBoundsException e) {
                        //
                }

                LabelingMapping<String> resMapping = res.firstElement()
                                .getMapping();
                Map<List<L>, List<String>> map = new HashMap<List<L>, List<String>>();
                for (int i = 0; i < size; i++) {
                        List<L> from = srcMapping.listAtIndex(i);
                        List<String> to = new ArrayList<String>(from.size());
                        for (L type : from) {
                                to.add(type.toString());
                        }

                        map.put(from, resMapping.intern(to));
                }

                Cursor<LabelingType<L>> inCursor = op.cursor();
                Cursor<LabelingType<String>> resCursor = res.cursor();

                while (inCursor.hasNext()) {
                        inCursor.fwd();
                        resCursor.fwd();
                        resCursor.get().setLabeling(
                                        map.get(inCursor.get().getLabeling()));
                }

                return res;
        }

        @Override
        public UnaryOperation<Labeling<L>, Labeling<String>> copy() {
                return new MakeStringLabeling<L>();
        }
}
