package org.knime.knip.core.awt.specializedrendering;

import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;

class RenderTripel {

        Abstract2DProjector<?, ?> m_projector;
        ScreenImage m_image;
        boolean m_successfull;

        RenderTripel(Abstract2DProjector<?, ?> projector,
                        ScreenImage image) {
                m_projector = projector;
                m_image = image;
                m_successfull = true;
        }

        RenderTripel() {
                m_successfull = false;
        }


}
