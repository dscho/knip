/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003, 2010
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */
package org.knime.knip.core.ui.imgviewer.panels.transfunc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ColorDispenser;
import org.knime.knip.core.ui.imgviewer.ColorWrapper;
import org.knime.knip.core.ui.transfunc.PolylineTransferFunction;
import org.knime.knip.core.ui.transfunc.TransferFunction;
import org.knime.knip.core.ui.transfunc.TransferFunctionBundle;
import org.knime.knip.core.ui.transfunc.TransferFunctionColor;

/**
 * This class displays and allows the manipulation of Transferfunctions.
 *
 * It supports two Modes, Gray and RGB. In both modes a transferfunction for the
 * opacity is drawn. In Gray mode this is amplified with a single
 * transferfunction and in RGB mode three additional transferfunctions are
 * available, one for each color.
 *
 * @author Clemens Müthing (clemens.muething@uni-konstanz.de)
 * @author Clemens M??thing (clemens.muething@uni-konstanz.de)
 * @author Clemens M??thing (clemens.muething@uni-konstanz.de)
 * @version
 */
public class TransferFunctionPainter {

    private final class TFSection {
        public final PolylineTransferFunction m_func;
        public final PolylineTransferFunction.Point m_p0;
        public final PolylineTransferFunction.Point m_p1;

        public TFSection(final PolylineTransferFunction func,
                final PolylineTransferFunction.Point p0,
                final PolylineTransferFunction.Point p1) {
            m_func = func;
            m_p0 = p0;
            m_p1 = p1;
        }
    }

    private class Adapter extends MouseAdapter {

        private int m_oldX = 0;
        private int m_oldY = 0;

        private void storeCoord(final MouseEvent event) {
            m_oldX = event.getX();
            m_oldY = event.getY();
        }

        /**
         * This method is used to actually move a control point.
         *
         * {@inheritDoc}
         *
         * @see MouseMotionListener#mouseDragged(MouseEvent)
         */
        @Override
        public final void mouseDragged(final MouseEvent event) {
            // drag the linesegment left-right, only possible when no fixed
            // point is involved
            if (m_transferSelected != null && m_tfsectionSelected != null
                    && event.isShiftDown()
                    && !m_tfsectionSelected.m_p0.getFixed()
                    && !m_tfsectionSelected.m_p1.getFixed()) {
                int x0 = (event.getX() - m_oldX)
                        + getXPanelCoordinate(m_tfsectionSelected.m_p0.getX(),
                                true);

                int x1 = (event.getX() - m_oldX)
                        + getXPanelCoordinate(m_tfsectionSelected.m_p1.getX(),
                                true);

                // make sure the ramp cannot be changed when hitting the borders
                if (x0 >= 0 && x1 <= m_width) {
                    PolylineTransferFunction temp = moveControlPointX(
                            m_tfsectionSelected.m_p0, x0);
                    temp = moveControlPointX(m_tfsectionSelected.m_p1, x1);
                    m_eventService.publish(new PointMovedEvent(temp));
                }
            }

            // drag the linesegment up-down
            if (m_transferSelected != null && m_tfsectionSelected != null
                    && event.isControlDown()) {
                int y0 = (event.getY() - m_oldY)
                        + getYPanelCoordinate(m_tfsectionSelected.m_p0.getY(),
                                true);

                int y1 = (event.getY() - m_oldY)
                        + getYPanelCoordinate(m_tfsectionSelected.m_p1.getY(),
                                true);

                // make sure the ramp cannot be changed when hitting the borders
                if (y0 >= 0 && y0 <= m_height && y1 >= 0 && y1 <= m_height) {
                    PolylineTransferFunction temp = moveControlPointY(
                            m_tfsectionSelected.m_p0, y0);
                    temp = moveControlPointY(m_tfsectionSelected.m_p1, y1);
                    m_eventService.publish(new PointMovedEvent(temp));
                }
            }

            // drag a point
            else if (m_transferSelected != null && m_pointSelected0 != null
                    && m_pointSelected1 == null) {
                PolylineTransferFunction temp = moveControlPoint(
                        m_pointSelected0, event.getX(), event.getY());

                m_eventService.publish(new PointMovedEvent(temp));
            }

            storeCoord(event);
        }

