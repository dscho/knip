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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import mpicbg.ij.integral.Scale;


/**
 * This class displays and allows the manipulation of Transferfunctions.
 *
 * @author Clemens Müthing (clemens.muething@uni-konstanz.de)
 */
public class TransferFunctionPanel extends JPanel implements TransferFunctionChgListener, ChangeListener {

        private final EventListenerList m_listener = new EventListenerList();

        /* the bundle we are currently drawing */
        private TransferFunctionBundle m_bundle = null;

        /* the current histogram */
        private HistogramWithNormalization m_histogram;
        private HistogramWithNormalization m_histogramNormalized;

        /* controls wheter functions should be displayed normalized */
        private boolean m_normalize = false;

        private final HistogramPainter m_histogramPainter = new HistogramPainter();
        private final TransferFunctionBundlePainter m_tfPainter = new TransferFunctionBundlePainter();

        private final Dimension m_preferredSize = new Dimension(250, 150);

        /**
         * Set up a new Panel displaying a bundle of transfer functions.
         */
        public TransferFunctionPanel() {

                addMouseListener(m_tfPainter);
                addMouseMotionListener(m_tfPainter);

                m_tfPainter.addTransferFunctionChgListener(this);
                m_tfPainter.addChangeListener(this);
        }

        @Override
        public final void paintComponent(final Graphics g) {

                // call super for painting background etc
                super.paintComponent(g);
                final Graphics2D g2 = (Graphics2D) g.create();

                m_histogramPainter.paint(g2);
                m_tfPainter.paint(g2);
        }

        /**
         * Set the new data for the histogram.<br>
         *
         * @param data the new data
         */
        public final void setData(final int[] data) {
                if (data == null) {
                        setHistogram(null);
                } else {
                        setHistogram(new HistogramWithNormalization(data));
                }
        }

        /**
         * Set the new histogram that should be used for painting.<br>
         *
         * @param hist the new histogram
         */
        public final void setHistogram(final HistogramWithNormalization hist) {

                m_histogram = hist;
                if (m_histogram != null) {
                        m_histogramNormalized = new HistogramWithNormalization(m_histogram.getNormalizedData());
                } else {
                        m_histogramNormalized = null;
                }

                setHistogram();

                normalizeFunctions();

                repaint();
        }

        /**
         * @see HistogramPainter#setScale(Scale)
         * @param scale
         *                the new scale
         */
        public final void setScale(final HistogramPainter.Scale scale) {
                m_histogramPainter.setScale(scale);
                repaint();
        }

        /**
         * @see TransferFunctionBundlePainter#setTransferFocus(String)
         * @param color
         *                the color of the function to draw topmost
         */
        public final void setTransferFocus(final TransferFunctionColor color) {
                m_tfPainter.setTransferFocus(color);
                repaint();
        }

        /**
         * @see TransferFunctionBundlePainter#setFunctions(TransferFunctionBundle)
         * @param bundle
         *                the bundle of functions to display
         */
        public final void setBundle(final TransferFunctionBundle bundle) {
                if (bundle == null) throw new NullPointerException();

                m_bundle = bundle;

                normalizeFunctions();

                repaint();
        }

        private void setHistogram() {
                if (m_normalize) {
                        m_histogramPainter.setHistogram(m_histogramNormalized);
                } else {
                        m_histogramPainter.setHistogram(m_histogram);
                }
        }

        private void normalizeFunctions() {
                double[] frac = new double[] {0, 1};

                if (m_normalize && m_histogram != null) {
                        frac = m_histogram.getFractions();
                }

                if (m_bundle != null) {
                        for (TransferFunction tf : m_bundle) {
                                tf.zoom(frac[0], frac[1]);
                        }
                }

                m_tfPainter.setBundle(m_bundle);
        }

        public final void normalize(final boolean value) {

                m_normalize = value;

                setHistogram();

                normalizeFunctions();

                repaint();
        }


        @Override
        public void transferFunctionChg(final TransferFunctionChgEvent event) {
                for (TransferFunctionChgListener l : m_listener.getListeners(TransferFunctionChgListener.class)) {
                        l.transferFunctionChg(event);
                }

                repaint();
        }

        public void addTransferFunctionChgListener(final TransferFunctionChgListener l) {
                m_listener.add(TransferFunctionChgListener.class, l);
        }

        public void removeTransferFunctionChgListener(
                        final TransferFunctionChgListener l) {
                m_listener.remove(TransferFunctionChgListener.class, l);
        }

        @Override
        public void stateChanged(final ChangeEvent e) {
                setCursor(m_tfPainter.getCursor());
                repaint();
        }

        @Override
        public Dimension getMinimumSize() {
                return m_preferredSize;
        }
}
