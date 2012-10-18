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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import mpicbg.ij.integral.Scale;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
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
 */
public class TransferFunctionViewer extends ViewerComponent {

        private EventService m_eventService;

        private TransferFunctionBundle m_functions;

        private final HistogramPainter m_histogram;
        private final TransferFunctionPainter m_tfDrawer;

        private final Cursor m_moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        private final Cursor m_defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);

        /**
         * Set up a new Panel displaying the given transfer functions.
         *
         * @param bundle
         *                the bundle of transfer functions to display
         * @param eventService
         *                the EventService to use
         * @param data
         *                the data for the histogram background
         */
        public TransferFunctionViewer(final EventService eventService) {
                super("Color Transfer Picker", true);

                setEventService(eventService);

                // set up the histogram
                m_histogram = new HistogramPainter();

                // Set up the TFDrawer
                EventService componentService = new EventService();
                componentService.subscribe(this);
                m_tfDrawer = new TransferFunctionPainter(m_functions,
                                componentService);

                // listen to mouseEvents
                addMouseListener(m_tfDrawer.getMouseAdapter());
                addMouseMotionListener(m_tfDrawer.getMouseAdapter());

                addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentResized(final ComponentEvent e) {
                                m_tfDrawer.setSize(getWidth(), getHeight());
                                repaint();
                        }
                });
        }

        /**
         * {@inheritDoc}
         *
         * @see javax.swing.JComponent#paintComponent(Graphics)
         */
        @Override
        public final void paintComponent(final Graphics g) {

                int width = getWidth();
                int height = getHeight();

                // call super for painting background etc
                super.paintComponent(g);
                final Graphics2D g2 = (Graphics2D) g.create();

                // turn on anti aliasing for nicer looks
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

                // paint the histogram in the background
                m_histogram.paintHistogram(g2, width, height);

                // paint the transfer functions
                m_tfDrawer.paint(g2);
        }

        /**
         * {@inheritDoc}
         *
         * @see org.knime.knip.core.ui.event.EventServiceClient#setEventService(EventService)
         */
        @Override
        public final void setEventService(final EventService eventService) {
                if (m_eventService == null) {
                        m_eventService = new EventService();
                } else {
                        m_eventService = eventService;
                }
                m_eventService.subscribe(this);
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#setParent(Component)
         */
        @Override
        public void setParent(final Component parent) {
                // Ignore this method
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#getPosition()
         */
        @Override
        public final Position getPosition() {
                // not used
                return null;
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#reset()
         */
        @Override
        public void reset() {
                // not used
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#saveComponentConfiguration(ObjectOutput)
         */
        @Override
        public void saveComponentConfiguration(final ObjectOutput out)
                        throws IOException {
                // not used
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#loadComponentConfiguration(ObjectInput)
         */
        @Override
        public void loadComponentConfiguration(final ObjectInput in)
                        throws IOException, ClassNotFoundException {
                // not used
        }

        /**
         * @see HistogramPainter#setData(int[])
         * @param data
         *                the new data
         */
        public final void setData(final int[] data) {
                m_histogram.setData(data);
        }

        /**
         * @see HistogramPainter#setScale(Scale)
         * @param scale
         *                the new scale
         */
        public final void setScale(final HistogramPainter.Scale scale) {
                m_histogram.setScale(scale);

                // repaint
                repaint();
        }

        /**
         * @see TransferFunctionPainter#setTransferFocus(String)
         * @param color
         *                the name of the function to draw topmost
         */
        public final void setTransferFocus(final TransferFunctionColor color) {
                m_tfDrawer.setTransferFocus(color);
                repaint();
        }

        /**
         * @see TransferFunctionPainter#setFunctions(TransferFunctionBundle)
         * @param functions
         *                the bundle of functions to display
         */
        public final void setFunctions(final TransferFunctionBundle functions) {
                m_functions = functions;
                normalizeFunctions();
        }

        public final void normalizeFunctions() {
                double[] frac = m_histogram.getNormalizationFractions();

                for (TransferFunction tf : m_functions) {
                        tf.zoom(frac[0], frac[1]);
                }

                // update the drawing info in the drawer
                m_tfDrawer.setFunctions(m_functions);

                repaint();
        }

        @EventListener
        public final void onNormalizationChg(final NormalizationChgEvent e) {
                m_histogram.setNormalize(e.normalize());
                normalizeFunctions();

                m_eventService.publish(new NormalizationPerformedEvent(e
                                .normalize()));
        }

        /**
         * Called when an element of the bundle has been highlighted.
         */
        @EventListener
        public final void onHighlightingChanged(HilitingChgEvent e) {
                setCursor(m_moveCursor);
                repaint();
        }

        /**
         * Called when nothing should be highlighted.
         */
        @EventListener
        public final void onHighlightingCleared(HilitingClearedEvent e) {
                setCursor(m_defaultCursor);
                repaint();
        }

        /**
         * Called when a point is added.
         *
         * @param func
         *                the function to which the point has been added
         */
        @EventListener
        public final void onPointAdded(PointAddedEvent e) {
                transferFunctionChanged(e.getFunc());
        }

        /**
         * Called when a point is removed.
         *
         * @param func
         *                the function to which the point has been removed
         */
        @EventListener
        public final void onPointRemoved(PointRemovedEvent e) {
                transferFunctionChanged(e.getFunc());
        }

        /**
         * Called when a point is moved.
         *
         * @param func
         *                the function of which the point has been moved
         */
        @EventListener
        public final void onPointMoved(PointMovedEvent e) {
                transferFunctionChanged(e.getFunc());
        }

        /**
         * Called whenever a transfer function changes.
         *
         * This method than forwards the change and adds the bundle to which the
         * changed functions belongs to the event.
         */
        private void transferFunctionChanged(final PolylineTransferFunction func) {
                repaint();
                m_eventService.publish(new TransferFuncChgEvent(func,
                                m_functions));
        }
}
