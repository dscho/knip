package org.knime.knip.core.awt;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Set;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ScreenImage;
import net.imglib2.img.subset.LabelingView;
import net.imglib2.img.subset.SubsetViews;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.LabelingType;
import net.imglib2.roi.IterableRegionOfInterest;
import net.imglib2.type.Type;

import org.knime.knip.core.awt.parametersupport.RendererWithHilite;
import org.knime.knip.core.awt.parametersupport.RendererWithLabels;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter.Operator;

public class BoundingBoxLabelRenderer<L extends Comparable<L> & Type<L>>
                implements ImageRenderer<LabelingType<L>, Labeling<L>>,
                RendererWithLabels<L>, RendererWithHilite {

        private final Color HILITED_RGB_COLOR = new Color(
                        SegmentColorTable.HILITED_RGB);

        private Color getBOX_RGB_COLOR() {
                return SegmentColorTable.getBoundingBoxColor();
        };

        private Set<String> m_hilitedLabels;
        private Set<String> m_activeLabels;

        protected double m_scale = 1.0;

        @Override
        public ScreenImage render(Labeling<L> source, int dimX, int dimY,
                        long[] planePos) {
                return render(dimX, dimY, planePos, source, m_activeLabels,
                                m_scale);
        }

        private ScreenImage render(int dimX, int dimY, long[] planePos,
                        Labeling<L> source, Set<String> activeLabels,
                        double scale) {

                Labeling<L> subLab = source;

                if (source.numDimensions() > 2) {
                        long[] min = planePos.clone();
                        long[] max = planePos.clone();

                        min[dimX] = 0;
                        min[dimY] = 0;

                        max[dimX] = source.max(dimX);
                        max[dimY] = source.max(dimY);

                        subLab = new LabelingView<L>(
                                        SubsetViews.iterableSubsetView(source,
                                                        new FinalInterval(min,
                                                                        max)),
                                        source.<L> factory());
                }

                long[] dims = new long[source.numDimensions()];
                source.dimensions(dims);
                int width = (int) Math.round(dims[dimX] * scale) + 1;
                int height = (int) Math.round(dims[dimY] * scale) + 1;

                ScreenImage res = createCanvas(width, height);
                Graphics g = res.image().getGraphics();
                g.setColor(Color.black);

                for (L label : subLab.getLabels()) {

                        // test hilite
                        if (m_hilitedLabels != null
                                        && m_hilitedLabels.contains(label)) {
                                g.setColor(HILITED_RGB_COLOR);
                        } else {
                                g.setColor(getBOX_RGB_COLOR());
                        }

                        // test active labels (null = all active)
                        if (activeLabels == null
                                        || activeLabels.contains(label)) {

                                IterableRegionOfInterest roi = subLab
                                                .getIterableRegionOfInterest(label);
                                Interval ii = roi
                                                .getIterableIntervalOverROI(source);
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
                }

                return res;
        }

        protected ScreenImage createCanvas(int width, int height) {
                ScreenImage ret = new ARGBScreenImage(width, height);
                Graphics g = ret.image().getGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);

                return ret;
        }

        @Override
        public String toString() {
                return "Bounding Box Renderer";
        }

        public void setScale(double scale) {
                m_scale = scale;
        }

        @Override
        public void setHilitedLabels(Set<String> hilitedLabels) {
                m_hilitedLabels = hilitedLabels;
        }

        @Override
        public void setActiveLabels(Set<String> activeLabels) {
                m_activeLabels = activeLabels;
        }

        @Override
        public void setHiliteMode(boolean isHiliteMode) {
                // TODO: Nothing going on here
        }

        @Override
        public void setLabelMapping(LabelingMapping<L> labelMapping) {
                // do nothing
        }

        @Override
        public void setOperator(Operator operator) {
                // do nothing
        }
}
