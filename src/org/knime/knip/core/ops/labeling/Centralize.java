package org.knime.knip.core.ops.labeling;

import java.util.Arrays;
import java.util.Collection;

import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.operation.BinaryObjectFactory;
import net.imglib2.ops.operation.BinaryOutputOperation;
import net.imglib2.ops.operation.iterableinterval.unary.Centroid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.core.algorithm.PolarImageFactory;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter;

public class Centralize<T extends RealType<T>, L extends Comparable<L>> implements
        BinaryOutputOperation<Img<T>, Labeling<L>, Labeling<L>> {

    private final int m_radius;

    private final int m_numAngles;

    private final int m_maxIterations;

    private final RulebasedLabelFilter<L> m_filter;

    public Centralize(final RulebasedLabelFilter<L> filter, final int radius, final int numAngles,
                      final int maxIterations) {
        m_radius = radius;
        m_numAngles = numAngles;
        m_maxIterations = maxIterations;
        m_filter = filter;
    }

    @Override
    public Labeling<L> compute(final Img<T> img, final Labeling<L> labeling, final Labeling<L> r) {
        if (img.numDimensions() != 2) {
            throw new IllegalArgumentException("Only labelings / images with dimensionality = 2  are allowed");
        }

        final T val = img.firstElement().createVariable();
        val.setReal(val.getMinValue());

        final Centroid centroidOp = new Centroid();
        final CentralizeOnePoint<T> centralizeOnePointOp =
                new CentralizeOnePoint<T>(new PolarImageFactory<T>(Views.extendMirrorDouble(img)), m_maxIterations,
                        m_radius, m_numAngles);

        final RandomAccess<LabelingType<L>> resAccess = r.randomAccess();
        final RandomAccess<LabelingType<L>> srcAccess = labeling.randomAccess();

        final long[] posBuffer = new long[resAccess.numDimensions()];

        final Collection<L> labels = labeling.getLabels();
        for (final L label : labels) {
            if (!m_filter.isValid(label)) {
                continue;
            }

            final IterableInterval<T> labelRoi =
                    labeling.getIterableRegionOfInterest(label).getIterableIntervalOverROI(img);

            final double[] centroid = centroidOp.compute(labelRoi, new double[labelRoi.numDimensions()]);

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

    public Labeling<L> createType(final Img<T> src, final Labeling<L> src2, final long[] dims) {

        return src2.<L> factory().create(dims);
    }

    public long[] resultDims(final Interval srcOp1, final Interval srcOp2) {
        final long[] dims = new long[srcOp1.numDimensions()];
        srcOp1.dimensions(dims);

        return dims;
    }

    @Override
    public BinaryOutputOperation<Img<T>, Labeling<L>, Labeling<L>> copy() {
        return new Centralize<T, L>(m_filter, m_radius, m_numAngles, m_maxIterations);
    }

    @Override
    public BinaryObjectFactory<Img<T>, Labeling<L>, Labeling<L>> bufferFactory() {
        return new BinaryObjectFactory<Img<T>, Labeling<L>, Labeling<L>>() {

            @Override
            public Labeling<L> instantiate(final Img<T> op0, final Labeling<L> op1) {
                return createType(op0, op1, resultDims(op0, op1));
            }
        };
    }
}