        /**
         * In this method we check if something has been selected.
         *
         * {@inheritDoc}
         *
         * @see MouseMotionListener#mouseMoved(MouseEvent)
         */
        @Override
        public final void mouseMoved(final MouseEvent event) {

            // paint the selection image
            BufferedImage selectionImage = new BufferedImage(m_widthReal,
                    m_heightReal, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = selectionImage.createGraphics();
            paint(g2, true);

            // extract the color
            Color c = new Color(selectionImage.getRGB(event.getX(),
                    event.getY()));
            ColorWrapper cw = new ColorWrapper(c);

            // find the selected element
            PolylineTransferFunction control = null;

            if (m_mapColorPoint.containsKey(cw)) {
                m_pointSelected0 = m_mapColorPoint.get(cw);
                m_pointSelected0.setSelected(true);
                control = m_mapPointTF.get(m_pointSelected0);
            }

            if (control == null && m_mapColorSection.containsKey(cw)) {
                m_tfsectionSelected = m_mapColorSection.get(cw);
                control = m_tfsectionSelected.m_func;
            }

            PolylineTransferFunction oldSelection = m_transferSelected;

            // Set the current selection
            m_transferSelected = control;

            if (oldSelection != null && control == null) {
                clearSelection();
            } else if (control != null) {
                m_eventService.publish(new HilitingChgEvent());
            }

            storeCoord(event);
        }

        /**
         * This method is used to find out if a point should be removed.
         *
         * {@inheritDoc}
         *
         * @see MouseListener#mouseClicked(MouseEvent)
         */
        @Override
        public final void mouseClicked(final MouseEvent event) {
            if (m_transferSelected != null && m_pointSelected0 != null
                    && m_pointSelected1 == null) {

                // remove only on right button down
                if (event.getButton() == MouseEvent.BUTTON3) {
                    PolylineTransferFunction temp = removeControlPoint(m_pointSelected0);

                    if (temp != null) {
                        m_eventService.publish(new PointRemovedEvent(temp));
                        clearSelection();
                    }
                }
            }

            storeCoord(event);
        }

        /**
         * This method is uesd to check if a new control point should be added.
         *
         * {@inheritDoc}
         *
         * @see MouseListener#mousePressed(MouseEvent)
         */
        @Override
        public final void mousePressed(final MouseEvent event) {
            if (m_transferSelected != null
                    && event.getButton() == MouseEvent.BUTTON1
                    && !event.isControlDown() && !event.isShiftDown()) {

                // if a line is selected
                if (m_transferSelected != null && m_pointSelected0 == null) {
                    PolylineTransferFunction func = addControlPoint(
                            event.getX(), event.getY());
                    m_eventService.publish(new PointAddedEvent(func));
                }
            }

            storeCoord(event);
        }
    }

    private final int m_selectionThickness = 4;

    // NOT real height and width
    private int m_height;
    private int m_width;

    // the real size of the window
    private int m_widthReal;
    private int m_heightReal;

    private final int m_pointSIZE = 10;
    private final int m_lineSIZE = 5;

    private final Color m_colorBorder = Color.darkGray;
    private final Color m_colorHighlight = Color.lightGray;

    private PolylineTransferFunction.Point m_pointSelected0 = null;
    private PolylineTransferFunction.Point m_pointSelected1 = null;

    private TFSection m_tfsectionSelected = null;

    private PolylineTransferFunction m_transferSelected = null;

    private TransferFunctionBundle m_functions;

    private final Adapter m_adapter = new Adapter();

    // all maps needed for the selection process
    private final Map<ColorWrapper, PolylineTransferFunction.Point> m_mapColorPoint = new HashMap<ColorWrapper, PolylineTransferFunction.Point>();
    private final Map<ColorWrapper, TFSection> m_mapColorSection = new HashMap<ColorWrapper, TFSection>();
    private final Map<PolylineTransferFunction.Point, Color> m_mapPointColor = new HashMap<PolylineTransferFunction.Point, Color>();
    private final Map<PolylineTransferFunction.Point, PolylineTransferFunction> m_mapPointTF = new HashMap<PolylineTransferFunction.Point, PolylineTransferFunction>();

    // get new unique colors here
    private final ColorDispenser m_colorDispenser = new ColorDispenser();

    private EventService m_eventService;

