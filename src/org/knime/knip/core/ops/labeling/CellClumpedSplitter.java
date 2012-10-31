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
package org.knime.knip.core.ops.labeling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.read.ConvertedRandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.sparse.NtreeImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterableinterval.unary.Centroid;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.DistanceMap;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.LocalMaximaForDistanceMap;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.LocalMaximaForDistanceMap.NeighborhoodType;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.AbstractRegionGrowing;
import net.imglib2.ops.operation.randomaccessibleinterval.unary.regiongrowing.CCA;
import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.sampler.special.ConstantRandomAccessible;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.FloatType;

import org.knime.knip.core.algorithm.extendedem.AttributeTmp;
import org.knime.knip.core.algorithm.extendedem.ExtendedEM;
import org.knime.knip.core.algorithm.extendedem.InstanceTmp;
import org.knime.knip.core.algorithm.extendedem.InstancesTmp;
import org.knime.knip.core.ops.bittype.PositionsToBitTypeImage;

/**
 * <code> code m_executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());<br>
 * final CellClumpedSplitter<T, L> op = new CellClumpedSplitter<T, L>(NeighborhoodType.SIXTEEN, m_executor, ...);<br>
 * m_executor.shutdown();<br>
 * </code>
 *
 * @author metznerj, University of Konstanz
 */
