package org.knime.knip.core.awt.specializedrendering;

import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;

class RenderTripel {

    private Abstract2DProjector<?, ?> m_projector;

    private ScreenImage m_img;

    private boolean m_successfull;

    RenderTripel(final Abstract2DProjector<?, ?> projector, final ScreenImage img) {
        this.m_projector = projector;
        this.m_img = img;
        this.m_successfull = true;
    }

    RenderTripel() {
        this.m_successfull = false;
    }

    /**
     * @return the m_successfull
     */
    public boolean isSuccessfull() {
        return m_successfull;
    }

    /**
     * @return the m_projector
     */
    public Abstract2DProjector<?, ?> getProjector() {
        return m_projector;
    }

    /**
     * @return the m_image
     */
    public ScreenImage getImage() {
        return m_img;
    }

}
