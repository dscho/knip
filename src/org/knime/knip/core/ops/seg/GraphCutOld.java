/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 * 
 * History
 *   30 Dec 2010 (hornm): created
 */
package org.knime.knip.core.ops.seg;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.ops.BinaryOutputOperation;
import net.imglib2.ops.operation.unary.iterableinterval.MakeHistogram;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.IntervalIndexer;

import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter;

/**
 * GraphCut
 * 
 * TODO: Kick it? Make it Iterable?
 * 
 * @author dietzc, University of Konstanz
 */

@SuppressWarnings("rawtypes")
public class GraphCutOld<T extends RealType<T>, L extends Comparable<L>>
                implements
                BinaryOutputOperation<Img<T>, Labeling<L>, Img<BitType>> {

        private ImgFactory m_factory;

        private List<long[]> m_sources;

        private List<long[]> m_sinks;

        private double m_srcAvg;

        private double m_sinkAvg;

        private double m_lambda;

        private String m_bgLabel;

        private String m_fgLabel;

        public GraphCutOld(ImgFactory factory, double lambda, String fgLabel,
                        String bgLabel) {
                m_bgLabel = "*" + bgLabel + "*";
                m_fgLabel = "*" + fgLabel + "*";
                m_factory = factory;
                m_lambda = lambda;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public Img<BitType> createEmptyOutput(Img<T> src,
                        Labeling<L> labelingAsImg) {

                long[] dims = new long[src.numDimensions()];
                src.dimensions(dims);

                return m_factory.create(dims, new BitType());
        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        public Img<BitType> compute(Img<T> src, Labeling<L> labeling,
                        Img<BitType> res) {

                boolean isBg, isFg;

                m_sources = new ArrayList<long[]>();
                m_sinks = new ArrayList<long[]>();

                for (L label : labeling.getLabels()) {
                        isBg = RulebasedLabelFilter.isValid(label, m_bgLabel);
                        isFg = RulebasedLabelFilter.isValid(label, m_fgLabel);

                        if (!isBg && !isFg)
                                continue;

                        Cursor<T> roiCursor = labeling
                                        .getIterableRegionOfInterest(label)
                                        .getIterableIntervalOverROI(src)
                                        .localizingCursor();

                        long[] pos = new long[labeling.numDimensions()];
                        while (roiCursor.hasNext()) {
                                roiCursor.fwd();
                                roiCursor.localize(pos);
                                if (isBg) {
                                        m_sources.add(pos.clone());
                                        m_srcAvg += roiCursor.get()
                                                        .getRealDouble();
                                }
                                if (isFg) {
                                        m_sinks.add(pos.clone());
                                        m_sinkAvg += roiCursor.get()
                                                        .getRealDouble();
                                }

                        }

                }

                if (m_sinks.size() == 0 && m_sources.size() == 0)
                        throw new IllegalStateException(
                                        "GraphCut needs sinks and sources");

                m_srcAvg /= m_sources.size();
                m_sinkAvg /= m_sinks.size();

                calculateGraphCut(src, res);
                return res;
        }

        /**
         * calculates the GraphCut on the Image Img
         * 
         * @param img
         *                The Image
         * @return the processed Image with black and white values for sink and
         *         Source
         */
        private void calculateGraphCut(Img<T> src, Img<BitType> res) {

                /*
                 * Image has been normalized before. Therefore the lowest and
                 * the highest possible values MUST exist. These can be used as
                 * sources and sinks for the graphcut algorithm.
                 */

                /*
                 * Calculate the 'camera noise' as the std. deviation of the
                 * image's pixel values.
                 */
                MakeHistogram<T> histOp = new MakeHistogram<T>();
                final int[] bins = histOp.compute(src,
                                histOp.createEmptyOutput(src)).hist();

                long noPixels = 0;
                long sumValues = 0;
                for (int i = 0; i < bins.length; ++i) {
                        sumValues += i * bins[i];
                        noPixels += bins[i];
                }
                final double mean = sumValues / noPixels;
                long sum = 0;
                for (int i = 0; i < bins.length; ++i) {
                        sum += bins[i] * (i - mean) * (i - mean);
                }
                final float stdDev = (float) Math.sqrt(sum / noPixels);

                // for the neighbor nodes we need the slower ByDim Cursor
                final RandomAccess<T> srcRandomAcess = src.randomAccess();
                final Cursor<T> srcCursor = src.localizingCursor();

                // get the number of nodes and number of edges from all
                // dimensions
                final long numNodes = src.size();
                final long numEdges = numNodes * (src.numDimensions() + 2);

                final org.knime.knip.core.algorithm.GraphCutAlgorithm graphCut = new org.knime.knip.core.algorithm.GraphCutAlgorithm(
                                (int) numNodes, (int) numEdges);

                /*
                 * computing the edge weights and Computing the maximum weight
                 * for the K-value (p. 108 "Interactive Graph Cuts")
                 */
                float K_value = 0;

                // the neighbor position for looking at the adjacent nodes
                long[] dims = new long[src.numDimensions()];
                src.dimensions(dims);
                long[] neighborPos;
                long[] cursorPos = new long[srcCursor.numDimensions()];
                while (srcCursor.hasNext()) {
                        srcCursor.fwd();

                        // get the position of the cursor
                        srcCursor.localize(cursorPos);
                        neighborPos = cursorPos.clone();
                        srcRandomAcess.setPosition(srcCursor);
                        final int nodeID = listPosition(cursorPos, dims);
                        final T value = srcCursor.get();
                        for (int d = 0; d < dims.length; d++) {
                                // one step back to the neighbor in negative
                                // direction
                                neighborPos[d] -= 1;

                                // if we are not at the lower dimension bounds
                                if (neighborPos[d] >= 0) {

                                        /*
                                         * weight according to p.109 lower right
                                         * in the paper (ad-hoc) function
                                         */

                                        // get the intensity from this pixel
                                        final float intensity1 = value
                                                        .getRealFloat();

                                        // get the intensity from the neighbor
                                        // pixel
                                        srcRandomAcess.bck(d);
                                        srcRandomAcess.localize(cursorPos);
                                        final int neighborID = listPosition(
                                                        cursorPos, dims);
                                        final float intensity2 = srcRandomAcess
                                                        .get().getRealFloat();

                                        srcRandomAcess.fwd(d);

                                        float weight = -((intensity1 - intensity2)
                                                        * (intensity1 - intensity2) / (2 * stdDev * stdDev));

                                        // save maximum value for K
                                        K_value = Math.max(K_value, weight);

                                        // assumption distance between nodes is
                                        // 1
                                        weight = (float) Math.exp(weight);

                                        weight *= (1 - m_lambda);

                                        graphCut.setEdgeWeight(nodeID,
                                                        neighborID, weight);
                                }

                                neighborPos[d] += 1;
                        }

                }

                // K has to be bigger than all weights in the graph ==> +1
                K_value = (K_value * dims.length) + 1;

                /*
                 * computing the weights to source and sink using the K-value
                 */

                srcCursor.reset();
                while (srcCursor.hasNext()) {
                        srcCursor.fwd();
                        srcCursor.localize(cursorPos);
                        final int nodeID = listPosition(cursorPos, dims);

                        final T value = srcCursor.get();

                        if (m_sinks.contains(cursorPos)) {
                                // found sink at loc_cursor.getPosition()
                                graphCut.setTerminalWeights(nodeID, 0, K_value);
                        } else if (m_sources.contains(cursorPos)) {
                                // found source at loc_cursor.getPosition()
                                graphCut.setTerminalWeights(nodeID, K_value, 0);
                        } else {
                                float r_Source = (float) -Math.log(1.0 / Math
                                                .abs(value.getRealFloat()
                                                                - m_srcAvg));

                                float r_Sink = (float) -Math.log(1.0 / Math
                                                .abs(value.getRealFloat()
                                                                - m_sinkAvg));

                                r_Source *= m_lambda;
                                r_Sink *= m_lambda;
                                graphCut.setTerminalWeights(nodeID, r_Source,
                                                r_Sink);
                        }

                }

                // compute the maximum flow i.e. the graph cut
                graphCut.computeMaximumFlow(false, null);

                Cursor<BitType> resCursor = res.localizingCursor();

                // Set output image
                long[] resPos = new long[resCursor.numDimensions()];
                while (resCursor.hasNext()) {
                        resCursor.fwd();
                        resCursor.localize(resPos);
                        resCursor.get()
                                        .set(graphCut.getTerminal(
                                                        listPosition(resPos,
                                                                        dims))
                                                        .equals(org.knime.knip.core.algorithm.GraphCutAlgorithm.Terminal.BACKGROUND));

                }

        }

        /**
         * Gives the position of the node in the list from the pixel position in
         * the image.
         * 
         * @param imagePosition
         *                Coordinates of the pixel in x,y,z,... direction
         * @param dimensions
         *                overall image dimensions (width, height, depth,...)
         * @return the position of the node in the list
         */
        private static synchronized int listPosition(
                        final long[] imagePosition, final long[] dimensions) {
                return (int) IntervalIndexer.positionToIndex(imagePosition,
                                dimensions);
        }

        @Override
        public BinaryOutputOperation<Img<T>, Labeling<L>, Img<BitType>> copy() {
                return new GraphCutOld<T, L>(m_factory, m_lambda, m_fgLabel,
                                m_bgLabel);
        }

        @Override
        public Img<BitType> compute(Img<T> in1, Labeling<L> in2) {
                return compute(in1, in2, createEmptyOutput(in1, in2));
        }
}
