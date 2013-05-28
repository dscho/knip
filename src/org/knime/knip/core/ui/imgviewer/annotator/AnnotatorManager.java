package org.knime.knip.core.ui.imgviewer.annotator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorFilelistChgEvent;
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
public class AnnotatorManager<T extends RealType<T>> extends HiddenViewerComponent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /* Are serialized */
    private Map<String, Overlay<String>> m_overlayMap;

    private String[] m_selectedLabels;

    private PlaneSelectionEvent m_sel;

    /* Are not serialized or calculated from serzalization values */
    private EventService m_eventService;

    private List<OverlayElement2D<String>> m_removeList;

    private Overlay<String> m_currentOverlay;

    private AnnotatorTool<?> m_currentTool;

    public AnnotatorManager() {
        setOverlayMap(new HashMap<String, Overlay<String>>());
        m_removeList = new ArrayList<OverlayElement2D<String>>();
        m_selectedLabels = new String[]{"Unknown"};
    }

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
    public void onFileListChange(final AnnotatorFilelistChgEvent e) {

        for (final String key : new HashSet<String>(m_overlayMap.keySet())) {
            // Switching systems
            boolean contains = false;
            for (final String file : e.getFileList()) {
                // Exactly same path
                if (key.equals(file)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                m_overlayMap.remove(key);
            }

        }

    }

    @EventListener
    public void onLabelsDeleted(final AnnotatorLabelsDelEvent e) {
        for (final Overlay<String> overlay : m_overlayMap.values()) {
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
        for (final Overlay<String> overlay : m_overlayMap.values()) {
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

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        out.writeInt(getOverlayMap().size());

        for (final Entry<String, Overlay<String>> entry : getOverlayMap().entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().writeExternal(out);
        }
        out.writeInt(m_selectedLabels.length);

        for (final String s : m_selectedLabels) {
            out.writeUTF(s);
        }

    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {

        getOverlayMap().clear();
        final int num = in.readInt();
        for (int i = 0; i < num; i++) {
            final String key = in.readUTF();
            final Overlay<String> o = new Overlay<String>();
            o.readExternal(in);
            o.setEventService(m_eventService);
            getOverlayMap().put(key, o);
        }

        m_selectedLabels = new String[in.readInt()];
        for (int i = 0; i < m_selectedLabels.length; i++) {
            m_selectedLabels[i] = in.readUTF();
        }

    }

    public Map<String, Overlay<String>> getOverlayMap() {
        return m_overlayMap;
    }

    public void setOverlayMap(final Map<String, Overlay<String>> m_overlayMap) {
        this.m_overlayMap = m_overlayMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        m_currentOverlay = null;
        m_overlayMap = new HashMap<String, Overlay<String>>();
        m_removeList = new ArrayList<OverlayElement2D<String>>();
        m_selectedLabels = new String[]{"Unknown"};
    }

}
