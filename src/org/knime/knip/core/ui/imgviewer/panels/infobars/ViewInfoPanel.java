package org.knime.knip.core.ui.imgviewer.panels.infobars;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseMovedEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;

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

        private final JLabel m_mouseInfoLabel;

        private final JLabel m_imageInfoLabel;

        private CalibratedSpace m_imgAxes;

        private long[] m_pos;

        private ImgViewerMouseEvent m_currentCoords;

        private long[] m_dims;

        private final JPanel m_mouseInfoPanel;

        private final JPanel m_imageInfoPanel;

        public ViewInfoPanel() {
                super("Image Info", false);
                m_infoBuffer = new StringBuffer();

                m_mouseInfoLabel = new JLabel();
                m_imageInfoLabel = new JLabel();

                m_mouseInfoPanel = new JPanel(new BorderLayout());
                m_imageInfoPanel = new JPanel(new BorderLayout());
                m_mouseInfoPanel.add(m_mouseInfoLabel, BorderLayout.LINE_START);
                m_imageInfoPanel.add(m_imageInfoLabel, BorderLayout.LINE_END);

                setLayout(new GridLayout(2, 1));
                add(m_mouseInfoPanel);
                add(m_imageInfoPanel);
        }

        protected abstract String updateMouseLabel(StringBuffer buffer, I img,
                        CalibratedSpace axes, RandomAccess<T> rndAccess,
                        long[] coords);

        protected abstract String updateImageLabel(StringBuffer buffer, I img,
                        RandomAccess<T> rndAccess, String imgName);

        /**
         * @param name
         */
        @EventListener
        public void onImgChanged(IntervalWithMetadataChgEvent<I> e) {
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

                m_imageInfoLabel.setText(updateImageLabel(m_infoBuffer, m_img,
                                m_rndAccess, e.getName().getName()));
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

                m_mouseInfoLabel.setText(updateMouseLabel(m_infoBuffer, m_img,
                                m_imgAxes, m_rndAccess, m_pos));

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
                        m_mouseInfoLabel.setText(updateMouseLabel(m_infoBuffer,
                                        m_img, m_imgAxes, m_rndAccess, m_pos));
                        m_infoBuffer.setLength(0);
                }
        }

        @Override
        public Position getPosition() {
                return Position.CENTER;
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
                m_mouseInfoLabel.setText("");
                m_imageInfoLabel.setText("");
                m_infoBuffer.setLength(0);
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }

        /**
         * sets the mouse and image info labels. This method is intended to be
         * used if a subclass reacts to additional events ... that should change
         * the labels.
         *
         * @param mouseInfoText
         * @param imageInfoText
         */
        protected void manualTextUpdate(String mouseInfoText,
                        String imageInfoText) {
                m_mouseInfoLabel.setText(mouseInfoText);
                m_imageInfoLabel.setText(imageInfoText);
        }

}