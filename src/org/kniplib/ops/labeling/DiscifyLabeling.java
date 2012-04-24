package org.kniplib.ops.labeling;

import java.util.ArrayList;
import java.util.Collection;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.UnaryOperation;

/**
 * Discifies a labeling
 * 
 * TODO: What does discify mean? Documentation!!!
 * 
 * @author Thorsten Riess
 * 
 * @param <L>
 */
public class DiscifyLabeling<L extends Comparable<L>> implements
                UnaryOperation<Labeling<L>, Labeling<L>> {

        private int m_radius;

        /**
	 * 
	 */
        public DiscifyLabeling(int radius) {
                m_radius = radius;
        }

        @Override
        public Labeling<L> compute(Labeling<L> op, Labeling<L> r) {

                Collection<L> labels = op.getLabels();
                IterableInterval<LabelingType<L>> ii = null;

                RandomAccess<LabelingType<L>> ra = r.randomAccess();
                Cursor<LabelingType<L>> c = null;
                ArrayList<L> nl = null;
                Collection<L> l = null;
                long pos[] = new long[op.numDimensions()];
                long centroidi[] = new long[op.numDimensions()];
                double centroid[];
                long count = 0;
                for (L label : labels) {
                        ii = op.getIterableRegionOfInterest(label)
                                        .getIterableIntervalOverROI(op);
                        c = ii.localizingCursor();

                        centroid = new double[ii.numDimensions()];
                        count = 0;

                        while (c.hasNext()) {
                                c.fwd();
                                for (int i = 0; i < centroid.length; i++) {
                                        centroid[i] += c.getDoublePosition(i);
                                }
                                count++;
                        }

                        for (int i = 0; i < centroid.length; i++) {
                                centroid[i] /= count;
                                centroidi[i] = (long) centroid[i];
                        }

                        double dist;
                        c.reset();
                        while (c.hasNext()) {
                                c.fwd();
                                c.localize(pos);
                                dist = 0;
                                for (int i = 0; i < centroid.length; i++) {
                                        dist += (pos[i] - centroid[i])
                                                        * (pos[i] - centroid[i]);
                                }
                                if (dist < m_radius * m_radius) {
                                        ra.setPosition(pos);
                                        l = ra.get().getLabeling();
                                        nl = new ArrayList<L>(l);
                                        nl.add(label);
                                        ra.get().setLabeling(nl);
                                }
                        }

                }
                return r;

        }

        @Override
        public UnaryOperation<Labeling<L>, Labeling<L>> copy() {
                return new DiscifyLabeling<L>(m_radius);
        }
}
