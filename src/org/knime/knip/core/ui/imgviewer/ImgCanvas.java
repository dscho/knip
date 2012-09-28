package org.knime.knip.core.ui.imgviewer;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.type.Type;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.events.AWTImageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.AddCrosshairEvent;
import org.knime.knip.core.ui.imgviewer.events.CrosshairSettingsChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseDraggedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseMovedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMousePressedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerMouseReleasedEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerRectChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgViewerTextMessageChgEvent;
import org.knime.knip.core.ui.imgviewer.events.MinimapOffsetChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewZoomfactorChgEvent;
import org.knime.knip.core.ui.imgviewer.panels.MinimapPanel;

/**
 *
 * Panel to draw a BufferedImage.
 *
 * Propagates {@link ImgViewerRectChgEvent}.
 *
 * @author dietzc, hornm, fschoenenberer, muethingc
 */
public class ImgCanvas<T extends Type<T>, I extends IterableInterval<T> & RandomAccessible<T>>
                extends ViewerComponent {

        private class Crosshair {
                private int m_x;
                private int m_y;
                private final Color m_xColor;
                private final Color m_yColor;
                private final Color m_xSelectionColor;
                private final Color m_ySelectionColor;

                private ColorWrapper m_selectedColor = null;

                public Crosshair(final int x, final int y, final Color xColor,
                                final Color yColor) {
                        m_x = x;
                        m_y = y;
                        m_xColor = xColor;
                        m_yColor = yColor;
                        m_xSelectionColor = m_colorDispenser.next();
                        m_ySelectionColor = m_colorDispenser.next();
                }

                public int getXDrawingPos(final double factor) {
                        return (int) ((m_x - 0.5) * factor);
                }

                public int getYDrawingPos(final double factor) {
                        return (int) ((m_y - 0.5) * factor);
                }

                public void setX(int x) {
                    x = x < 0 ? 0 : x;
                    x = x > m_imageCanvas.getWidth() ? m_imageCanvas.getWidth() : x;
                    m_x = x;
                }

                public void setY(int y) {
                    y = y < 0 ? 0 : y;
                    y = y > m_imageCanvas.getHeight() ? m_imageCanvas.getHeight() : y;
                    m_y = y;
                }
        }

        private static BufferedImage TEXTMSGIMG = new BufferedImage(100, 50,
                        BufferedImage.TYPE_INT_RGB);

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private static final int SELECTION_EXTRA = 4;

        private final JPanel m_imageCanvas;

        private final JScrollPane m_imageScrollPane;

        private BufferedImage m_image;

        private double m_factor;

        private double m_oldFactor;

        private boolean m_keyDraggingEnabled;

        private Point m_dragPoint;

        private Rectangle m_dragRect;

        private boolean horScrollbarMoved = false;

        private boolean verScrollbarMoved = false;

        protected Rectangle m_currentRectangle;

        protected StringBuffer m_labelBuffer = new StringBuffer();

        protected EventService m_eventService;

        private boolean m_blockMouseEvents;

        private final List<Crosshair> m_crosshairs = new ArrayList<Crosshair>();

        private int m_crossHairThickness = 2;
        private int m_crossHairThicknessSelection = m_crossHairThickness
                        + SELECTION_EXTRA;
        private float m_crossHairAlpha = 1.0f;

        private final Cursor m_moveCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        private final Cursor m_defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        private final ColorDispenser m_colorDispenser = new ColorDispenser();

        private final Map<ColorWrapper, Crosshair> m_selectionMap = new HashMap<ColorWrapper, Crosshair>();

        private Crosshair m_selectedCross = null;

        public ImgCanvas() {
                this("Image", false);
        }

        /**
         *
         */
        public ImgCanvas(final String name, final boolean isImageHidden) {
                super(name, isImageHidden);

                m_currentRectangle = new Rectangle();
                m_oldFactor = 1;

                m_imageCanvas = new JPanel() {
                        /**
                         *
                         */
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void paint(Graphics g) {
                                super.paint(g);
                                if (m_image == null)
                                        return;
                                g.drawImage(m_image,
                                                0,
                                                0,
                                                (int) (m_image.getWidth(null) * m_factor),
                                                (int) (m_image.getHeight(null) * m_factor),
                                                null);

                                // paint the crosshairs
                                paintCrosshairs(g, m_crossHairThickness, false);
                        }
                };

                m_imageCanvas.setBackground(Color.DARK_GRAY);
                // TODO discuss global key listener
                // getToolkit().addAWTEventListener(new AWTEventListener() {
                // @Override
                // public void eventDispatched(AWTEvent awte) {
                // KeyEvent e = (KeyEvent) awte;
                // if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                // if (e.getID() == KeyEvent.KEY_PRESSED) {
                // m_imageCanvas.setCursor(new Cursor(
                // Cursor.HAND_CURSOR));
                // m_keyDraggingEnabled = true;
                // } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                // m_imageCanvas.setCursor(new Cursor(
                // Cursor.DEFAULT_CURSOR));
                // m_keyDraggingEnabled = false;
                // }
                // }
                // }
                // }, AWTEvent.KEY_EVENT_MASK);

                m_imageCanvas.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent e) {
                                m_dragPoint = e.getLocationOnScreen();
                                m_dragRect = m_imageCanvas.getVisibleRect();

                                // remove the crosshair on right click
                                if (m_selectedCross != null
                                                && e.getButton() == MouseEvent.BUTTON3) {
                                        m_crosshairs.remove(m_selectedCross);
                                        m_selectionMap.remove(new ColorWrapper(
                                                        m_selectedCross.m_xSelectionColor));
                                        m_selectionMap.remove(new ColorWrapper(
                                                        m_selectedCross.m_ySelectionColor));
                                        repaint();
                                }

                                fireImageCoordMousePressed(e);
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {
                                fireImageCoordMouseReleased(e);
                        }

                });
                m_imageCanvas.addMouseMotionListener(new MouseMotionAdapter() {
                        @Override
                        public void mouseDragged(MouseEvent e) {

                                if (m_keyDraggingEnabled
                                                || ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 1024)) {

                                        // if no crosshair selected, move the
                                        // image
                                        if (m_selectedCross == null) {
                                                m_currentRectangle
                                                                .setBounds(m_dragRect);
                                                m_currentRectangle
                                                                .translate((m_dragPoint.x - e
                                                                                .getXOnScreen()),
                                                                                (m_dragPoint.y - e
                                                                                                .getYOnScreen()));
                                                m_imageCanvas.scrollRectToVisible(m_currentRectangle);
                                        } else {

                                                // x-axis movement
                                                if (m_selectedCross.m_selectedColor
                                                                .equals(new ColorWrapper(
                                                                                m_selectedCross.m_xSelectionColor))) {
                                                    m_selectedCross.setX((int) (e.getX() / m_factor));
                                                } else {
                                                    m_selectedCross.setY((int) (e.getY() / m_factor));
                                                }

                                                repaint();
                                        }
                                }
                                fireImageCoordMouseDragged(e);
                        }

                        @Override
                        public void mouseMoved(MouseEvent e) {
                                m_selectedCross = checkSelection(e.getX(),
                                                e.getY());

                                if (m_selectedCross != null) {
                                        setCursor(m_moveCursor);
                                } else {
                                        setCursor(m_defaultCursor);
                                }

                                fireImageCoordMouseMoved(e);
                        }
                });
                m_imageCanvas.addMouseWheelListener(new MouseWheelListener() {

                        @Override
                        public void mouseWheelMoved(MouseWheelEvent e) {
                                if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                                        int direction = -1;
                                        if (e.getWheelRotation() < 0) {
                                                direction = 1;
                                        }

                                        int oldValue = (int) (m_factor * 100.0);

                                        int change = (int) Math.sqrt(oldValue)
                                                        * direction;
                                        int newValue = oldValue + change;

                                        if (newValue < MinimapPanel.ZOOM_MIN) {
                                                newValue = MinimapPanel.ZOOM_MIN;
                                        } else if (newValue > MinimapPanel.ZOOM_MAX) {
                                                newValue = MinimapPanel.ZOOM_MAX;
                                        }

                                        m_eventService.publish(new ViewZoomfactorChgEvent(
                                                        newValue / 100d));

                                }
                        }

                });
                m_imageScrollPane = new JScrollPane(m_imageCanvas);

                m_imageScrollPane.getHorizontalScrollBar()
                                .addAdjustmentListener(
                                                new AdjustmentListener() {
                                                        @Override
                                                        public void adjustmentValueChanged(
                                                                        AdjustmentEvent e) {
                                                                if (verScrollbarMoved) {
                                                                        verScrollbarMoved = false;
                                                                        return;
                                                                }
                                                                horScrollbarMoved = true;
                                                                handleScrollbarEvent();
                                                        }
                                                });

                m_imageScrollPane.getVerticalScrollBar().addAdjustmentListener(
                                new AdjustmentListener() {
                                        @Override
                                        public void adjustmentValueChanged(
                                                        AdjustmentEvent e) {
                                                if (horScrollbarMoved) {
                                                        horScrollbarMoved = false;
                                                        return;
                                                }
                                                verScrollbarMoved = true;
                                                handleScrollbarEvent();
                                        }
                                });

                setLayout(new GridBagLayout());
                GridBagConstraints gc = new GridBagConstraints();
                gc.fill = GridBagConstraints.BOTH;
                gc.weightx = 1.0;
                gc.weighty = 1.0;

                add(m_imageScrollPane, gc);

                m_factor = 1;
                updateImageCanvas();

        }

        private Crosshair checkSelection(final int x, final int y) {
                BufferedImage backscreen = new BufferedImage(
                                m_imageCanvas.getWidth(),
                                m_imageCanvas.getHeight(),
                                BufferedImage.TYPE_INT_ARGB);
                paintCrosshairs(backscreen.createGraphics(),
                                m_crossHairThicknessSelection, true);

                ColorWrapper cw = new ColorWrapper(new Color(backscreen.getRGB(
                                x, y)));

                // check if this color belongs to one of the crosshairs
                if (m_selectionMap.containsKey(cw)) {
                        Crosshair selected = m_selectionMap.get(cw);
                        selected.m_selectedColor = cw;
                        return selected;
                } else {
                        return null;
                }
        }

        private void paintCrosshairs(final Graphics g, final int thickness,
                        final boolean selection) {
                Graphics2D g2 = (Graphics2D) g;

                if (!selection) {
                        g2.setComposite(AlphaComposite.getInstance(
                                        AlphaComposite.SRC_OVER,
                                        m_crossHairAlpha));
                }

                for (Crosshair c : m_crosshairs) {

                        g2.setStroke(new BasicStroke(thickness));

                        // draw x line
                        if (selection) {
                                g2.setColor(c.m_xSelectionColor);
                        } else {
                                g2.setColor(c.m_xColor);
                        }

                        g2.drawLine(c.getXDrawingPos(m_factor), 0,
                                        c.getXDrawingPos(m_factor),
                                        m_imageCanvas.getHeight());

                        // draw y line
                        if (selection) {
                                g2.setColor(c.m_ySelectionColor);
                        } else {
                                g2.setColor(c.m_yColor);
                        }

                        g2.drawLine(0, c.getYDrawingPos(m_factor),
                                        m_imageCanvas.getWidth(),
                                        c.getYDrawingPos(m_factor));
                }
        }

        private void handleScrollbarEvent() {
                if (m_currentRectangle == null
                                || !m_currentRectangle.equals(m_imageCanvas
                                                .getVisibleRect())) {
                        m_eventService.publish(new ImgViewerRectChgEvent(
                                        getVisibleImageRect()));
                }

        }

        private boolean isMouseEventBlocked() {
                return m_blockMouseEvents || m_image == null;
        }

        /**
         * Returns the visible bounding box in the image coordinate space.
         *
         * @return the visible bounding box.
         */
        public Rectangle getVisibleImageRect() {
                m_currentRectangle = m_imageCanvas.getVisibleRect();
                m_currentRectangle.x = (int) (m_currentRectangle.x / m_factor);
                m_currentRectangle.y = (int) (m_currentRectangle.y / m_factor);
                m_currentRectangle.width = (int) (m_currentRectangle.width / m_factor);
                m_currentRectangle.height = (int) (m_currentRectangle.height / m_factor);
                return m_currentRectangle;
        }

        private void fireImageCoordMousePressed(MouseEvent e) {
                if (!isMouseEventBlocked()) {
                        m_eventService.publish(new ImgViewerMousePressedEvent(
                                        e, m_factor, m_image.getWidth(),
                                        m_image.getHeight()));
                }

        }

        private void fireImageCoordMouseReleased(MouseEvent e) {
                if (!isMouseEventBlocked()) {
                        m_eventService.publish(

                        new ImgViewerMouseReleasedEvent(e, m_factor, m_image
                                        .getWidth(), m_image.getHeight()));
                }

        }

        private void fireImageCoordMouseDragged(MouseEvent e) {
                if (!isMouseEventBlocked()) {
                        m_eventService.publish(

                        new ImgViewerMouseDraggedEvent(e, m_factor, m_image
                                        .getWidth(), m_image.getHeight()));
                }

        }

        private void fireImageCoordMouseMoved(MouseEvent e) {
                if (!isMouseEventBlocked()) {
                        m_eventService.publish(new ImgViewerMouseMovedEvent(e,
                                        m_factor, m_image.getWidth(), m_image
                                                        .getHeight()));
                }

        }

        @EventListener
        public void onZoomFactorChanged(ViewZoomfactorChgEvent zoomEvent) {
                m_factor = zoomEvent.getZoomFactor();
                updateImageCanvas();
        }

        @EventListener
        public void onAddCrosshair(final AddCrosshairEvent e) {
                int x = (int) (m_imageCanvas.getWidth() / 2 / m_factor);
                int y = (int) (m_imageCanvas.getHeight() / 2 / m_factor);
                Crosshair ch = new Crosshair(x, y, e.getXColor(), e.getYColor());
                m_selectionMap.put(new ColorWrapper(ch.m_xSelectionColor), ch);
                m_selectionMap.put(new ColorWrapper(ch.m_ySelectionColor), ch);
                m_crosshairs.add(ch);
                repaint();
        }

        @EventListener
        public void onCrosshairSettingsChg(final CrosshairSettingsChgEvent e) {
                m_crossHairThickness = e.getThickness();
                m_crossHairThicknessSelection = e.getThickness() + SELECTION_EXTRA;
                m_crossHairAlpha = e.getAlpha();
                repaint();
        }

        /**
         * Scrolls the image so the rectangle gets visible.
         *
         * @param rect
         */
        @EventListener
        public void onMinimapOffsetChanged(MinimapOffsetChgEvent e) {
                m_currentRectangle = m_imageCanvas.getVisibleRect();
                m_currentRectangle.x = (int) (e.getOffest()[0] * m_factor);
                m_currentRectangle.y = (int) (e.getOffest()[1] * m_factor);
                m_imageCanvas.scrollRectToVisible(m_currentRectangle);
                updateImageCanvas();
        }

        @EventListener
        public void onBufferedImageChanged(AWTImageChgEvent e) {
                m_image = (BufferedImage) e.getImage();
                m_blockMouseEvents = false;
                updateImageCanvas();
        }

        public void updateImageCanvas() {
                if (m_image == null)
                        return;

                if (m_oldFactor != m_factor) {

                        // get old center of the image

                        Rectangle rect = m_imageCanvas.getVisibleRect();
                        double imgCenterX = rect.getCenterX() / m_oldFactor;
                        double imgCenterY = rect.getCenterY() / m_oldFactor;

                        // enlarge canvas
                        Dimension d = new Dimension(
                                        (int) (m_image.getWidth(null) * m_factor),
                                        (int) (m_image.getHeight(null) * m_factor));
                        m_imageCanvas.setSize(d);
                        m_imageCanvas.setPreferredSize(d);

                        double xCorrect = getVisibleImageRect().width / 2.0;
                        double yCorrect = getVisibleImageRect().height / 2.0;

                        // apply old center
                        m_imageScrollPane
                                        .getViewport()
                                        .setViewPosition(
                                                        new Point(
                                                                        (int) (((imgCenterX - xCorrect) * m_factor)),
                                                                        (int) (((imgCenterY - yCorrect) * m_factor))));

                        m_oldFactor = m_factor;

                }
                m_imageScrollPane.validate();
                m_imageScrollPane.repaint();
        }

        /**
         * An image with the message.
         *
         * @param message
         */
        @EventListener
        public void onTextMessageChanged(ImgViewerTextMessageChgEvent e) {

                Graphics2D g = (Graphics2D) m_imageCanvas.getGraphics();
                if (g != null) {
                        g.setBackground(Color.GRAY.darker());
                        g.clearRect(0, 0, m_imageCanvas.getWidth(),
                                        m_imageCanvas.getHeight());
                        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                        int h = g.getFont().getSize();
                        int y = h;
                        g.setColor(Color.YELLOW);
                        for (String s : e.getMessage().split("\n")) {
                                g.drawString(s, h, y);
                                y += h;
                        }

                        m_blockMouseEvents = true;
                        m_image = TEXTMSGIMG;
                        updateImageCanvas();
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
        public void setEventService(EventService eventService) {
                m_eventService = eventService;
                eventService.subscribe(this);
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                out.writeDouble(m_factor);
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                m_factor = in.readDouble();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
                m_currentRectangle = new Rectangle();
                m_factor = 1;
                m_oldFactor = 1;
                m_image = null;
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }

}
