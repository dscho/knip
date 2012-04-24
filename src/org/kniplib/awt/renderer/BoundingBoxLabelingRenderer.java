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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.labeling.Labeling;
import net.imglib2.roi.IterableRegionOfInterest;

import org.kniplib.data.img.SubLabeling;
import org.kniplib.ui.imgviewer.events.RulebasedLabelFilter.Operator;

/**
 *
 * @author dietzc, hornm, schoenenbergerf University of Konstanz
 * @param <L>
 */
public class BoundingBoxLabelingRenderer<L extends Comparable<L>> extends
                LabelingRenderer<L> {

        private final LabelingRenderer<L> m_labelingRenderer;

        private Set<String> m_hilitedLabels;

        /**
	 *
	 */
        public BoundingBoxLabelingRenderer() {
                m_labelingRenderer = null;
        }

        /**
         * @param labelingRenderer
         */
        public BoundingBoxLabelingRenderer(LabelingRenderer<L> labelingRenderer) {
                m_labelingRenderer = labelingRenderer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public BufferedImage render(Labeling<L> lab, int dimX, int dimY,
                        long[] pos, double scale, Set<String> activeLabels,
                        Operator op) {
                Labeling<L> subLab = lab;
                if (lab.numDimensions() > 2) {
                        long[] min = pos.clone();
                        long[] max = pos.clone();

                        min[dimX] = 0;
                        min[dimY] = 0;

                        max[dimX] = lab.max(dimX);
                        max[dimY] = lab.max(dimY);

                        subLab = new SubLabeling<L>(lab, new FinalInterval(min,
                                        max));
                }

                long[] dims = new long[lab.numDimensions()];
                lab.dimensions(dims);
                int width = (int) Math.round(dims[dimX] * scale) + 1;
                int height = (int) Math.round(dims[dimY] * scale) + 1;

                Graphics g;
                BufferedImage res;
                if (m_labelingRenderer == null) {
                        res = new BufferedImage(width, height,
                                        BufferedImage.TYPE_BYTE_GRAY);
                        g = res.getGraphics();
                        g.setColor(Color.WHITE);
                        g.fillRect(0, 0, width, height);

                } else {
                        res = m_labelingRenderer.render(lab, dimX, dimY, pos,
                                        scale, activeLabels, op);
                        g = res.getGraphics();
                }
                g.setColor(Color.black);

                Collection<L> labels = subLab.getLabels();
                List<L> singleLabelList = new ArrayList<L>(1);
                singleLabelList.add(null);

                Color hilite = new Color(SegmentColorTable.HILITED_RGB);

                for (L label : labels) {

                        if (m_hilitedLabels != null
                                        && m_hilitedLabels.contains(label)) {
                                g.setColor(hilite);
                        } else {
                                g.setColor(Color.yellow);

                        }

                        singleLabelList.set(0, label);
                        if (activeLabels != null
                                        && intersection(activeLabels,
                                                        singleLabelList) == 0) {
                                continue;
                        }

                        IterableRegionOfInterest roi = lab
                                        .getIterableRegionOfInterest(label);
                        Interval ii = roi.getIterableIntervalOverROI(lab);
                        g.drawRect((int) (ii.min(dimX) * scale),
                                        (int) (ii.min(dimY) * scale),
                                        (int) ((ii.dimension(dimX) - 1) * scale),
                                        (int) ((ii.dimension(dimY) - 1) * scale));

                        if (scale > .6) {

                                g.drawString(label.toString(),
                                                (int) ((ii.min(dimX) + 1) * scale),
                                                (int) ((ii.min(dimY) + 10) * scale));
                        }
                }

                return res;
        }

        private int intersection(Set<String> activeLabels,
                        List<L> singleLabelList) {
                int intersect = 0;
                for (L l : singleLabelList) {
                        if (activeLabels.contains(l))
                                intersect++;
                }

                return intersect;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
                return "Bounding Box"
                                + (m_labelingRenderer == null ? " "
                                                : " Color Labeling ")
                                + "Renderer";
        }

        @Override
        public void setHilitedLabels(Set<String> hilitedLabels) {
                m_hilitedLabels = hilitedLabels;
        }

        @Override
        public void setHiliteMode(boolean isHiliteMode) {
                // TODO: Nothing going on here
        }
}