    /**
     * Set up a new Panel in the given Viewer3DNodeMode and with the given
     * EventService.
     *
     * All events concerning the transferfunctions will be propagated using the
     * EventService provided.
     *
     * @param functions
     *            the functions to use
     * @param eventService
     *            the EventService to use
     */
    public TransferFunctionPainter(final TransferFunctionBundle functions,
            final EventService eventService) {
        setEventService(eventService);
        setFunctions(functions);
    }

    /**
     * Set the size to use for drawing.<br>
     *
     * This is vital to set, as this is not a child of component and thus does
     * not know its own size!
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public final void setSize(final int width, final int height) {
        m_widthReal = width;
        m_heightReal = height;

        m_width = width - m_pointSIZE;
        m_height = height - m_pointSIZE;

        // create a new image for the selection detection
        // m_selectionImage =
        // m_graphicsConfiguration.createCompatibleImage(m_widthReal,
        // m_heightReal);
    }

    /**
     * Use this method to paint the transferfunctions.
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     *
     * @param g2
     *            the Graphics2D object to use
     */
    public final void paint(final Graphics2D g2) {
        paint(g2, false);
    }

    /**
     * Use this method to paint the transferfunctions.
     *
     * @see javax.swing.JComponent#paintComponent(Graphics)
     *
     * @param g2
     *            the Graphics2D object to use
     * @param selection
     *            wheter to draw for selection detection
     *
     */
    private void paint(final Graphics2D g2, final boolean selection) {

        // turn on anti aliasing for nicer looks
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // clear if drawing for new selection
        if (selection) {
            m_mapColorSection.clear();
        }

        if (m_functions != null) {
            // paint the functions
            // this must gurantee the correct ordering of the functions so that a
            // selected function is drawn tompost
            for (TransferFunctionColor color : m_functions.getKeys()) {
                PolylineTransferFunction func = (PolylineTransferFunction) m_functions.get(color);

                Color c;
                if (!selection) {
                    c = color.getColor();
                } else {
                    c = null;
                }

                paintLines(g2, func, c, selection);
                paintControlPoints(g2, func, c, selection);
            }
        }
    }

    /**
     * Paint all control points of a given transferfunction.
     *
     * @param g2
     *            the graphics object to use
     * @param function
     *            the function to paint
     * @param color
     *            the color this function should be painted in
     * @param selection
     *            wheter to draw for selection testing or not
     */
    private void paintControlPoints(final Graphics2D g2,
            final PolylineTransferFunction function, final Color color,
            final boolean selection) {

        // paint the points
        for (PolylineTransferFunction.Point p : function.getPoints()) {

            if (!selection) {

                // determine the color of the hightlighting area
                Color outer;

                if (p.getSelected()) {
                    outer = m_colorHighlight;
                } else {
                    outer = m_colorBorder;
                }

                paintPoint(g2, p, outer, color);

            } else {

                // paint everything in the same color for identification
                Color c = m_mapPointColor.get(p);
                paintPoint(g2, p, c, c);
            }
        }
    }

    /**
     * This method actually paints all the points.
     *
     * @param g2
     *            graphics2d
     * @param function
     *            function the point belongs to
     * @param outer
     *            color to use for the highlighting/border painting
     * @param inner
     *            color to use for the acutal point
     */
    private void paintPoint(final Graphics2D g2,
            final PolylineTransferFunction.Point p, final Color outer,
            final Color inner) {

        // correct the frac to fit into the coordinate space of the panel
        double xFrac = p.getX();
        double yFrac = p.getY();
        int x = getXPanelCoordinate(xFrac, true);
        int y = getYPanelCoordinate(yFrac, true);

        int half = m_selectionThickness / 2;

        if (p.getFixed()) {

            g2.setColor(outer);
            g2.fillRect(x - half, y - half, m_pointSIZE + m_selectionThickness,
                    m_pointSIZE + m_selectionThickness);

            g2.setColor(inner);
            g2.fillRect(x, y, m_pointSIZE, m_pointSIZE);
        } else { // paint the control point

            g2.setColor(outer);
            g2.fillOval(x - half, y - half, m_pointSIZE + m_selectionThickness,
                    m_pointSIZE + m_selectionThickness);

            g2.setColor(inner);
            g2.fillOval(x, y, m_pointSIZE, m_pointSIZE);
        }
    }

