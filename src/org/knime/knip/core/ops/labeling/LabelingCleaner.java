package org.knime.knip.core.ops.labeling;

import net.imglib2.Cursor;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.UnaryOperation;

/**
 * 
 * 
 * @author schoenen
 * 
 * @param <L>
 */
public final class LabelingCleaner<L extends Comparable<L>> implements UnaryOperation<Labeling<L>, Labeling<L>> {

    @Override
    public final Labeling<L> compute(final Labeling<L> op, final Labeling<L> res) {
        for (final L l : op.getLabels()) {
            final Cursor<LabelingType<L>> c =
                    op.getIterableRegionOfInterest(l).getIterableIntervalOverROI(res).cursor();
            while (c.hasNext()) {
                c.next().setLabel(l);
            }
        }

        return res;
    }

    @Override
    public UnaryOperation<Labeling<L>, Labeling<L>> copy() {
        return new LabelingCleaner<L>();
    }
}
