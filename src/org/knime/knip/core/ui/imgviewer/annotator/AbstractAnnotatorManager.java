package org.knime.knip.core.ui.imgviewer.annotator;

import java.util.ArrayList;
import java.util.Map;

import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorImgAndOverlayChgEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelEditEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsColResetEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsDelEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsSelChgEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorLabelsSetEvent;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorToolChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseDraggedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMousePressedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseReleasedEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.OverlayChgEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;
import org.knime.knip.core.ui.imgviewer.overlay.OverlayElement2D;
import org.knime.knip.core.ui.imgviewer.panels.HiddenViewerComponent;

/**
 * Manages overlays and overlay elements ...
 *
 * @author Christian
 *
 */
public abstract class AbstractAnnotatorManager<T extends RealType<T>> extends HiddenViewerComponent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    protected String[] m_selectedLabels;

    protected PlaneSelectionEvent m_sel;

    /* Are not serialized or calculated from serzalization values */
    protected EventService m_eventService;

    protected Overlay<String> m_currentOverlay;

    protected AnnotatorTool<?> m_currentTool;

    public AbstractAnnotatorManager() {
        m_selectedLabels = new String[]{"Unknown"};
    }

    protected abstract Map<String, Overlay<String>> getOverlayMap();

    @Override
    public void setEventService(final EventService eventService) {
        m_eventService = eventService;
        eventService.subscribe(this);
    }

    @EventListener
    public void onLabelsColorReset(final AnnotatorLabelsColResetEvent e) {
        for (final String label : e.getLabels()) {
            SegmentColorTable.resetColor(label);
        }

        m_eventService.publish(new OverlayChgEvent(m_currentOverlay));
    }

    @EventListener
    public void onSetClassLabels(final AnnotatorLabelsSetEvent e) {
        if (m_currentTool != null) {
            m_currentTool.setLabelsCurrentElements(m_currentOverlay, e.getLabels());
        }
    }

    @EventListener
    public void onSelectedLabelsChg(final AnnotatorLabelsSelChgEvent e) {
        m_selectedLabels = e.getLabels();
    }

    @EventListener
    public void onToolChange(final AnnotatorToolChgEvent e) {
        if (m_currentTool != null) {
            m_currentTool.fireFocusLost(m_currentOverlay);
        }

        m_currentTool = e.getTool();

    }

    @EventListener
    public void onLabelsDeleted(final AnnotatorLabelsDelEvent e) {
        ArrayList<OverlayElement2D<String>> m_removeList = new ArrayList<OverlayElement2D<String>>();

        for (final Overlay<String> overlay : getOverlayMap().values()) {
            for (final OverlayElement2D<String> element : overlay.getElements()) {
                for (final String label : e.getLabels()) {
                    element.getLabels().remove(label);
                }

                if (element.getLabels().size() == 0) {
                    m_removeList.add(element);
                }
            }

            overlay.removeAll(m_removeList);
            m_removeList.clear();
        }
        if (m_currentOverlay != null) {
            m_currentOverlay.fireOverlayChanged();
        }
    }

    /**
     * @param axes
     */
    @EventListener
    public void onUpdate(final IntervalWithMetadataChgEvent<T> e) {

        m_currentOverlay = getOverlayMap().get(e.getSource().getSource());

        if (m_currentOverlay == null) {
            m_currentOverlay = new Overlay<String>(e.getRandomAccessibleInterval());
            getOverlayMap().put(e.getSource().getSource(), m_currentOverlay);
            m_currentOverlay.setEventService(m_eventService);
        }

        final long[] dims = new long[e.getRandomAccessibleInterval().numDimensions()];
        e.getRandomAccessibleInterval().dimensions(dims);

        if ((m_sel == null) || !isInsideDims(m_sel.getPlanePos(), dims)) {
            m_sel = new PlaneSelectionEvent(0, 1, new long[e.getRandomAccessibleInterval().numDimensions()]);
        }

        m_eventService.publish(new AnnotatorImgAndOverlayChgEvent(e.getRandomAccessibleInterval(), m_currentOverlay));

        m_eventService.publish(new ImgRedrawEvent());
    }

    private boolean isInsideDims(final long[] planePos, final long[] dims) {
        if (planePos.length != dims.length) {
            return false;
        }

        for (int d = 0; d < planePos.length; d++) {
            if (planePos[d] >= dims[d]) {
                return false;
            }
        }

        return true;
    }

    @EventListener
    public void onUpdate(final PlaneSelectionEvent sel) {
        m_sel = sel;
    }

    @EventListener
    public void onLabelEdit(final AnnotatorLabelEditEvent e) {
        for (final Overlay<String> overlay : getOverlayMap().values()) {
            for (final OverlayElement2D<String> element : overlay.getElements()) {
                if (element.getLabels().remove(e.getOldLabel())) {
                    element.getLabels().add(e.getNewLabel());
                }
            }
        }
        onSelectedLabelsChg(new AnnotatorLabelsSelChgEvent(e.getNewLabel()));

        SegmentColorTable.setColor(e.getNewLabel(), SegmentColorTable.getColor(e.getOldLabel()));
    }

    /*
     * Handling mouse events
     */

    @EventListener
    public void onMousePressed(final ImgViewerMousePressedEvent e) {

        if ((m_currentOverlay != null) && (m_currentTool != null)) {
            m_currentTool.onMousePressed(e, m_sel, m_currentOverlay, m_selectedLabels);
        }
    }

    @EventListener
    public void onMouseDragged(final ImgViewerMouseDraggedEvent e) {

        if ((m_currentOverlay != null) && (m_currentTool != null)) {
            m_currentTool.onMouseDragged(e, m_sel, m_currentOverlay, m_selectedLabels);
        }
    }

    @EventListener
    public void onMouseReleased(final ImgViewerMouseReleasedEvent e) {
        if ((m_currentOverlay != null) && (m_currentTool != null)) {
            if (e.getClickCount() > 1) {
                m_currentTool.onMouseDoubleClick(e, m_sel, m_currentOverlay, m_selectedLabels);
            } else {
                m_currentTool.onMouseReleased(e, m_sel, m_currentOverlay, m_selectedLabels);
            }

        }
    }


    /**
     * {@inheritDoc}
     */
    @EventListener
    public void reset(final AnnotatorResetEvent e) {
        m_currentOverlay = null;
        m_selectedLabels = new String[]{"Unknown"};
    }

}
