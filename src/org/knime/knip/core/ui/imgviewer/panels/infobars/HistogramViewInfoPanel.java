package org.knime.knip.core.ui.imgviewer.panels.infobars;

import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JLabel;

import net.imglib2.img.Img;
import net.imglib2.type.Type;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.AWTImageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.HistogramChgEvent;
import org.knime.knip.core.ui.imgviewer.events.HistogramFactorChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseMovedEvent;

/**
 *
 *
 * @author dietzc
 */
public class HistogramViewInfoPanel<T extends Type<T>, I extends Img<T>>
                extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private final JLabel m_infoLabel;

        private final StringBuffer m_infoBuffer;

        private int[] m_hist;

        private double m_factor = 1;

        private int m_binPosition;

        private long[] m_dims;

        public HistogramViewInfoPanel() {
                super("Image Info", false);
                m_infoLabel = new JLabel();
                m_infoBuffer = new StringBuffer();

                add(m_infoLabel);
        }

        @EventListener
        public void onMouseMoved(ImgViewerMouseMovedEvent e) {
                if (e.isInsideImgView(m_dims[0], m_dims[1])) {
                        m_binPosition = e.getPosX();
                        updateLabel();
                }
        }

        @EventListener
        public void onHistAWTImageChanged(AWTImageChgEvent e) {
                m_dims = new long[2];
                m_dims[0] = e.getImage().getWidth(null);
                m_dims[1] = e.getImage().getHeight(null);
        }

        @EventListener
        public void onHistogramChanged(HistogramChgEvent e) {
                m_hist = e.getHistogram();
        }

        @EventListener
        public void onHistogramFactorChanged(HistogramFactorChgEvent e) {
                m_factor = e.getFactor();
        }

        /** Updates cursor probe label. */
        protected void updateLabel() {

                m_infoBuffer.setLength(0);

                if (m_infoBuffer.length() > 0) {
                        m_infoBuffer.deleteCharAt(m_infoBuffer.length() - 1);
                }

                int x = m_binPosition;

                if (x >= 0 && x < m_hist.length) {
                        m_infoBuffer.append("value=");
                        m_infoBuffer.append(String.format(
                                        "[from %.2f; to %.2f]", m_factor * x,
                                        m_factor * (x + 1)));
                        m_infoBuffer.append("; count=");
                        m_infoBuffer.append(m_hist[x]);

                        m_infoLabel.setText(m_infoBuffer.toString());
                        m_infoBuffer.setLength(0);
                }

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
                return Position.CENTER;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        /**
         * {@inheritDoc}
         */
        public void setEventService(EventService eventService) {
                eventService.subscribe(this);

        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                // Nothing to do here
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException {
                // Nothing to do here
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
                // Nothing to do here
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }

}