public class CellClumpedSplitter<L extends Comparable<L>> implements
                UnaryOperation<Labeling<L>, Labeling<Integer>> {

        private final LocalMaximaForDistanceMap<FloatType, Img<FloatType>> m_localMaximaOp;

        private final ExecutorService m_executor;

        private final Queue<FutureTask<Long>> m_splittedQueue;

        private final NeighborhoodType m_neighborhood;

        private final double m_minMaximaSize;

        private final double m_ignoreValueBelowAvgPrecent;

        private final int m_maxInterations;

        private final Converter<LabelingType<L>, BitType> m_converter;

        private final DistanceMap<BitType, RandomAccessibleInterval<BitType>, Img<FloatType>> m_distanceMap;

        public CellClumpedSplitter(final NeighborhoodType neighborhood,
                        final ExecutorService executor, double minMaximaSize,
                        double ignoreValueBelowAvgPrecent, int maxInterations) {

                final BitType minBitType = new BitType();
                minBitType.setReal(minBitType.getMinValue());

                m_neighborhood = neighborhood;
                m_localMaximaOp = new LocalMaximaForDistanceMap<FloatType, Img<FloatType>>(
                                neighborhood);

                m_converter = new Converter<LabelingType<L>, BitType>() {

                        @Override
                        public void convert(LabelingType<L> input,
                                        BitType output) {
                                output.set(!input.getLabeling().isEmpty());

                        }
                };
                m_distanceMap = new DistanceMap<BitType, RandomAccessibleInterval<BitType>, Img<FloatType>>();
                m_executor = executor;
                m_minMaximaSize = minMaximaSize;
                m_maxInterations = maxInterations;
                m_ignoreValueBelowAvgPrecent = ignoreValueBelowAvgPrecent;
                m_splittedQueue = new LinkedList<FutureTask<Long>>();
        }

        /**
         *
         */
        public final static int MIN_DIMS = 2;

        /**
         *
         */
        public final static int MAX_DIMS = 3;

        /**
         *
         */
        private Integer m_label = 0;

        // AWTImageTools.showInSameFrame(...)
        @Override
        public Labeling<Integer> compute(final Labeling<L> cellLabeling,
                        final Labeling<Integer> res) {

                /*
                 * dim test
                 */
                final int numDim = cellLabeling.numDimensions();
                if (numDim > MAX_DIMS) {
                        throw new IllegalArgumentException(
                                        "Too many dimensions are selected.");
                }
                if (cellLabeling.numDimensions() < MIN_DIMS) {
                        throw new IllegalArgumentException(
                                        "Two dimensions have to be selected.");
                }

                RandomAccessibleInterval<BitType> bitMask = new ConvertedRandomAccessibleInterval<LabelingType<L>, BitType>(
                                cellLabeling, m_converter, new BitType());

                /*
                 * label queue
                 */
                final Queue<L> cellsQueue = new LinkedList<L>(
                                cellLabeling.getLabels());

                Img<FloatType> distanceMap = m_distanceMap.compute(bitMask,
                                new ArrayImgFactory<FloatType>().create(
                                                bitMask, new FloatType()));

                /*
                 * compute centroids for m prediction
                 */
                final List<long[]> posses = m_localMaximaOp.compute(
                                distanceMap, new ArrayList<long[]>());

                final Img<BitType> maxima = new PositionsToBitTypeImage()
                                .compute(posses, new NtreeImgFactory<BitType>()
                                                .create(cellLabeling,
                                                                new BitType()));

                final Labeling<Integer> lab = new NativeImgLabeling<Integer, IntType>(
                                new NtreeImgFactory<IntType>().create(
                                                cellLabeling, new IntType()));
                new CCA<BitType, Img<BitType>, Labeling<Integer>>(
                                AbstractRegionGrowing.get8ConStructuringElement(maxima
                                                .numDimensions()),
                                new BitType()).compute(maxima, lab);

                final Collection<Integer> labels = lab.firstElement()
                                .getMapping().getLabels();
                final ArrayList<long[]> centroidsList = new ArrayList<long[]>();
                for (final Integer i : labels) {
                        final IterableInterval<BitType> ii = lab
                                        .getIterableRegionOfInterest(i)
                                        .getIterableIntervalOverROI(
                                                        new ConstantRandomAccessible<BitType>(
                                                                        new BitType(),
                                                                        lab.numDimensions()));
                        final double[] centroidD = new Centroid().compute(ii,
                                        new double[ii.numDimensions()]);

                        final long[] centroidL = new long[numDim];
                        for (int d = 0; d < numDim; ++d)
                                centroidL[d] = (long) centroidD[d];
                        centroidsList.add(centroidL);

                }
                /*
                 * computed centroids and saved into localMaxima BitType Img
                 */
                final Img<BitType> localMaxima = new PositionsToBitTypeImage()
                                .compute(centroidsList,
                                                new NtreeImgFactory<BitType>()
                                                                .create(cellLabeling,
                                                                                new BitType()));

                // AWTImageTools.showInSameFrame(localMaxima, 1.0);
                /*
                 * set boundaries. needed for internal cca
                 */
                final long[] boundaries = new long[numDim];
                for (int d = 0; d < numDim; ++d)
                        boundaries[d] = localMaxima.dimension(d);
                /*
                 * start threads. every label gets own thread.
                 */
                while (!cellsQueue.isEmpty()) {
                        final FutureTask<Long> task = new FutureTask<Long>(
                                        new ClusterThread(
                                                        cellLabeling.getIterableRegionOfInterest(cellsQueue
                                                                        .poll()),
                                                        distanceMap,
                                                        localMaxima, res,
                                                        boundaries));
                        m_splittedQueue.add(task);
                        m_executor.execute(task);
                }
                /*
                 * wait until every tar is clustered
                 */
                long max = -Long.MAX_VALUE;
                long min = Long.MAX_VALUE;

                try {
                        while (!m_splittedQueue.isEmpty()) {
                                final long t = m_splittedQueue.poll().get();
                                min = Math.min(min, t);
                                max = Math.max(max, t);
                        }
                } catch (final InterruptedException e) {
                        throw new IllegalArgumentException(
                                        "Error during execution of the em algorithm: IllegalArgumentException");
                } catch (final ExecutionException e) {
                        throw new IllegalArgumentException(
                                        "Error during execution of the em algorithm: ExecutionException");
                } catch (final Exception e) {
                        throw new IllegalArgumentException(
                                        "Error during execution of the em algorithm.");
                }

                return res;
        }

        class ClusterThread implements Callable<Long> {
                /*
                 * num dimensions
                 */
                private final int t_numDim;

                /*
                 * predicted number of clusters
                 */
                private int t_predictionOfCluster;

                /*
                 * lambda for h-maxima transformation
                 */
                private int t_lambda = 0;

                /*
                 * max distance in distancemap of tar
                 */
                private float t_max = 0;

                /*
                 * seed points
                 */
                private ArrayList<long[]> t_seedPoints = new ArrayList<long[]>();

                /*
                 * center point of tar
                 */
                private final int[] t_centerPoint;

                /*
                 * data set of the clumped cell.
                 */
                private InstancesTmp t_dataSet;

                /*
                 * distanceMap image roi Cursor
                 */
                private final Cursor<FloatType> distanceMapRoiCursor;

                /*
                 * Random Access
                 */
                private final RandomAccess<FloatType> distanceMapRandomAccess;

                // /*
                // * local maxima image cursor
                // */
                // private final Cursor<BitType> localMaximaRoiCursor;

                /*
                 * distanceMap image access cursor
                 */
                private final RandomAccess<BitType> localMaximaAccess;

                /*
                 * random Access over res
                 */
                private final Labeling<Integer> res;

                /*
                 * random Access over res
                 */
                private final RandomAccess<LabelingType<Integer>> resRandomAccess;

                /*
                 * result of the em
                 */
                private ExtendedEM r_EM;

                /*
                 * result of validation
                 */
                private double r_validationMax = -Double.MAX_VALUE;

                /*
                 * founded clusters
                 */
                private int r_numOfCluster;

                /*
                 * boundaries of src/res image
                 */
                private final long[] boundaries;

                /*
                 * position and density instances (to get the probability
                 */
                private final double[] pos;

                private final InstanceTmp inst;

                private ArrayList<AttributeTmp> attr;

                /**
                 * Constructor
                 *
                 * @param roi
                 *                target
                 * @param distanceMap
                 *                distance map
                 * @param localMaxima
                 *                local maxima of distance map
                 * @param resAccess
                 *                random access of result image
                 * @param dimension
                 *                boundaries of source image
                 */
                public ClusterThread(final IterableRegionOfInterest roi,
                                final Img<FloatType> distanceMap,
                                final Img<BitType> localMaxima,
                                final Labeling<Integer> res,
                                final long[] dimension) {
                        t_numDim = distanceMap.numDimensions();
                        localMaximaAccess = localMaxima.randomAccess();
                        distanceMapRoiCursor = roi.getIterableIntervalOverROI(
                                        distanceMap).localizingCursor();
                        this.res = res;
                        resRandomAccess = res.randomAccess();
                        distanceMapRandomAccess = distanceMap.randomAccess();
                        t_centerPoint = new int[t_numDim];
                        boundaries = dimension;
                        pos = new double[t_numDim];
                        inst = new InstanceTmp(1, pos);
                        inst.setDataset(t_dataSet);
                }

                @Override
                public Long call() throws Exception {
                        final long start = System.nanoTime();
                        /*
                         * set predicted number of clusters. generates
                         * normalized data
                         */
                        preprocessData();
                        while (t_predictionOfCluster > 1) {
                                do {
                                        /*
                                         * evaluate h-Maxima transformation.
                                         * break loop only, if two centers fuse
                                         * together(decrease predicted number of
                                         * clusters)
                                         */
                                } while ((t_seedPoints = hMaximaTransformation(++t_lambda))
                                                .size() > t_predictionOfCluster);
                                if (t_seedPoints.size() == t_predictionOfCluster) {
                                        /*
                                         * create new seeding points for EM
                                         */
                                        final InstancesTmp seedPoints = prepareSeedingPointsForEM();
                                        /*
                                         * perform em
                                         */
                                        final ExtendedEM EM = performEMAlgo(seedPoints);
                                        /*
                                         * get new Cluster centers
                                         */
                                        final ArrayList<long[]> emClusterCenters = getNewClusterCenters(EM);
                                        /*
                                         * Cluster evaluation
                                         */
                                        final double sep = getSeperation(EM,
                                                        emClusterCenters);
                                        final double com = getCompactness(EM,
                                                        emClusterCenters);
                                        final double validation = (sep / com);
                                        /*
                                         * change the better result
                                         */
                                        if (validation > r_validationMax) {
                                                r_validationMax = validation;
                                                r_EM = EM;
                                                r_numOfCluster = t_predictionOfCluster;
                                        }
                                }
                                /*
                                 * decrease predicted number of clusters for
                                 * next iteration
                                 */
                                --t_predictionOfCluster;
                        }
                        /*
                         * set label for every cluster in res
                         */
                        synchronized (res) {
                                final Map<Integer, List<Integer>> listMap = new HashMap<Integer, List<Integer>>();

                                for (int c = 0; c < (r_numOfCluster == 0 ? 1
                                                : r_numOfCluster); ++c) {
                                        final List<Integer> labeling = new ArrayList<Integer>();
                                        labeling.add(++m_label);
                                        listMap.put(c, resRandomAccess.get()
                                                        .intern(labeling));
                                }
                                distanceMapRoiCursor.reset();
                                if (r_EM != null) {
                                        /*
                                         * result processing only if there is an
                                         * EM result
                                         */
                                        while (distanceMapRoiCursor.hasNext()) {
                                                distanceMapRoiCursor.fwd();
                                                resRandomAccess.setPosition(distanceMapRoiCursor);
                                                for (int d = 0; d < t_numDim; ++d)
                                                        pos[d] = resRandomAccess
                                                                        .getDoublePosition(d);
                                                final double[] p = r_EM
                                                                .distributionForInstance(inst);
                                                double pMax = Double.MIN_VALUE;
                                                int cMax = -1;
                                                for (int c = 0; c < p.length; ++c) {
                                                        if (pMax < p[c]) {
                                                                pMax = p[c];
                                                                cMax = c;
                                                        }
                                                }
                                                resRandomAccess.get()
                                                                .setLabeling(listMap
                                                                                .get(cMax));
                                        }
                                } else {
                                        /*
                                         * result processing for cells with m =
                                         * 1
                                         */
                                        while (distanceMapRoiCursor.hasNext()) {
                                                distanceMapRoiCursor.fwd();
                                                resRandomAccess.setPosition(distanceMapRoiCursor);
                                                resRandomAccess.get()
                                                                .setLabeling(listMap
                                                                                .get(0));
                                        }
                                }
                        }
                        return System.nanoTime() - start;
                }

                /**
                 * Performs EM Algorithm
                 *
                 * @param seedPoints
                 *                seed points
                 * @return result of EM Algorithm
                 * @throws Exception
                 */
                private ExtendedEM performEMAlgo(final InstancesTmp seedPoints)
                                throws Exception {
                        final ExtendedEM res = new ExtendedEM();
                        /*
                         * set cluster centers
                         */
                        res.setCenters(seedPoints);
                        /*
                         *
                         */
                        res.setMaxInterations(m_maxInterations);
                        /*
                         *
                         */
                        res.setClusterNominalCounts(new int[t_predictionOfCluster][2][0]);
                        /*
                         * build cluster via nearest Neighbour
                         */
                        res.setClusterSizes(clusterSizesByNearestNeighbor());
                        /*
                         * set number of clusters
                         */
                        res.setNumClusters(t_predictionOfCluster);
                        /*
                         * build clusterer
                         */
                        res.buildClusterer(t_dataSet);
                        /*
                         * get result center points
                         */
                        return res;
                }

                /**
                 * build cluster size via nearest Neighbor to seeding points
                 *
                 * @return
                 */
                private int[] clusterSizesByNearestNeighbor() {
                        final int[] res = new int[t_predictionOfCluster];
                        double distance;
                        int container = 0;
                        distanceMapRoiCursor.reset();
                        while (distanceMapRoiCursor.hasNext()) {
                                distanceMapRoiCursor.fwd();
                                distance = Double.MAX_VALUE;
                                for (int i = 0; i < res.length; ++i) {
                                        double distanceTemp = 0;
                                        for (int d = 0; d < t_numDim; ++d) {
                                                final double v = distanceMapRoiCursor
                                                                .getLongPosition(d)
                                                                - t_seedPoints.get(i)[d];
                                                distanceTemp += v * v;
                                        }
                                        distanceTemp = Math.sqrt(distanceTemp);
                                        distanceMapRandomAccess
                                                        .setPosition(t_seedPoints
                                                                        .get(i));
                                        if (distanceTemp < distance) {
                                                distance = distanceTemp;
                                                container = i;
                                        }
                                        res[container] += 1;
                                }
                        }
                        return res;
                }

                /**
                 * gets new cluster centers from a finished em
                 *
                 * @param extendedEM
                 *                result of EM
                 * @return cluster centers
                 */
                private ArrayList<long[]> getNewClusterCenters(
                                final ExtendedEM extendedEM) {
                        final ArrayList<long[]> emClusterCenters = new ArrayList<long[]>();
                        for (final double[][] a : extendedEM
                                        .getClusterModelsNumericAtts()) {
                                final long[] pos = new long[t_numDim];
                                for (int d = 0; d < t_numDim; ++d) {
                                        pos[d] = (long) a[d][0];
                                }
                                emClusterCenters.add(pos);
                        }
                        return emClusterCenters;
                }

                /**
                 * generates data. calculate min/max and normalize data
                 */
                private void preprocessData() {
                        /*
                         * find min & max for normalization
                         */
                        double min = 0;
                        int n = 0;
                        double avg = 0;
                        while (distanceMapRoiCursor.hasNext()) {
                                final float p = distanceMapRoiCursor.next()
                                                .getRealFloat();
                                if (p > 0) {
                                        t_max = Math.max(t_max, p);
                                        min = Math.min(min, p);
                                        ++n;
                                }
                                localMaximaAccess
                                                .setPosition(distanceMapRoiCursor);

                                // AWTImageTools.showInSameFrame(localMaxima,
                                // 1.0d);
                                if (localMaximaAccess.get().get()) {
                                        final long[] pos = new long[t_numDim];
                                        for (int d = 0; d < t_numDim; ++d)
                                                pos[d] = distanceMapRoiCursor
                                                                .getLongPosition(d);
                                        t_seedPoints.add(pos);
                                        avg += distanceMapRoiCursor.get()
                                                        .getRealDouble();
                                }
                        }
                        t_max -= min;
                        t_predictionOfCluster = t_seedPoints.size();
                        /*
                         * remove seeds under the average
                         */

                        // TODO: Parameter
                        avg = (avg / t_predictionOfCluster)
                                        * m_ignoreValueBelowAvgPrecent;
                        for (int i = 0; i < t_predictionOfCluster; ++i) {
                                distanceMapRandomAccess
                                                .setPosition(t_seedPoints
                                                                .get(i));
                                final double p = distanceMapRandomAccess.get()
                                                .getRealDouble();
                                if (p < avg || p < m_minMaximaSize) {
                                        t_seedPoints.remove(i);
                                        --t_predictionOfCluster;
                                        --i;
                                }
                        }

                        /*
                         * create EM data only if m > 1 (#2+ centroids were
                         * found)
                         */
                        if ((t_predictionOfCluster) > 1) {
                                /*
                                 * create cluster center points
                                 */
                                attr = new ArrayList<AttributeTmp>(t_numDim);
                                for (int d = 0; d < t_numDim; ++d)
                                        attr.add(new AttributeTmp("d" + d,
                                                        AttributeTmp.NUMERIC));

                                t_dataSet = new InstancesTmp(
                                                "CellClumpedSplitter", attr, n);
                                distanceMapRoiCursor.reset();

                                while (distanceMapRoiCursor.hasNext()) {
                                        final float v = distanceMapRoiCursor
                                                        .next().getRealFloat();
                                        final InstanceTmp row = new InstanceTmp(
                                                        t_numDim);
                                        for (int d = 0; d < t_numDim; ++d) {
                                                final double p = distanceMapRoiCursor
                                                                .getDoublePosition(d);
                                                t_centerPoint[d] += p;
                                                row.setValue(attr.get(d), p);
                                        }
                                        row.setWeight((v - min) / (t_max - min));
                                        t_dataSet.add(row);
                                }
                                for (int i = 0; i < t_centerPoint.length; ++i) {
                                        t_centerPoint[i] /= n;
                                }
                        }
                }

                /**
                 * after h maxima transformation calculate centroids. uses old
                 * seeding points for "caa" flooding points
                 *
                 * @param lambda
                 *                given depth
                 * @return predicted center points
                 */
                private ArrayList<long[]> hMaximaTransformation(final int lambda) {

                        /*
                         * H Maxima Transformation
                         */

                        Iterator<long[]> iter = t_seedPoints.iterator();
                        while (iter.hasNext()) {
                                localMaximaAccess.setPosition(iter.next());
                                localMaximaAccess.get().set(true);
                        }
                        final double tresh = t_max - (lambda);

                        distanceMapRoiCursor.reset();
                        while (distanceMapRoiCursor.hasNext()) {
                                distanceMapRoiCursor.fwd();

                                if (distanceMapRoiCursor.get().getRealDouble() > tresh) {
                                        localMaximaAccess
                                                        .setPosition(distanceMapRoiCursor);
                                        localMaximaAccess.get().set(true);
                                }
                        }
                        /*
                         * Calculate centroids. regionalMaximaCentroids as
                         * floodingpoints
                         */
                        final ArrayList<long[]> res = new ArrayList<long[]>();
                        /*
                         * old seeding points for "cca" flooding point
                         */
                        iter = t_seedPoints.iterator();
                        final Queue<long[]> q = new LinkedList<long[]>();
                        while (iter.hasNext()) {
                                int n = 0;
                                final long[] centroid = new long[t_numDim];
                                q.add(iter.next());
                                while (!q.isEmpty()) {
                                        final long[] p = q.poll();
                                        localMaximaAccess.setPosition(p);
                                        if (localMaximaAccess.get().get()) {
                                                ++n;
                                                final long[] perm = new long[t_numDim];
                                                for (int d = 0; d < t_numDim; ++d) {
                                                        centroid[d] += p[d];
                                                        perm[d] = -1;
                                                }
                                                localMaximaAccess.get().set(
                                                                false);
                                                /*
                                                 * add neighbours to queue
                                                 */
                                                long[] nextPos;
                                                int i = t_numDim - 1;
                                                boolean add;
                                                while (i > -1) {
                                                        nextPos = p.clone();
                                                        add = true;
                                                        /*
                                                         * Modify position
                                                         */
                                                        for (int j = 0; j < t_numDim; j++) {
                                                                nextPos[j] += perm[j];
                                                                /*
                                                                 * Check
                                                                 * boundaries
                                                                 */
                                                                if (nextPos[j] < 0
                                                                                || nextPos[j] >= boundaries[j]) {
                                                                        add = false;
                                                                        break;
                                                                }
                                                        }
                                                        if (add) {
                                                                q.add(nextPos);
                                                        }
                                                        /*
                                                         * Calculate next
                                                         * permutation
                                                         */
                                                        for (i = perm.length - 1; i > -1; i--) {
                                                                if (perm[i] < 1) {
                                                                        perm[i]++;
                                                                        for (int j = i + 1; j < perm.length; j++) {
                                                                                perm[j] = -1;
                                                                        }
                                                                        break;
                                                                }
                                                        }
                                                }
                                        }
                                }
                                if (n > 0) {
                                        for (int d = 0; d < t_numDim; ++d) {
                                                centroid[d] /= n;
                                        }
                                        res.add(centroid);
                                }
                        }
                        return res;
                }

                /**
                 * construct new seeding points
                 *
                 * @return Instance of seeding points
                 */
                private InstancesTmp prepareSeedingPointsForEM() {
                        final InstancesTmp res = new InstancesTmp("centers",
                                        attr, t_seedPoints.size());
                        for (final long[] t : t_seedPoints) {
                                final InstanceTmp row = new InstanceTmp(
                                                t_numDim);
                                for (int d = 0; d < t_numDim; ++d)
                                        row.setValue(attr.get(d), (int) t[d]);
                                res.add(row);
                        }
                        return res;
                }

                /**
                 * calculate the separation of given clustering
                 *
                 * @param extendedEM
                 *                result of finished EM
                 * @param emClusterCenters
                 *                new calculated cluster centers
                 * @return separation of given clustering
                 * @throws Exception
                 */
                private double getSeperation(final ExtendedEM extendedEM,
                                final ArrayList<long[]> emClusterCenters)
                                throws Exception {
                        double res = 0;
                        for (int i = 0; i < t_predictionOfCluster; ++i) {
                                distanceMapRoiCursor.reset();
                                double sum = 0;
                                for (int d = 0; d < t_numDim; ++d) {
                                        final double mioverallm = (emClusterCenters
                                                        .get(i)[d] - t_centerPoint[d]);
                                        sum += mioverallm * mioverallm;
                                }
                                while (distanceMapRoiCursor.hasNext()) {
                                        distanceMapRoiCursor.fwd();
                                        for (int d = 0; d < t_numDim; ++d)
                                                pos[d] = distanceMapRoiCursor
                                                                .getDoublePosition(d);
                                        res += extendedEM
                                                        .distributionForInstance(inst)[i]
                                                        * sum;
                                }
                        }
                        return res;
                }

                /**
                 * calculate the compactness of given clustering
                 *
                 * @param extendedEM
                 *                result of finished EM
                 * @param emClusterCenters
                 *                new calculated cluster centers
                 * @return compactness of given clustering
                 * @throws Exception
                 */
                private double getCompactness(final ExtendedEM extendedEM,
                                final ArrayList<long[]> emClusterCenters)
                                throws Exception {
                        double res = 0;
                        for (int i = 0; i < t_predictionOfCluster; ++i) {
                                double pkixkmiSum = 0;// numerator
                                double pkiSum = 0;// denominator
                                distanceMapRoiCursor.reset();
                                while (distanceMapRoiCursor.hasNext()) {
                                        distanceMapRoiCursor.fwd();
                                        double sum = 0;
                                        for (int d = 0; d < t_numDim; ++d) {
                                                final double xkmi = (distanceMapRoiCursor
                                                                .getDoublePosition(d) - emClusterCenters
                                                                .get(i)[d]);
                                                sum += xkmi * xkmi;
                                        }
                                        for (int d = 0; d < t_numDim; ++d)
                                                pos[d] = distanceMapRoiCursor
                                                                .getDoublePosition(d);
                                        final double pki = extendedEM
                                                        .distributionForInstance(inst)[i];
                                        pkixkmiSum += pki * sum;
                                        pkiSum += pki;
                                }
                                res += pkixkmiSum / pkiSum;
                        }
                        return res;
                }
        }

        @Override
        public UnaryOperation<Labeling<L>, Labeling<Integer>> copy() {
                return new CellClumpedSplitter<L>(m_neighborhood, m_executor,
                                m_minMaximaSize, m_ignoreValueBelowAvgPrecent,
                                m_maxInterations);
        }
}
