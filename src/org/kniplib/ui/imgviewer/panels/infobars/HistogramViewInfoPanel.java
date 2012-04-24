package org.kniplib.ui.imgviewer.panels.infobars;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JLabel;

import net.imglib2.img.Img;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.type.Type;

import org.kniplib.ui.event.EventListener;
import org.kniplib.ui.event.EventService;
import org.kniplib.ui.imgviewer.ViewerComponent;
import org.kniplib.ui.imgviewer.events.HistogramChgEvent;
import org.kniplib.ui.imgviewer.events.HistogramFactorChgEvent;
import org.kniplib.ui.imgviewer.events.ImgChgEvent;
import org.kniplib.ui.imgviewer.events.ImgViewerMouseMovedEvent;
import org.kniplib.ui.imgviewer.events.PlaneSelectionEvent;

/**
 *
 *
 * @author dietzc
 */
public class HistogramViewInfoPanel<T extends Type<T>, I extends Img<T>>
                extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        private final JLabel m_infoLabel;

        private I m_img;

        private PlaneSelectionEvent m_sel;

        private final StringBuffer m_infoBuffer;

        private CalibratedSpace m_imgAxes;

        private int[] m_hist;

        private double m_factor = 1;

        private long[] m_pos;

        private long[] m_dims;

        public HistogramViewInfoPanel() {
                super("Image Info", false);
                m_infoLabel = new JLabel();
                m_infoBuffer = new StringBuffer();

                add(m_infoLabel);
        }

        /**
         * @param name
         */
        @EventListener
        public void onImgChanged(ImgChgEvent<I> e) {

                m_img = e.getInterval();
                m_dims = new long[m_img.numDimensions()];
                m_img.dimensions(m_dims);
                m_imgAxes = e.getCalibratedSpace();

                if (m_sel == null
                                || m_sel.numDimensions() != e.getInterval()
                                                .numDimensions()) {
                        onPlaneSelectionChanged(new PlaneSelectionEvent(0, 1,
                                        new long[e.getInterval()
                                                        .numDimensions()]));
                }
        }

        @EventListener
        public void onPlaneSelectionChanged(PlaneSelectionEvent e) {
                m_sel = e;
                m_pos = m_sel.getPlanePos().clone();
                m_pos[m_sel.getPlaneDimIndex1()] = -1;
                m_pos[m_sel.getPlaneDimIndex2()] = -1;

                updateLabel();
        }

        @EventListener
        public void onMouseMoved(ImgViewerMouseMovedEvent e) {
                if (e.isInsideImgView(m_dims[m_sel.getPlaneDimIndex1()],
                                m_dims[m_sel.getPlaneDimIndex2()])) {
                        m_pos = m_sel.getPlanePos(e.getPosX(), e.getPosY());
                        updateLabel();
                }
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

                // TODO Array index out of bounds
                m_infoBuffer.setLength(0);

                for (int i = 0; i < m_pos.length; i++) {
                        m_infoBuffer.append(" ");
                        if (i < m_img.numDimensions()) {
                                m_infoBuffer.append(m_imgAxes != null ? m_imgAxes
                                                .axis(i).getLabel() : i);
                        }
                        if (m_pos[i] == -1) {
                                m_infoBuffer.append("[ Not set ];");
                        } else {
                                m_infoBuffer.append("[" + (m_pos[i] + 1) + "/"
                                                + m_img.dimension(i) + "];");
                        }
                }

                if (m_infoBuffer.length() > 0) {
                        m_infoBuffer.deleteCharAt(m_infoBuffer.length() - 1);
                }

                int x = (int) m_pos[m_sel.getPlaneDimIndex1()];

                if (x >= 0 && x < m_hist.length) {

                        m_infoBuffer.append("; ");
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
        public String getPosition() {
                return BorderLayout.CENTER;
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
