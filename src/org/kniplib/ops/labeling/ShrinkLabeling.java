package org.kniplib.ops.labeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsRandomAccess;
import net.imglib2.view.Views;

import org.kniplib.ops.misc.LabelingDependency;
import org.kniplib.types.ConnectedType;

/**
 * Shrinks a labeling
 * 
 * @author Christian Dietz
 * 
 * @param <L>
 */
public class ShrinkLabeling<L extends Comparable<L>> implements
                UnaryOperation<Labeling<L>, Labeling<L>> {

        private LabelingDependency<L> m_dependencies;

        private ConnectedType m_ct;

        private int m_numIterations;

        /**
         * 
         * @param ct
         * @param numIterations
         */
        public ShrinkLabeling(ConnectedType ct, int numIterations) {
                this(ct, numIterations, null);
        }

        public ShrinkLabeling(ConnectedType ct, int numIterations,
                        LabelingDependency<L> dependencies) {
                m_dependencies = dependencies;
                m_ct = ct;
                m_numIterations = numIterations;
        }

        @Override
        public Labeling<L> compute(Labeling<L> op, Labeling<L> r) {

                Collection<L> labels = null;

                if (m_dependencies == null) {
                        labels = op.getLabels();
                } else {
                        labels = m_dependencies.compute(op,
                                        m_dependencies.createEmptyOutput(op))
                                        .keySet();
                }

                OutOfBoundsRandomAccess<LabelingType<L>> opRa = Views
                                .extendValue(op,
                                                new LabelingType<L>(
                                                                new ArrayList<L>()))
                                .randomAccess();
                OutOfBoundsRandomAccess<LabelingType<L>> resRa = Views
                                .extendValue(r,
                                                new LabelingType<L>(
                                                                new ArrayList<L>()))
                                .randomAccess();

                IterableInterval<LabelingType<L>> ii = null;
                Cursor<LabelingType<L>> iiCursor = null;
                int[] pos = new int[op.numDimensions()];

                Set<int[]> nextSeeds = new HashSet<int[]>();
                for (int i = 0; i < m_numIterations; i++) {
                        if (i == 0) {
                                if (m_ct == ConnectedType.FOUR_CONNECTED) {

                                        for (L label : labels) {

                                                ii = op.getIterableRegionOfInterest(
                                                                label)
                                                                .getIterableIntervalOverROI(
                                                                                op);

                                                iiCursor = ii.cursor();

                                                while (iiCursor.hasNext()) {
                                                        iiCursor.fwd();

                                                        opRa.setPosition(iiCursor);
                                                        opRa.localize(pos);

                                                        nextSeeds.addAll(operate4Connected(
                                                                        pos,
                                                                        iiCursor.get()
                                                                                        .getLabeling(),
                                                                        opRa,
                                                                        resRa));
                                                }
                                        }
                                } else if (m_ct == ConnectedType.EIGHT_CONNECTED) {
                                        for (L label : labels) {
                                                ii = op.getIterableRegionOfInterest(
                                                                label)
                                                                .getIterableIntervalOverROI(
                                                                                op);

                                                iiCursor = ii.cursor();

                                                while (iiCursor.hasNext()) {
                                                        iiCursor.fwd();

                                                        opRa.setPosition(iiCursor);
                                                        opRa.localize(pos);

                                                        // 8 Connected
                                                        nextSeeds.addAll(operate8Connected(
                                                                        pos,
                                                                        iiCursor.get()
                                                                                        .getLabeling(),
                                                                        opRa,
                                                                        resRa));
                                                }
                                        }
                                }
                        } else {
                                Set<int[]> currentSeeds = new HashSet<int[]>();
                                currentSeeds.addAll(nextSeeds);
                                nextSeeds.clear();
                                if (m_ct == ConnectedType.FOUR_CONNECTED) {

                                        for (int[] currentSeed : currentSeeds) {
                                                resRa.setPosition(currentSeed);

                                                nextSeeds.addAll(operate4Connected(
                                                                currentSeed,
                                                                resRa.get()
                                                                                .getLabeling(),
                                                                resRa, resRa));
                                        }
                                }

                                if (m_ct == ConnectedType.EIGHT_CONNECTED) {
                                        for (int[] currentSeed : currentSeeds) {
                                                resRa.setPosition(currentSeed);

                                                nextSeeds.addAll(operate8Connected(
                                                                currentSeed,
                                                                resRa.get()
                                                                                .getLabeling(),
                                                                resRa, resRa));
                                        }
                                }

                        }
                }
                return r;

        }

        private synchronized Collection<int[]> operate4Connected(
                        int[] currentPos, List<L> currentLabeling,
                        OutOfBoundsRandomAccess<LabelingType<L>> opRa,
                        OutOfBoundsRandomAccess<LabelingType<L>> resRa) {

                List<int[]> nextSeeds = new ArrayList<int[]>();

                // 4 Connected
                opRa.setPosition(currentPos[0] - 1, 0);
                if (!opRa.get().getLabeling().isEmpty()) {
                        opRa.setPosition(currentPos[0] + 1, 0);
                        if (!opRa.get().getLabeling().isEmpty()) {
                                opRa.setPosition(currentPos[0], 0);
                                opRa.setPosition(currentPos[1] - 1, 1);
                                if (!opRa.get().getLabeling().isEmpty()) {
                                        opRa.setPosition(currentPos[1] + 1, 1);
                                        if (!opRa.get().getLabeling().isEmpty()) {
                                                nextSeeds.add(currentPos
                                                                .clone());
                                        }
                                }
                        }
                }

                // Settings result cursor to currentPos
                resRa.setPosition(currentPos);

                // No seeds: Labeling touches empty region
                if (nextSeeds.size() == 0) {
                        resRa.get().setLabeling(
                                        opRa.get().getMapping().emptyList());
                } else {
                        resRa.get().setLabeling(currentLabeling);
                }

                return nextSeeds;
        }

        private synchronized Collection<int[]> operate8Connected(
                        int[] currentPos, List<L> currentLabeling,
                        OutOfBoundsRandomAccess<LabelingType<L>> opRa,
                        OutOfBoundsRandomAccess<LabelingType<L>> resRa) {

                List<int[]> nextSeeds = new ArrayList<int[]>();

                // middle left
                opRa.setPosition(currentPos[0] - 1, 0);
                if (!opRa.get().getLabeling().isEmpty()) {

                        // middle right
                        opRa.setPosition(currentPos[0] + 1, 0);
                        if (!opRa.get().getLabeling().isEmpty()) {

                                // upper right
                                opRa.setPosition(currentPos[1] - 1, 1);
                                if (!opRa.get().getLabeling().isEmpty()) {

                                        // lower right
                                        opRa.setPosition(currentPos[1] + 1, 1);
                                        if (!opRa.get().getLabeling().isEmpty()) {

                                                // lower middle
                                                opRa.setPosition(currentPos[0],
                                                                0);

                                                if (!opRa.get().getLabeling()
                                                                .isEmpty()) {

                                                        // lower left
                                                        opRa.setPosition(
                                                                        currentPos[0] - 1,
                                                                        0);

                                                        if (!opRa.get()
                                                                        .getLabeling()
                                                                        .isEmpty()) {

                                                                // upper left
                                                                opRa.setPosition(
                                                                                currentPos[1] - 1,
                                                                                1);

                                                                if (!opRa.get()
                                                                                .getLabeling()
                                                                                .isEmpty()) {

                                                                        // upper
                                                                        // middle
                                                                        opRa.setPosition(
                                                                                        currentPos[0],
                                                                                        0);
                                                                        if (!opRa.get()
                                                                                        .getLabeling()
                                                                                        .isEmpty()) {
                                                                                nextSeeds.add(currentPos
                                                                                                .clone());
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }

                // Settings result cursor to currentPos
                resRa.setPosition(currentPos);

                // No seeds: Labeling touches empty region
                if (nextSeeds.size() == 0) {
                        resRa.get().setLabeling(
                                        opRa.get().getMapping().emptyList());
                } else {
                        resRa.get().setLabeling(currentLabeling);
                }

                return nextSeeds;
        }

        public UnaryOperation<Labeling<L>, Labeling<L>> copy() {
                return new ShrinkLabeling<L>(m_ct, m_numIterations,
                                (LabelingDependency<L>) m_dependencies.copy());
        }
}
