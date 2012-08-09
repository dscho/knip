/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *
 * History
 *   11 Feb 2010 (hornm): created
 */
package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;

import net.imglib2.Interval;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.type.Type;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ForcePlanePosEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.PlaneSelectionEvent;

/**
 * Allows the user to select a plane in a multdimensional space.
 *
 * Publishes {@link PlaneSelectionEvent}
 *
 *
 * @author dietzc, hornm
 * @param <T>
 *                image type
 */
@SuppressWarnings("serial")
public class PlaneSelectionPanel<T extends Type<T>, I extends Interval> extends
                ViewerComponent {

        private JScrollBar[] m_scrollBars;

        private JScrollBar m_totalSlider;

        // private JComboBox[] m_planeFields;
        private JCheckBox[] m_planeCheckBoxes;

        private JFormattedTextField[] m_coordinateTextFields;

        /* the plane dimension indices */
        private int m_dimX;

        private int m_dimY;

        /* the steps to switch to the subsequent coordinate in one dimension */
        private int[] m_steps;

        /* the dimension sizes */
        private long[] m_dimSizes;

        /* recognizes which dimension to alter next */
        private int m_alterDim;

        private I m_img;

        private CalibratedSpace m_axesLabels;

        private EventService m_eventService;

        private boolean m_isAdjusting;

        private long[] m_oldCoordinates;

        // private JTextField m_totalField;

        public PlaneSelectionPanel() {
                super("Plane selection", false);
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                // empty panel that ensures a minimum width
                // setting the same thing on this has bad impacts on the
                // component height
                JPanel wider = new JPanel();
                wider.setMaximumSize(new Dimension(200,
                                wider.getMaximumSize().height));
                wider.setPreferredSize(new Dimension(200, wider
                                .getPreferredSize().height));
                wider.setMinimumSize(new Dimension(200,
                                wider.getMinimumSize().height));
                add(wider);

                setMaximumSize(new Dimension(200, getMaximumSize().height));

        }

        /**
         * @param dimX
         *                the first dimension index
         * @param dimY
         *                the second dimension index
         */
        private void setPlaneDimensionIndices(int dimX, int dimY) {
                m_isAdjusting = true;

                m_scrollBars[m_dimX].setEnabled(true);
                m_scrollBars[m_dimY].setEnabled(true);
                m_planeCheckBoxes[m_dimX].setSelected(false);
                m_planeCheckBoxes[m_dimY].setSelected(false);
                m_planeCheckBoxes[m_dimX].setEnabled(true);
                m_planeCheckBoxes[m_dimY].setEnabled(true);

                // m_planeFields[m_dimY].setEditable(false);
                // m_planeFields[m_dimY].setEditable(false);
                // m_planeFields[m_dimX].setEnabled(true);
                // m_planeFields[m_dimY].setEnabled(true);

                m_dimX = dimX;
                m_dimY = dimY;
                m_scrollBars[dimX].setEnabled(false);
                m_scrollBars[dimY].setEnabled(false);
                m_planeCheckBoxes[m_dimX].setSelected(true);
                m_planeCheckBoxes[m_dimY].setSelected(true);
                m_planeCheckBoxes[m_dimX].setEnabled(false);
                m_planeCheckBoxes[m_dimY].setEnabled(false);
                //
                // m_planeFields[m_dimY].setEditable(true);
                // m_planeFields[m_dimY].setEditable(true);
                // m_planeFields[m_dimX].setEnabled(false);
                // m_planeFields[m_dimY].setEnabled(false);

                m_isAdjusting = false;

                // calculate the steps to step forward in the linear array
                boolean first = true;
                int lasti = 0;
                for (int i = 0; i < m_dimSizes.length; i++) {
                        if (i == dimX || i == dimY) {
                                m_coordinateTextFields[i].setEnabled(false);
                        } else {
                                if (first) {
                                        m_steps[i] = 1;
                                        lasti = i;
                                        first = false;
                                } else {
                                        m_steps[i] = m_steps[lasti]
                                                        * (int) m_dimSizes[lasti];
                                        lasti = i;
                                }
                                m_coordinateTextFields[i].setEnabled(true);
                        }
                }

                // maximum index
                int max = 1;
                for (int i = 0; i < m_dimSizes.length; i++) {
                        if (i == dimX || i == dimY)
                                continue;
                        max *= m_dimSizes[i];
                }

                m_isAdjusting = true;

                m_totalSlider.setVisibleAmount(1);
                m_totalSlider.setValue(m_totalSlider.getValue() <= max ? m_totalSlider
                                .getValue() : 1);
                m_totalSlider.setMaximum(max);
                // for (int i = 1; i < max + 1; i++)
                // m_totalField.addItem(new Integer(i));

                // m_totalField.setSelectedIndex(0);

                m_totalSlider.setEnabled(max > 1);
                // m_totalField.setEnabled(max > 1);

                setEnabled(max > 1);
                m_isAdjusting = false;
        }

        // -- ChangeListener API methods --

        /** Handles slider events. */
        private void onSliderChanged(final int id) {

                if (m_isAdjusting)
                        return;

                if (id == -1) {
                        // m_totalField.setText(String.valueOf(m_totalSlider.getValue()
                        // +
                        // 1));
                        updateDimSliders();
                } else {
                        // m_planeFields[id].setSelectedIndex(m_scrollBars[id].getValue()
                        // +
                        // 1);
                        updateTotalSlider();
                }

                // test if the slider positions changed
                boolean change = false;
                long[] imgCoords = getImageCoordinate();
                if (m_oldCoordinates != null
                                && imgCoords.length == m_oldCoordinates.length) {
                        for (int i = 0; i < imgCoords.length; i++) {
                                if (imgCoords[i] != m_oldCoordinates[i]) {
                                        change = true;
                                        break;
                                }
                        }
                } else {
                        change = true;
                }

                m_oldCoordinates = imgCoords;

                if (change) {
                        for (int i = 0; i < m_dimSizes.length; i++) {
                                m_coordinateTextFields[i]
                                                .setValue(m_scrollBars[i]
                                                                .getValue());
                        }

                        m_eventService.publish(new PlaneSelectionEvent(Math
                                        .min(m_dimX, m_dimY), Math.max(m_dimY,
                                        m_dimX), imgCoords));
                        m_eventService.publish(new ImgRedrawEvent());
                }
        }

        // private void onTextFieldChange(ActionEvent e, int id) {
        //
        // if (m_isAdjusting
        // || e.getActionCommand().equalsIgnoreCase("comboBoxEdited"))
        // return;
        // try {
        //
        // int newVal = Integer.valueOf(String.valueOf(((JTextField) e
        // .getSource()).getText())) - 1;
        //
        // if (id == -1) {
        // if (newVal >= m_totalSlider.getMaximum()) {
        // newVal = m_totalSlider.getMaximum() - 1;
        // }
        // m_totalSlider.setValue(newVal);
        // updateDimSliders();
        // } else {
        // if (newVal >= m_scrollBars[id].getMaximum()) {
        // newVal = m_scrollBars[id].getMaximum() - 1;
        // }
        // m_scrollBars[id].setValue(newVal);
        // updateTotalSlider();
        // }
        // m_eventService.publish(
        // EventType.PLANESELECTION_VALUES_CHG,
        // new PlaneSelection(Math.min(m_dimX, m_dimY), Math.max(
        // m_dimY, m_dimX), getImageCoordinate()));
        //
        // } catch (NumberFormatException exp) {
        // JOptionPane.showMessageDialog(null, "Only numbers are allowed",
        // "Please enter a number", JOptionPane.ERROR_MESSAGE);
        // }
        //
        // }

        /**
         *
         * @param e
         * @param id
         */
        private void onCheckBoxChange(ItemEvent e, int id) {

                if (m_isAdjusting)
                        return;

                int idx = Integer.parseInt(((JCheckBox) e.getSource())
                                .getActionCommand());

                if (m_alterDim == 0) {
                        setPlaneDimensionIndices(idx, m_dimY);
                } else {
                        setPlaneDimensionIndices(m_dimX, idx);
                }
                m_alterDim = (m_alterDim + 1) % 2;
                m_eventService.publish(new PlaneSelectionEvent(Math.min(m_dimX,
                                m_dimY), Math.max(m_dimY, m_dimX),
                                getImageCoordinate()));
                m_eventService.publish(new ImgRedrawEvent());

        }

        /* Gets the index of the currently displayed image. */
        private void updateTotalSlider() {
                // calc index
                int index = 0;
                for (int i = 0; i < m_steps.length; i++) {
                        if (i == m_dimX || i == m_dimY)
                                continue;
                        index += m_steps[i] * (m_scrollBars[i].getValue());
                }
                // update index
                if (index >= 0) {
                        m_isAdjusting = true;
                        // m_totalField.setText("" + index + 1);
                        m_totalSlider.setValue(index);
                        m_isAdjusting = false;
                }
        }

        private void updateDimSliders() {
                // calc logical coordinates
                int[] coords = new int[m_scrollBars.length];
                int idx = m_totalSlider.getValue();

                for (int i = coords.length - 1; i > -1; i--) {
                        if (i == m_dimX || i == m_dimY)
                                continue;
                        coords[i] = idx / m_steps[i];
                        idx = idx % m_steps[i];
                }
                // update coordinates
                for (int i = 0; i < coords.length; i++) {
                        if (i == m_dimX || i == m_dimY)
                                continue;
                        m_isAdjusting = true;
                        // m_planeFields[i].setSelectedIndex(coords[i]);
                        m_scrollBars[i].setValue(coords[i]);
                        m_isAdjusting = false;
                }
        }

        /**
         *
         * @return the coordinates of the currently selected image (a newly
         *         generated array)
         */
        protected long[] getImageCoordinate() {
                if (m_scrollBars == null)
                        return new long[m_img.numDimensions()];
                long[] res = new long[m_img.numDimensions()];
                for (int i = 0; i < res.length; i++) {
                        res[i] = m_scrollBars[i].getValue();
                }
                return res;
        }

        public void onViewClosed() {
                //
        }

        /**
         * @param name
         */
        @EventListener
        public void onImgUpdated(IntervalWithMetadataChgEvent<I> e) {
                m_axesLabels = e.getCalibratedSpace();
                m_img = e.getInterval();
                draw();
                for (int i = 0; i < m_dimSizes.length; i++) {
                        m_coordinateTextFields[i].setValue(m_scrollBars[i]
                                        .getValue());
                }
        }

        @EventListener
        public void onFocusLabel(ForcePlanePosEvent e) {
                m_isAdjusting = true;
                for (int d = 0; d < e.getPosition().length; d++) {

                        if (d == m_dimX || d == m_dimY)
                                m_scrollBars[d].setValue(0);
                        else
                                m_scrollBars[d].setValue((int) e.getPosition()[d]);
                }
                updateTotalSlider();
                onSliderChanged(0);
        }

        private void draw() {

                if (m_img != null && m_axesLabels != null) {

                        m_dimSizes = new long[m_img.numDimensions()];
                        m_img.dimensions(m_dimSizes);
                        m_steps = new int[m_dimSizes.length];

                        removeAll();
                        JPanel nPanel = new JPanel();
                        nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.X_AXIS));
                        add(nPanel);
                        add(Box.createVerticalStrut(2));
                        m_totalSlider = new JScrollBar(JScrollBar.HORIZONTAL);
                        Dimension dim = m_totalSlider.getPreferredSize();
                        dim.width = 150;
                        m_totalSlider.setPreferredSize(dim);
                        // m_totalField = new JTextField();
                        //
                        // m_totalField.setEditable(true);
                        // m_totalField.setMinimumSize(new Dimension(10,
                        // m_totalField
                        // .getPreferredSize().height));
                        // m_totalField.setMaximumSize(new Dimension(45,
                        // m_totalField
                        // .getPreferredSize().height));
                        // m_totalField.setPreferredSize(new Dimension(45,
                        // m_totalField
                        // .getPreferredSize().height));

                        // m_totalField.addActionListener(new
                        // ActionListenerWithId(-1));
                        // m_totalField.setEnabled(false);
                        m_totalSlider.addAdjustmentListener(new ChangeListenerWithId(
                                        -1));
                        nPanel.add(new JLabel("N"));
                        nPanel.add(Box.createHorizontalStrut(3));
                        nPanel.add(m_totalSlider);
                        // nPanel.add(m_totalField);

                        // add key bindings to the JTextArea
                        int condition = JScrollBar.WHEN_IN_FOCUSED_WINDOW;
                        InputMap inMap = getInputMap(condition);
                        ActionMap actMap = getActionMap();

                        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0),
                                        "FORWARD");
                        inMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0),
                                        "BACKWARD");
                        actMap.put("FORWARD", new ForwardBackwardAction(
                                        "FORWARD", m_totalSlider, 1));
                        actMap.put("BACKWARD", new ForwardBackwardAction(
                                        "BACKWARD", m_totalSlider, 1));

                        JPanel dimPanel = new JPanel();
                        dimPanel.setLayout(new BoxLayout(dimPanel,
                                        BoxLayout.Y_AXIS));
                        add(dimPanel);

                        m_scrollBars = new JScrollBar[m_dimSizes.length];
                        m_planeCheckBoxes = new JCheckBox[m_dimSizes.length];
                        m_coordinateTextFields = new JFormattedTextField[m_dimSizes.length];

                        // m_planeFields = new JComboBox[m_dimSizes.length];
                        JPanel sliderPanel;
                        for (int i = 0; i < m_dimSizes.length; i++) {
                                sliderPanel = new JPanel();
                                sliderPanel.setLayout(new BoxLayout(
                                                sliderPanel, BoxLayout.X_AXIS));
                                m_scrollBars[i] = new JScrollBar(
                                                JScrollBar.HORIZONTAL);
                                m_planeCheckBoxes[i] = new JCheckBox("", false);
                                m_planeCheckBoxes[i]
                                                .addItemListener(new ItemListenerWithId(
                                                                i));
                                m_planeCheckBoxes[i].setActionCommand(i + "");

                                // m_planeFields[i] = new JComboBox();
                                // m_planeFields[i].setEditable(true);
                                //
                                // m_planeFields[i].addActionListener(new
                                // ActionListenerWithId(i));
                                // m_planeFields[i].setPreferredSize(new
                                // Dimension(55,
                                // m_planeFields[i].getPreferredSize().height));
                                // m_planeFields[i].setEditable(true);

                                dim = m_scrollBars[i].getPreferredSize();
                                dim.width = 150;
                                m_scrollBars[i].setPreferredSize(dim);
                                m_scrollBars[i].setValue(m_scrollBars[i]
                                                .getValue() < m_dimSizes[i] ? m_scrollBars[i]
                                                .getValue() : 0);
                                m_scrollBars[i].setMinimum(0);
                                m_scrollBars[i].setMaximum((int) m_dimSizes[i]);

                                m_scrollBars[i].setEnabled(m_dimSizes[i] > 1);
                                m_scrollBars[i].setVisibleAmount(1);
                                m_scrollBars[i].addAdjustmentListener(new ChangeListenerWithId(
                                                i));

                                sliderPanel.add(m_axesLabels != null ? (new JLabel(
                                                m_axesLabels.axis(i).getLabel()))
                                                : (new JLabel("" + i)));

                                sliderPanel.add(Box.createHorizontalStrut(3));
                                sliderPanel.add(m_scrollBars[i]);

                                // add coordinate text fields
                                NumberFormat nf = DecimalFormat.getInstance();
                                nf.setGroupingUsed(false);
                                JFormattedTextField tmp = new JFormattedTextField(
                                                nf);
                                tmp.setMinimumSize(new Dimension(40, tmp
                                                .getMinimumSize().height));
                                tmp.setPreferredSize(new Dimension(40, tmp
                                                .getPreferredSize().height));
                                tmp.setMaximumSize(new Dimension(40, tmp
                                                .getPreferredSize().height));

                                final int index = i;
                                tmp.addActionListener(new ActionListener() {

                                        @Override
                                        public void actionPerformed(
                                                        ActionEvent e) {
                                                textCoordinatesChanged(index);
                                        }
                                });

                                tmp.addFocusListener(new FocusListener() {

                                        @Override
                                        public void focusLost(FocusEvent arg0) {
                                                textCoordinatesChanged(index);
                                        }

                                        @Override
                                        public void focusGained(FocusEvent e) {
                                        }

                                });

                                m_coordinateTextFields[i] = tmp;
                                sliderPanel.add(m_coordinateTextFields[i]);
                                sliderPanel.add(m_planeCheckBoxes[i]);
                                // sliderPanel.add(m_planeFields[i]);
                                sliderPanel.add(Box.createHorizontalStrut(7));
                                dimPanel.add(sliderPanel);

                        }

                        setPlaneDimensionIndices(0, 1);
                }

                updateUI();
        }

        private void textCoordinatesChanged(int fieldIndex) {
                int value = Integer.valueOf(m_coordinateTextFields[fieldIndex]
                                .getText());
                if (value != m_scrollBars[fieldIndex].getValue()) {
                        if (value < m_scrollBars[fieldIndex].getMinimum()) {
                                m_coordinateTextFields[fieldIndex]
                                                .setText(String.valueOf(m_scrollBars[fieldIndex]
                                                                .getMinimum()));
                        } else if (value > m_scrollBars[fieldIndex]
                                        .getMaximum()) {
                                m_coordinateTextFields[fieldIndex]
                                                .setText(String.valueOf(m_scrollBars[fieldIndex]
                                                                .getMaximum()));
                        }
                        // triggers also the necessary events
                        m_scrollBars[fieldIndex].setValue(value);
                }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
                return Position.SOUTH;
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
                // Nothing to do here
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
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

        private class ItemListenerWithId implements ItemListener {

                private final int m_id;

                public ItemListenerWithId(int id) {
                        m_id = id;
                }

                @Override
                public void itemStateChanged(ItemEvent e) {
                        onCheckBoxChange(e, m_id);

                }

        }

        private class ChangeListenerWithId implements AdjustmentListener {

                private final int m_id;

                public ChangeListenerWithId(int id) {
                        m_id = id;
                }

                @Override
                public void adjustmentValueChanged(AdjustmentEvent e) {
                        onSliderChanged(m_id);
                }

        }

        // Action for our key binding to perform when bound event occurs
        private class ForwardBackwardAction extends AbstractAction {
                private final JScrollBar slider;

                private final int scrollableIncrement;

                public ForwardBackwardAction(String name, JScrollBar slider,
                                int scrollableIncrement) {
                        super(name);
                        this.slider = slider;
                        this.scrollableIncrement = scrollableIncrement;
                }

                @Override
                public void actionPerformed(ActionEvent ae) {
                        String name = getValue(AbstractAction.NAME).toString();
                        int value = slider.getValue();
                        if (name.equals("FORWARD")) {
                                value += scrollableIncrement;
                                if (value >= m_totalSlider.getMaximum())
                                        return;

                                slider.setValue(value);
                        } else if (name.equals("BACKWARD")) {
                                value -= scrollableIncrement;
                                if (value < 0)
                                        return;
                                slider.setValue(value);
                        }
                }
        }

}
