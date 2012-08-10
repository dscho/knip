package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.AWTImageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerRectChgEvent;
import org.knime.knip.core.ui.imgviewer.events.MinimapOffsetChgEvent;
import org.knime.knip.core.ui.imgviewer.events.MinimapZoomfactorChgEvent;

/**
 * A panel showing the minimap of a buffered image and enables the user to zoom
 * and change the focus.
 *
 * Publishes {@link MinimapOffsetChgEvent} and {@link MinimapZoomfactorChgEvent}
 * .
 *
 * @author dietzc, hornm, schoenenbergerf
 */
public class MinimapPanel extends ViewerComponent {

        /**
	 *
	 */
        private static final long serialVersionUID = 1L;

        private static final Color BOUNDING_BOX_COLOR = new Color(0, 127, 255,
                        60);

        private static final Color BOUNDING_BOX_BORDER_COLOR = new Color(0,
                        127, 255, 255);

        private static final Integer[] ZOOM_LEVELS = new Integer[] { 25, 50,
                        75, 100, 200, 400, 800 };

        private final JSlider m_zoomSlider;

        private final JComboBox m_zoomComboBox;

        private boolean m_isZoomAdjusting;

        private final JPanel m_canvas;

        private Rectangle m_visibleRect;

        private BufferedImage m_img;

        private int[] m_offset;

        private float m_scaleFactor;

        private EventService m_eventService;