    /**
     * Paint all lines of a given transferfunction.
     *
     * @param g2
     *            the graphics object to use
     * @param function
     *            the function to paint
     * @param color
     *            the color this function should be painted in
     * @param selection
     *            wheter to draw for selection detection or not
     */
    private void paintLines(final Graphics2D g2,
            final PolylineTransferFunction function, Color color,
            final boolean selection) {

        List<PolylineTransferFunction.Point> list = function.getPoints();

        g2.setStroke(new BasicStroke(m_lineSIZE));
        PolylineTransferFunction.Point p0 = null;
        PolylineTransferFunction.Point p1 = list.get(0);

        for (int i = 1; i < list.size(); i++) {
            p0 = p1;
            p1 = list.get(i);

            int x0 = ((int) (p0.getX() * m_width)) + m_pointSIZE / 2;
            int y0 = ((int) ((1.0 - p0.getY()) * m_height))
                    + m_pointSIZE / 2;
            int x1 = ((int) (p1.getX() * m_width)) + m_pointSIZE / 2;
            int y1 = ((int) ((1.0 - p1.getY()) * m_height))
                    + m_pointSIZE / 2;

            // paint the highlighting
            if (!selection) {
                if (m_transferSelected != null && m_pointSelected0 == null
                        && m_transferSelected.equals(function)
                        && m_tfsectionSelected != null
                        && m_tfsectionSelected.m_p0 == p0
                        && m_tfsectionSelected.m_p1 == p1) {
                    g2.setColor(m_colorHighlight);
                } else {
                    g2.setColor(m_colorBorder);
                }
            } else {
                color = m_colorDispenser.next();
                m_mapColorSection.put(new ColorWrapper(color), new TFSection(
                        function, p0, p1));
                g2.setColor(color);
            }

            g2.setStroke(new BasicStroke(m_lineSIZE + 2));
            g2.drawLine(x0, y0, x1, y1);

            // paint the line
            g2.setColor(color);
            g2.setStroke(new BasicStroke(m_lineSIZE));
            g2.drawLine(x0, y0, x1, y1);
        }
    }

    /**
     * Get the fractional position of the given point relative in the panel.
     *
     * @param x
     *            the x value
     */
    private double getXPointCoordinate(final int x) {
        return (double) x / (double) m_width;
    }

    /**
     * Get the fractional position of the given point relative in the panel.
     *
     * @param y
     *            the y value
     */
    private double getYPointCoordinate(final int y) {
        return 1.0 - ((double) y / (double) m_height);
    }

    /**
     * Get the position of the point in the Panel.
     *
     * If paint = true, the coordinate will correspond to the upper left corner
     * for easy painting, else it will be centered in the object
     *
     * @param frac
     *            the corresponding x Fraction
     * @param paint
     *            wheter or not the coordiante is for painting
     */
    private int getXPanelCoordinate(final double frac, final boolean paint) {
        int x = ((int) (frac * m_width));

        if (paint) {
            return x;
        } else {
            return x + (m_pointSIZE / 2);
        }
    }

    /**
     * Get the position of the point in the Panel.
     *
     * If paint = true, the coordinate will correspond to the upper left corner
     * for easy painting, else it will be centered in the object
     *
     * @param frac
     *            the corresponding y Fraction
     * @param paint
     *            wheter or not the coordiante is for painting
     */
    private int getYPanelCoordinate(final double frac, final boolean paint) {
        int y = ((int) ((1.0 - frac) * m_height));

        if (paint) {
            return y;
        } else {
            return y + (m_pointSIZE / 2);
        }
    }

    /**
     * Convenience method to clear all selection flags.
     */
    private void clearSelection() {
        m_pointSelected0 = null;
        m_pointSelected1 = null;
        m_transferSelected = null;
        m_tfsectionSelected = null;

        for (TransferFunction tf : m_functions.getFunctions()) {

            PolylineTransferFunction func = (PolylineTransferFunction) tf;

            for (PolylineTransferFunction.Point p : func.getPoints()) {
                p.setSelected(false);
            }
        }

        m_eventService.publish(new HilitingClearedEvent());
    }

    /**
     * Move a control point to a new position.
     *
     * @param point
     *            the point to move
     * @param x
     *            the x value
     * @param y
     *            the y value
     */
    private PolylineTransferFunction moveControlPoint(
            final PolylineTransferFunction.Point point, final int x,
            final int y) {
        double xp = getXPointCoordinate(x);
        double yp = getYPointCoordinate(y);
        m_transferSelected.movePoint(point, xp, yp);

        return m_transferSelected;
    }

