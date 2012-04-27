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
 *   6 May 2011 (hornm): created
 */
package org.kniplib.awt.renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.imglib2.RandomAccess;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.LabelingType;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.labeling.NativeLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.IntervalIndexer;

import org.kniplib.awt.AWTImageTools;
import org.kniplib.io.serialization.PlanarImgContainerSamplerImpl;
import org.kniplib.ui.imgviewer.events.RulebasedLabelFilter.Operator;

/**
 *
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 * @param <L>
 */
public class RandomColorLabelingRenderer<L extends Comparable<L>> extends
                LabelingRenderer<L> {

        private static int WHITE_RGB = Color.WHITE.getRGB();

        private Set<String> m_hilitedLabels;

        private boolean m_isHiliteMode = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public BufferedImage render(Labeling<L> lab, int dimX, int dimY,
                        long[] pos, double scale, Set<String> activeLabels,
                        Operator op) {

                long[] newDim = new long[2];
                int[] slice;

                // Special !fast! rendering for the standard case
                if (scale == 1
                                && dimX == 0
                                && dimY == 1
                                && (lab instanceof NativeImgLabeling
                                                && (((NativeImgLabeling) lab)
                                                                .getStorageImg()
                                                                .factory() instanceof PlanarImgFactory || ((NativeImgLabeling) lab)
                                                                .getStorageImg()
                                                                .factory() instanceof ArrayImgFactory) && !(((NativeImgLabeling) lab)
                                                .getStorageImg().firstElement() instanceof BitType))) {
                        slice = extractSliceDirect((NativeLabeling<L>) lab,
                                        pos, activeLabels, op);
                        newDim[0] = lab.dimension(0);
                        newDim[1] = lab.dimension(1);
                } else {
                        slice = extractSliceScaleX(lab, dimX, dimY, pos, scale,
                                        newDim, activeLabels, op);
                }

                DataBuffer buffer = new DataBufferInt(slice, slice.length);
                return AWTImageTools.constructImage(3, DataBuffer.TYPE_INT,
                                (int) newDim[0], (int) newDim[1], true, false,
                                buffer);

        }

        private int[] extractSliceDirect(NativeLabeling<L> lab, long[] pos,
                        Set<String> activeLabels, Operator op) {

                int offset = 0;
                long[] dims = new long[lab.numDimensions()];
                lab.dimensions(dims);

                LabelingMapping<L> mapping = lab.getMapping();

                int[] slice = new int[(int) dims[0] * (int) dims[1]];

                Object access = extractLabelingStorage(dims, lab, pos);
                if (access instanceof byte[]) {

                        byte[] concrete = (byte[]) access;
                        if (concrete.length > slice.length)
                                offset = (int) IntervalIndexer.positionToIndex(
                                                pos, dims);

                        for (int i = 0; i < slice.length; i++) {

                                slice[i] = getColorForLabeling(
                                                activeLabels,
                                                op,
                                                mapping.listAtIndex(concrete[offset
                                                                + i]));
                        }
                } else if (access instanceof short[]) {

                        short[] arrayType = (short[]) access;
                        if (arrayType.length > slice.length)
                                offset = (int) IntervalIndexer.positionToIndex(
                                                pos, dims);

                        for (int i = 0; i < slice.length; i++) {
                                slice[i] = getColorForLabeling(
                                                activeLabels,
                                                op,
                                                mapping.listAtIndex(arrayType[offset
                                                                + i]));
                        }
                } else if (access instanceof int[]) {

                        int[] concrete = (int[]) access;
                        if (concrete.length > slice.length)
                                offset = (int) IntervalIndexer.positionToIndex(
                                                pos, dims);

                        for (int i = 0; i < slice.length; i++) {
                                slice[i] = getColorForLabeling(
                                                activeLabels,
                                                op,
                                                mapping.listAtIndex(concrete[offset
                                                                + i]));
                        }
                }

                return slice;
        }

        private static synchronized Object extractLabelingStorage(long[] dims,
                        Labeling<?> lab, long[] pos) {

                long[] reducedDims = new long[lab.numDimensions() - 2];
                long[] reducedPos = new long[lab.numDimensions() - 2];

                for (int d = 2; d < reducedDims.length; d++) {
                        reducedDims[d] = dims[d];
                        reducedPos[d] = pos[d];
                }

                if (lab instanceof NativeImgLabeling
                                && (((NativeImgLabeling) lab).getStorageImg()
                                                .factory() instanceof ArrayImgFactory)) {

                        return ((ArrayDataAccess) ((ArrayImg) ((NativeImgLabeling) lab)
                                        .getStorageImg()).update(null))
                                        .getCurrentStorageArray();

                } else if (lab instanceof NativeImgLabeling
                                && ((((NativeImgLabeling) lab)).getStorageImg()
                                                .factory() instanceof PlanarImgFactory)) {

                        PlanarImgContainerSamplerImpl sampler = new PlanarImgContainerSamplerImpl();

                        sampler.setCurrentSlice((int) (IntervalIndexer
                                        .positionToIndex(pos, dims) / (dims[0] * dims[1])));

                        return (((PlanarImg) ((NativeImgLabeling) lab)
                                        .getStorageImg()).update(sampler))
                                        .getCurrentStorageArray();

                }

                throw new RuntimeException("Labeling can't be rendered");

        }

        /*
         * Extracts a plane as an int array.
         */
        private final int[] extractSliceScaleX(final Labeling<L> lab,
                        final int dimX, final int dimY,
                        final long[] dimensionPositions, double scale,
                        long[] newDim, Set<String> activeLabels, Operator op) {

                newDim[0] = Math.round(lab.dimension(dimX) * scale) + 1;
                newDim[1] = Math.round(lab.dimension(dimY) * scale) + 1;

                // store the slice image
                int[] slice = new int[(int) (newDim[0] * newDim[1])];

                RandomAccess<LabelingType<L>> ra = lab.randomAccess();
                ra.setPosition(dimensionPositions);
                // TODO: cursor wenn scale = 1
                int i = 0;
                while (i < slice.length) {

                        slice[i++] = getColorForLabeling(activeLabels, op, ra
                                        .get().getLabeling());

                        if (i % newDim[0] != 0) {
                                ra.setPosition((long) Math.min(lab.max(dimX), i
                                                % newDim[0] / scale), dimX);
                        } else {
                                ra.setPosition(0, dimX);
                                ra.setPosition((long) Math.min(lab.max(dimY), i
                                                / newDim[0] / scale), dimY);
                        }
                }

                return slice;
        }

        private final List<L> m_filteredLabels = new ArrayList<L>(4);

        private int getColorForLabeling(Set<String> activeLabels, Operator op,
                        List<L> labeling) {
                if (labeling.size() == 0)
                        return WHITE_RGB;

                if (activeLabels == null && m_hilitedLabels == null)
                        return SegmentColorTable.getColor(labeling);

                if (activeLabels != null) {
                        m_filteredLabels.clear();
                        m_filteredLabels.addAll(intersection(activeLabels, op,
                                        labeling));
                        if (m_filteredLabels.size() == 0) {
                                return WHITE_RGB;
                        } else if (checkHilite(m_filteredLabels,
                                        m_hilitedLabels)) {
                                return SegmentColorTable.HILITED_RGB;
                        } else {
                                return m_isHiliteMode ? SegmentColorTable.NOTSELECTED_RGB
                                                : SegmentColorTable
                                                                .getColor(m_filteredLabels);
                        }

                }

                if (m_hilitedLabels != null) {
                        if (checkHilite(labeling, m_hilitedLabels)) {
                                return SegmentColorTable.HILITED_RGB;
                        } else {
                                return m_isHiliteMode ? SegmentColorTable.NOTSELECTED_RGB
                                                : SegmentColorTable
                                                                .getColor(labeling);
                        }
                }

                throw new IllegalStateException("Shouldn't be the case");
        }

        private boolean checkHilite(List<L> labeling, Set<String> hilited) {
                if (hilited != null && hilited.size() > 0) {
                        for (int i = 0; i < labeling.size(); i++) {
                                if (hilited.contains(labeling.get(i))) {
                                        return true;
                                }
                        }
                }
                return false;
        }

        private final Set<L> m_intersected = new HashSet<L>();

        private Collection<? extends L> intersection(Set<String> activeLabels,
                        Operator op, List<L> labeling) {
                m_intersected.clear();
                for (int i = 0; i < labeling.size(); i++) {
                        if (activeLabels.contains(labeling.get(i).toString())) {
                                m_intersected.add(labeling.get(i));
                        }
                }

                switch (op) {
                case OR:
                        break;
                case AND:
                        if (!labeling.containsAll(activeLabels))
                                m_intersected.clear();
                        break;
                case XOR:
                        if (m_intersected.size() > 1) {
                                m_intersected.clear();
                        }
                        break;
                }

                return m_intersected;
        }

        // private final BufferedImage constructImage(int[] buf,
        // final LabelingMapping<L, Integer> map, long width, long height,
        // int offset, double min, double factor, boolean signed) {
        //
        // int[] slice = new int[(int) (width * height)];
        // System.arraycopy(buf, offset, slice, 0, slice.length);
        //
        // int c = 3;
        // int[] bitMasks = new int[c];
        // for (int i = 0; i < c; i++) {
        // bitMasks[i] = 0xff << ((c - i - 1) * 8);
        // }
        // SampleModel model = new SinglePixelPackedSampleModel(
        // DataBuffer.TYPE_INT, (int) width, (int) height, bitMasks);
        //
        // DataBuffer buffer = new DataBufferInt(slice, slice.length);
        //
        // WritableRaster raster = Raster
        // .createWritableRaster(model, buffer, null);
        //
        // ColorModel cm = new ColorModel(24) {
        //
        // public int getRed(int pixel) {
        // if (pixel != 0) {
        // return SegmentColorTable.getColor(map.listAtIndex(pixel));
        // } else
        // return 0;
        // }
        //
        // public int getGreen(int pixel) {
        // if (pixel != 0) {
        // return SegmentColorTable.getColor(map.listAtIndex(pixel));
        // } else
        // return 0;
        // }
        //
        // public int getBlue(int pixel) {
        // if (pixel != 0) {
        // return SegmentColorTable.getColor(map.listAtIndex(pixel));
        // } else
        // return 0;
        // }
        //
        // public int getAlpha(int pixel) {
        // return 0;
        // }
        // };
        //
        // BufferedImage b = new BufferedImage(cm, raster, false, null);
        //
        // // GraphicsEnvironment env = GraphicsEnvironment
        // // .getLocalGraphicsEnvironment();
        // // GraphicsDevice device = env.getDefaultScreenDevice();
        // // GraphicsConfiguration config = device.getDefaultConfiguration();
        // // b = config.createCompatibleImage(w, h);
        // // b.setData(raster);
        //
        // }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
                return "Random Color Labeling Renderer";
        }

        @Override
        public void setHilitedLabels(Set<String> hilitedLabels) {
                m_hilitedLabels = hilitedLabels;
        }

        @Override
        public void setHiliteMode(boolean isHiliteMode) {
                m_isHiliteMode = isHiliteMode;
        }

}