        public MinimapPanel() {
                super("Minimap", false);

                setPreferredSize(new Dimension(160, getPreferredSize().height));
                setMaximumSize(new Dimension(160, getMaximumSize().height));
                setLayout(new BorderLayout());

                m_offset = new int[2];

                m_visibleRect = new Rectangle();
                addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(ComponentEvent e) {
                                SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                                if (m_img != null) {
                                                        onBufferedImageUpdated(new AWTImageChgEvent(
                                                                        m_img));
                                                }

                                        }
                                });
                        }
                });

                m_canvas = new JPanel() {
                        @Override
                        public void paint(Graphics g) {
                                super.paint(g);
                                if (m_img != null) {
                                        int w = (int) (m_img.getWidth() * m_scaleFactor);
                                        int h = (int) (m_img.getHeight() * m_scaleFactor);
                                        g.drawImage(m_img, 0, 0, w, h, null);
                                        g.setColor(BOUNDING_BOX_COLOR);
                                        g.fillRect((int) Math
                                                        .min(Math.max(m_offset[0]
                                                                        * m_scaleFactor
                                                                        + m_visibleRect.x,
                                                                        0),
                                                                        w
                                                                                        - m_visibleRect.width),
                                                        (int) Math.min(Math
                                                                        .max(m_offset[1]
                                                                                        * m_scaleFactor
                                                                                        + m_visibleRect.y,
                                                                                        0),
                                                                        h
                                                                                        - m_visibleRect.height),
                                                        (m_visibleRect.width),
                                                        (m_visibleRect.height));
                                        g.setColor(BOUNDING_BOX_BORDER_COLOR);
                                        g.drawRect((int) Math
                                                        .min(Math.max(m_offset[0]
                                                                        * m_scaleFactor
                                                                        + m_visibleRect.x,
                                                                        0),
                                                                        w
                                                                                        - m_visibleRect.width),
                                                        (int) Math.min(Math
                                                                        .max(m_offset[1]
                                                                                        * m_scaleFactor
                                                                                        + m_visibleRect.y,
                                                                                        0),
                                                                        h
                                                                                        - m_visibleRect.height),
                                                        (m_visibleRect.width),
                                                        (m_visibleRect.height));
                                }

                        }
                };
                m_canvas.setBackground(Color.DARK_GRAY);
                m_canvas.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {

                                m_offset[0] = ((int) ((e.getX() - m_visibleRect.width / 2) / m_scaleFactor));
                                m_offset[1] = ((int) ((e.getY() - m_visibleRect.height / 2) / m_scaleFactor));
                                SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                                m_scaleFactor = m_canvas
                                                                .getWidth()
                                                                / (float) m_img.getWidth();
                                                float t = m_canvas.getHeight()
                                                                / (float) m_img.getHeight();
                                                if (t < m_scaleFactor) {
                                                        m_scaleFactor = t;
                                                }

                                                repaint();
                                        }
                                });
                                m_eventService.publish(new MinimapOffsetChgEvent(
                                                m_offset));
                        }
                });
                m_canvas.addMouseMotionListener(new MouseMotionAdapter() {

                        @Override
                        public void mouseDragged(MouseEvent e) {

                                m_offset[0] = ((int) ((e.getX() - m_visibleRect.width / 2) / m_scaleFactor));
                                m_offset[1] = ((int) ((e.getY() - m_visibleRect.height / 2) / m_scaleFactor));

                                SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                                m_scaleFactor = m_canvas
                                                                .getWidth()
                                                                / (float) m_img.getWidth();
                                                float t = m_canvas.getHeight()
                                                                / (float) m_img.getHeight();
                                                if (t < m_scaleFactor) {
                                                        m_scaleFactor = t;
                                                }

                                                repaint();
                                        }
                                });
                                m_eventService.publish(new MinimapOffsetChgEvent(
                                                m_offset));
                        }
                });
                add(m_canvas, BorderLayout.CENTER);

                JPanel jp = new JPanel(new BorderLayout());
                add(jp, BorderLayout.SOUTH);

                m_zoomSlider = new JSlider(10, 1000, 100);
                m_zoomSlider.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                                if (m_isZoomAdjusting)
                                        return;
                                m_isZoomAdjusting = true;
                                m_zoomComboBox.setSelectedItem(new Integer(
                                                m_zoomSlider.getValue()));
                                m_isZoomAdjusting = false;
                                m_eventService.publish(new MinimapZoomfactorChgEvent(
                                                m_zoomSlider.getValue() / 100d));
                        }
                });
                jp.add(m_zoomSlider, BorderLayout.CENTER);

                m_zoomComboBox = new JComboBox(ZOOM_LEVELS);
                m_zoomComboBox.setPreferredSize(new Dimension(55,
                                m_zoomComboBox.getPreferredSize().height));
                m_zoomComboBox.setEditable(true);

                jp.add(m_zoomComboBox, BorderLayout.EAST);
                m_zoomComboBox.setSelectedIndex(3);

                m_zoomComboBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (m_isZoomAdjusting)
                                        return;
                                m_isZoomAdjusting = true;
                                m_zoomSlider.setValue((Integer) m_zoomComboBox
                                                .getSelectedItem());
                                m_isZoomAdjusting = false;

                                m_eventService.publish(new MinimapZoomfactorChgEvent(
                                                ((Integer) m_zoomComboBox
                                                                .getSelectedItem())
                                                                .doubleValue() / 100));
                        }
                });
        }

        @EventListener
        public void onBufferedImageUpdated(AWTImageChgEvent e) {
                m_img = (BufferedImage) e.getImage();

                SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                                if (m_visibleRect.width == 0
                                                && m_visibleRect.height == 0) {
                                        m_scaleFactor = m_canvas.getWidth()
                                                        / (float) m_img.getWidth();
                                        float t = m_canvas.getHeight()
                                                        / (float) m_img.getHeight();
                                        if (t < m_scaleFactor) {
                                                m_scaleFactor = t;
                                        }
                                        m_visibleRect.setBounds(
                                                        0,
                                                        0,
                                                        (int) (m_img.getWidth() * m_scaleFactor),
                                                        (int) (m_img.getHeight() * m_scaleFactor));
                                }

                                repaint();
                        }
                });
        }

        @EventListener
        public void onRectangleUpdated(ImgViewerRectChgEvent e) {
                m_visibleRect.setBounds(e.getRectangle());
                m_offset[0] = e.getRectangle().x;
                m_offset[1] = e.getRectangle().y;

                m_visibleRect.setBounds(0, 0,
                                (int) (m_visibleRect.width * m_scaleFactor),
                                (int) (m_visibleRect.height * m_scaleFactor));

                m_canvas.repaint();
        }

        @Override
        public Position getPosition() {
                return Position.SOUTH;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEventService(EventService eventService) {
                eventService.subscribe(this);
                m_eventService = eventService;
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                out.writeInt(m_zoomSlider.getValue());
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                m_zoomSlider.setValue(in.readInt());
        }

        @Override
        public void reset() {
                m_img = null;
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }

}