    /**
     * Move a control point to a new x-position, but keep the y-position.
     *
     * @param point
     *            the point to move
     * @param x
     *            the x value
     */
    private PolylineTransferFunction moveControlPointX(
            final PolylineTransferFunction.Point point, final int x) {
        double xp = getXPointCoordinate(x);
        double yp = point.getY();
        m_transferSelected.movePoint(point, xp, yp);

        return m_transferSelected;
    }

    /**
     * Move a control point to a new y-position, but keep the x-position.
     *
     * @param point
     *            the point to move
     * @param y
     *            the y value
     */
    private PolylineTransferFunction moveControlPointY(
            final PolylineTransferFunction.Point point, final int y) {
        double xp = point.getX();
        double yp = getYPointCoordinate(y);
        m_transferSelected.movePoint(point, xp, yp);

        return m_transferSelected;
    }

    /**
     * Add a new control point.
     *
     * @param x
     *            the x value
     * @param y
     *            the y value
     */
    private PolylineTransferFunction addControlPoint(final int x,
            final int y) {
        double xp = getXPointCoordinate(x);
        double yp = getYPointCoordinate(y);

        // mark the new point as selected
        m_pointSelected0 = m_transferSelected.addPoint(xp, yp);
        m_pointSelected0.setSelected(true);

        // add the new point to the maps
        Color c = m_colorDispenser.next();
        m_mapColorPoint.put(new ColorWrapper(c), m_pointSelected0);
        m_mapPointColor.put(m_pointSelected0, c);
        m_mapPointTF.put(m_pointSelected0, m_transferSelected);

        return m_transferSelected;
    }

    /**
     * Remove a control point from the currently selected TransferFunction.
     *
     * @param p
     *            the point to remove
     */
    private PolylineTransferFunction removeControlPoint(
            final PolylineTransferFunction.Point p) {
        if (m_transferSelected.removePoint(p)) {
            // also remove the point from the maps
            m_mapColorPoint.remove(m_mapPointColor.get(m_pointSelected0));
            m_mapPointColor.remove(m_pointSelected0);
            m_mapPointTF.remove(m_pointSelected0);

            return m_transferSelected;
        } else {
            return null;
        }
    }

    /**
     * @see org.knime.knip.core.ui.event.EventServiceClient#setEventService(EventService)
     */
    public final void setEventService(final EventService eventService) {
        m_eventService = eventService;
    }

    /**
     * Used to set the currently topmost drawn Tranfer function.
     *
     * @param color
     *            the name of the function to draw
     */
    public final void setTransferFocus(final TransferFunctionColor color) {
        m_functions.moveToLast(color);
    }

    /**
     * Set the functions to draw.
     *
     * @param functions
     *            the functions.
     */
    public final void setFunctions(final TransferFunctionBundle functions) {
        m_functions = functions;

        if (m_functions == null) {
            return;
        }

        for (TransferFunction tf : m_functions.getFunctions()) {
            if (tf.getClass() != PolylineTransferFunction.class)
                throw new IllegalArgumentException("Currently only implemented for Viewer3DNodeTransferFunction, but this type is " + tf.getClass());
        }


        // make sure that only PolylineFunctions are present
        for (TransferFunction tf : m_functions) {
            if (tf.getClass() != PolylineTransferFunction.class)
                throw new IllegalArgumentException("This class only works for PolylineTransferFunctions, but class of function is " + tf.getClass());
        }

        // clear the HashMaps and then readd everything
        m_mapColorPoint.clear();
        m_mapPointColor.clear();
        m_mapPointTF.clear();

        for (TransferFunction tf : m_functions) {
            PolylineTransferFunction f = (PolylineTransferFunction) tf;
            for (PolylineTransferFunction.Point p : f) {
                Color cp = m_colorDispenser.next();
                m_mapColorPoint.put(new ColorWrapper(cp), p);
                m_mapPointColor.put(p, cp);
                m_mapPointTF.put(p, f);
            }
        }
    }

    /**
     * Gets the mouse adapter for this instance.
     *
     * @return The mouse adapter.
     */
    public final Adapter getMouseAdapter() {
        return m_adapter;
    }

}
