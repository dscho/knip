package org.knime.knip.core.ui.imgviewer.panels.providers;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import net.imglib2.display.ScreenImage;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.LabelingType;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.awt.parametersupport.RendererWithLabels;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.events.AWTImageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelColoringChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelOptionsChangeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelPanelVisibleLabelsChgEvent;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter.Operator;

/**
 * Creates an awt image from a plane selection, labeling and labeling renderer. Propagates {@link AWTImageChgEvent}.
 * 
 * @author hornm, dietzc University of Konstanz
 */
public class LabelingBufferedImageProvider<L extends Comparable<L>> extends AWTImageProvider<LabelingType<L>> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private int m_colorMapNr = SegmentColorTable.getColorMapNr();

    private Color m_boundingBoxColor = SegmentColorTable.getBoundingBoxColor();

    protected Set<String> m_activeLabels;

    protected Operator m_operator;

    protected LabelingMapping<L> m_labelMapping;

    protected boolean m_withLabelStrings = false;

    public LabelingBufferedImageProvider(final int cacheSize) {
        super(cacheSize);
    }

    @Override
    @EventListener
    public void onUpdated(final IntervalWithMetadataChgEvent<LabelingType<L>> e) {
        m_labelMapping = e.getIterableInterval().firstElement().getMapping();
        super.onUpdated(e);
    }

    @EventListener
    public void onLabelColoringChangeEvent(final LabelColoringChangeEvent e) {
        m_colorMapNr = e.getColorMapNr();
        m_boundingBoxColor = e.getBoundingBoxColor();
    }

    @EventListener
    public void onLabelOptionsChangeEvent(final LabelOptionsChangeEvent e) {
        m_withLabelStrings = e.getRenderWithLabelStrings();
    }

    @Override
    protected int generateHashCode() {

        int hash = super.generateHashCode();

        if (m_activeLabels != null) {
            hash *= 31;
            hash += m_activeLabels.hashCode();
            hash *= 31;
            hash += m_operator.ordinal();
        }

        hash *= 31;
        hash += m_boundingBoxColor.hashCode();
        hash *= 31;
        hash += m_colorMapNr;
        hash *= 31;
        if (m_withLabelStrings) {
            hash += 1;
        } else {
            hash += 2;
        }

        return hash;
    }

    @Override
    protected Image createImage() {
        if (m_renderer instanceof RendererWithLabels) {
            final RendererWithLabels<L> r = (RendererWithLabels<L>)m_renderer;
            r.setActiveLabels(m_activeLabels);
            r.setOperator(m_operator);
            r.setLabelMapping(m_labelMapping);
            r.setRenderingWithLabelStrings(m_withLabelStrings);
        }

        final ScreenImage ret =
                m_renderer.render(m_src, m_sel.getPlaneDimIndex1(), m_sel.getPlaneDimIndex2(), m_sel.getPlanePos());

        return loci.formats.gui.AWTImageTools.makeBuffered(ret.image());
    }

    @EventListener
    public void onUpdate(final LabelPanelVisibleLabelsChgEvent e) {
        m_activeLabels = e.getLabels();
        m_operator = e.getOperator();
    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        super.saveComponentConfiguration(out);
        out.writeObject(m_activeLabels);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
        super.loadComponentConfiguration(in);
        m_activeLabels = (Set<String>)in.readObject();
    }
}
