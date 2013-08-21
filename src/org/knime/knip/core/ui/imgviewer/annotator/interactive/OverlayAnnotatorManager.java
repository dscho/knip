package org.knime.knip.core.ui.imgviewer.annotator.interactive;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.annotator.AbstractAnnotatorManager;
import org.knime.knip.core.ui.imgviewer.annotator.AnnotatorResetEvent;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;

/**
 * Manages overlays and overlay elements ...
 * @param <T>
 *
 *
 *
 */
public class OverlayAnnotatorManager<T extends RealType<T>> extends AbstractAnnotatorManager<T> {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    private Map<String, Overlay<String>> m_overlayMap;

    /**
     *
     */
    public OverlayAnnotatorManager() {
        super();
        m_overlayMap = new HashMap<String, Overlay<String>>();
    }

    /*
     * Handling storage
     */

    /**
     * {@inheritDoc}
     */
    @EventListener
    public void reset2(final AnnotatorResetEvent e) {
        m_overlayMap = new HashMap<String, Overlay<String>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Overlay<String>> getOverlayMap() {
        return m_overlayMap;
    }

    /**
     *
     * @param srcName
     * @return
     */
    public Overlay<String> getOverlay(final String srcName) {
        return m_overlayMap.get(srcName);
    }

    /**
     *
     * @param srcName
     * @param overlay
     */
    public void addOverlay(final String srcName, final Overlay<String> overlay) {
        m_overlayMap.put(srcName, overlay);
    }

    //save and load componentConfig NOT LONGER USED

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
       //nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException, ClassNotFoundException {
        //nothing to do
    }

}
