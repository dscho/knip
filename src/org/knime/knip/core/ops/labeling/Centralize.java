package org.knime.knip.core.ops.labeling;

import java.util.Arrays;
import java.util.Collection;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.BinaryOperation;
import net.imglib2.ops.BinaryOutputOperation;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.core.algorithm.PolarImageFactory;
import org.knime.knip.core.ops.interval.Centroid;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter;

public class Centralize<T extends RealType<T>, L extends Comparable<L>>
                implements
                BinaryOutputOperation<Img<T>, Labeling<L>, Labeling<L>> {

        private final int m_radius;

        private final int m_numAngles;

        private final int m_maxIterations;

        private final RulebasedLabelFilter<L> m_filter;

        public Centralize(RulebasedLabelFilter<L> filter, int radius,
                        int numAngles, int maxIterations) {
                m_radius = radius;
                m_numAngles = numAngles;
                m_maxIterations = maxIterations;
                m_filter = filter;
        }

        @Override
        public Labeling<L> createEmptyOutput(Img<T> op0, Labeling<L> op1) {
                return createType(op0, op1, resultDims(op0, op1));
        }

        @Override
        public Labeling<L> compute(Img<T> img, Labeling<L> labeling,
                        Labeling<L> r) {
                if (img.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "Only labelings / images with dimensionality = 2  are allowed");
                }

                T val = img.firstElement().createVariable();
                val.setReal(val.getMinValue());

                final Centroid centroidOp = new Centroid();
                CentralizeOnePoint<T> centralizeOnePointOp = new CentralizeOnePoint<T>(
                                new PolarImageFactory<T>(
                                                Views.extendMirrorDouble(img),
                                                m_radius), m_maxIterations,
                                m_numAngles);

                RandomAccess<LabelingType<L>> resAccess = r.randomAccess();
                RandomAccess<LabelingType<L>> srcAccess = labeling
                                .randomAccess();

                long[] posBuffer = new long[resAccess.numDimensions()];

                Collection<L> labels = labeling.getLabels();
                for (L label : labels) {
                        if (!m_filter.isValid(label))
                                continue;

                        final IterableInterval<T> labelRoi = labeling
                                        .getIterableRegionOfInterest(label)
                                        .getIterableIntervalOverROI(img);

                        final double[] centroid = centroidOp.compute(labelRoi);

                        final long[] centroidAsLong = new long[centroid.length];
                        for (int d = 0; d < centroid.length; d++) {
                                centroidAsLong[d] = Math.round(centroid[d]);
                        }

                        Arrays.fill(posBuffer, 0);
                        srcAccess.setPosition(centroidAsLong);
                        centralizeOnePointOp.compute(centroidAsLong, posBuffer);

                        resAccess.setPosition(posBuffer);
                        resAccess.get().set(srcAccess.get());
                }
                return r;
        }

        public Labeling<L> createType(Img<T> src, Labeling<L> src2, long[] dims) {

                return src2.<L> factory().create(dims);
        }

        public long[] resultDims(Interval srcOp1, Interval srcOp2) {
                long[] dims = new long[srcOp1.numDimensions()];
                srcOp1.dimensions(dims);

                return dims;
        }

        @Override
        public BinaryOperation<Img<T>, Labeling<L>, Labeling<L>> copy() {
                return new Centralize<T, L>(m_filter, m_radius, m_numAngles,
                                m_maxIterations);
        }

        @Override
        public Labeling<L> compute(Img<T> in1, Labeling<L> in2) {
                return compute(in1, in2);
        }
}
