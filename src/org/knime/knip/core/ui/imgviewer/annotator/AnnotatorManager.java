package org.knime.knip.core.ui.imgviewer.annotator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.annotator.events.AnnotatorFilelistChgEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;

/**
 * Manages overlays and overlay elements ...
 *
 * @author Christian
 *
 */
public class AnnotatorManager<T extends RealType<T>> extends AbstractAnnotatorManager<T> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /* Are serialized */
    private Map<String, Overlay<String>> m_overlayMap;

    public AnnotatorManager() {
        super();
        setOverlayMap(new HashMap<String, Overlay<String>>());
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

    /*
     * Handling storage
     */

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

    @Override
    public Map<String, Overlay<String>> getOverlayMap() {
        return m_overlayMap;
    }

    public void setOverlayMap(final Map<String, Overlay<String>> m_overlayMap) {
        this.m_overlayMap = m_overlayMap;
    }

    /**
     * {@inheritDoc}
     */
    @EventListener
    public void reset2(final AnnotatorResetEvent e) {
        m_overlayMap = new HashMap<String, Overlay<String>>();
    }
}
