package org.kniplib.ui.imgviewer.panels.infobars;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

import org.kniplib.ui.event.EventListener;
import org.kniplib.ui.event.EventService;
import org.kniplib.ui.imgviewer.ViewerComponent;
import org.kniplib.ui.imgviewer.events.ImgChgEvent;
import org.kniplib.ui.imgviewer.events.ImgViewerMouseEvent;
import org.kniplib.ui.imgviewer.events.ImgViewerMouseMovedEvent;
import org.kniplib.ui.imgviewer.events.PlaneSelectionEvent;

/**
 *
 *
 *
 * @author dietzc, hornm, schoenenbergerf
 */
public abstract class ViewInfoPanel<T extends Type<T>, I extends RandomAccessibleInterval<T> & IterableInterval<T>>
                extends ViewerComponent {

        protected I m_img;

        protected RandomAccess<T> m_rndAccess;

        protected PlaneSelectionEvent m_sel;

        private final StringBuffer m_infoBuffer;

        private static final long serialVersionUID = 1L;

        private final JLabel m_infoLabel;

        private CalibratedSpace m_imgAxes;

        private long[] m_pos;

        private ImgViewerMouseEvent m_currentCoords;

        private long[] m_dims;

        public ViewInfoPanel() {
                super("Image Info", false);
                m_infoLabel = new JLabel();
                m_infoBuffer = new StringBuffer();
                add(m_infoLabel);
        }

        protected abstract String updateLabel(I img, StringBuffer buffer,
                        CalibratedSpace axes, RandomAccess<T> rndAccess,
                        long[] coords);

        /**
         * @param name
         */
        @EventListener
        public void onImgChanged(ImgChgEvent<I> e) {
                m_img = e.getInterval();
                m_dims = new long[e.getInterval().numDimensions()];
                m_img.dimensions(m_dims);
                m_imgAxes = e.getCalibratedSpace();
                T val = e.getInterval().firstElement().createVariable();
                m_rndAccess = Views.extendValue(m_img, val).randomAccess();

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

                if (m_currentCoords == null
                                || !m_currentCoords
                                                .isInsideImgView(
                                                                m_dims[m_sel.getPlaneDimIndex1()],
                                                                m_dims[m_sel.getPlaneDimIndex2()])) {
                        m_pos[m_sel.getPlaneDimIndex1()] = -1;
                        m_pos[m_sel.getPlaneDimIndex2()] = -1;
                }

                m_infoLabel.setText(updateLabel(m_img, m_infoBuffer, m_imgAxes,
                                m_rndAccess, m_pos));
                m_infoBuffer.setLength(0);
        }

        @EventListener
        public void onMouseMoved(ImgViewerMouseMovedEvent e) {
                m_currentCoords = e;
                if (m_currentCoords.isInsideImgView(
                                m_dims[m_sel.getPlaneDimIndex1()],
                                m_dims[m_sel.getPlaneDimIndex2()])) {
                        m_pos = m_sel.getPlanePos(m_currentCoords.getPosX(),
                                        m_currentCoords.getPosY());
                        m_infoLabel.setText(updateLabel(m_img, m_infoBuffer,
                                        m_imgAxes, m_rndAccess, m_pos));
                        m_infoBuffer.setLength(0);
                }
        }

        @Override
        public String getPosition() {
                return BorderLayout.CENTER;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEventService(EventService eventService) {
                eventService.subscribe(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
                m_infoLabel.setText("");
                m_infoBuffer.setLength(0);
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }

}
