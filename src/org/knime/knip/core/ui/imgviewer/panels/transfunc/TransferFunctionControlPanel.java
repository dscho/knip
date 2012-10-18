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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.LayoutStyle;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.transfunc.TransferFunctionBundle;
import org.knime.knip.core.ui.transfunc.TransferFunctionColor;

/**
 * A Panel used to control the Transferfunctions and the actually drawn values
 * of the image to be shown.
 *
 * @author Clemens Müthing (clemens.muething@uni-konstanz.de)
 */
public class TransferFunctionControlPanel extends ViewerComponent {

        public final class Memento {

                private HistogramPainter.Scale scale;
                private TransferFunctionBundle currentBundle;
                private Map<TransferFunctionBundle, TransferFunctionColor> map;
                private int[] histogramData;

        }

        /**
         * Eclipse generated.
         */
        private static final long serialVersionUID = -2037066021463028825L;

        /**
         * This class is used to wrap some necessary information about the
         * bundles.
         */

        /**
         * The Adapter for the Scale ComboBox.
         */
        private class ScaleAdapter implements ActionListener {

                @Override
                public void actionPerformed(final ActionEvent event) {
                        m_transferPicker.setScale((HistogramPainter.Scale) m_scaleBox
                                        .getSelectedItem());
                }
        }

        /**
         * The Adapter for the Focus ComboBox.
         */
        private class FocusAdapter implements ActionListener {

                @Override
                public void actionPerformed(final ActionEvent event) {

                        TransferFunctionColor color = (TransferFunctionColor) m_focusBox
                                        .getSelectedItem();

                        m_transferPicker.setTransferFocus(color);
                        m_bundleMap.put(m_currentBundle, color);
                }
        }

        /**
         * The Adapter for the Color ComboBox.
         */
        private class ColorModeAdapter implements ActionListener {

                @Override
                public void actionPerformed(final ActionEvent event) {

                        TransferFunctionBundle bundle = (TransferFunctionBundle) m_bundleBox
                                        .getSelectedItem();
                        setActiveBundle(bundle);
                        m_eventService.publish(new BundleChgEvent(bundle));

                }
        }

        private Memento m_memento;

        private final TransferFunctionViewer m_transferPicker;

        private EventService m_eventService;

        private final JComboBox m_bundleBox;
        private final JComboBox m_scaleBox;
        private final JComboBox m_focusBox;

        private Map<TransferFunctionBundle, TransferFunctionColor> m_bundleMap = new HashMap<TransferFunctionBundle, TransferFunctionColor>();
        private TransferFunctionBundle m_currentBundle;

        private final JCheckBox m_boxNormalize;
        private final JCheckBox m_boxForce;
        private final JCheckBox m_boxAutoApply;
        private final JButton m_buttonApply;

        private final Dimension m_preferredSize = new Dimension(400, 200);

        // Save this adapter to unset/set while changing content
        private final ActionListener m_focusAdapter;
        private final ActionListener m_bundleAdapter;

        /**
         * Construct a new TransferPanel and use a fresh EventService.
         */
        public TransferFunctionControlPanel() {
                this(new EventService());
        }

        /**
         * Sets up a new TransferPanel and hooks the histogram and
         * Transferfunction Viewer to the given EventService.
         *
         * @param service
         *                The {@link EventService} to be used
         */
        public TransferFunctionControlPanel(final EventService service) {
                super("Transfer Function", false);

                setEventService(service);

                // Set up the Comboboxes
                m_scaleBox = new JComboBox(HistogramPainter.Scale.values());
                m_bundleBox = new JComboBox();
                m_focusBox = new JComboBox();

                // Set up the viewers
                m_transferPicker = new TransferFunctionViewer(m_eventService);

                // set up the checkboxes and the button
                m_boxForce = new JCheckBox("Force this settings");
                m_boxForce.addActionListener(new ActionListener() {

                        @Override
                        public final void actionPerformed(
                                        final ActionEvent event) {
                                m_eventService.publish(new ForceChgEvent(
                                                m_boxForce.isSelected()));
                        }
                });

                m_boxAutoApply = new JCheckBox("Autoapply changes");
                m_boxAutoApply.setSelected(true);
                m_boxAutoApply.addActionListener(new ActionListener() {

                        @Override
                        public final void actionPerformed(
                                        final ActionEvent event) {
                                m_eventService.publish(new ApplyChgEvent(
                                                m_boxAutoApply.isSelected()));
                        }
                });

                m_boxNormalize = new JCheckBox("Normalize");
                m_boxNormalize.setSelected(false);
                m_boxNormalize.addActionListener(new ActionListener() {

                        @Override
                        public final void actionPerformed(
                                        final ActionEvent event) {
                                m_eventService.publish(new NormalizationChgEvent(
                                                m_boxNormalize.isSelected()));
                        }
                });

                m_buttonApply = new JButton("Apply");
                m_buttonApply.addActionListener(new ActionListener() {
                        @Override
                        public final void actionPerformed(
                                        final ActionEvent event) {
                                m_eventService.publish(new ApplyEvent());
                        }
                });

                // create the layout
                GroupLayout layout = new GroupLayout(this);
                setLayout(layout);

                Component glue = Box.createHorizontalGlue();

                // Set up the layout
                // find the box with the longest Text
                int width = getLongestComboBox();

                // the subgroups to be added to the main groups
                // numbers are colums/rows respectivly
                // use fixed size horizontally
                GroupLayout.ParallelGroup box0 = layout.createParallelGroup()
                                .addComponent(m_boxForce)
                                .addComponent(m_boxAutoApply);

                GroupLayout.SequentialGroup boxgroup = layout
                                .createSequentialGroup()
                                .addComponent(m_boxNormalize)
                                .addComponent(glue).addGroup(box0);

                GroupLayout.ParallelGroup horizontal0 = layout
                                .createParallelGroup()
                                .addComponent(m_transferPicker)
                                .addGroup(boxgroup);

                GroupLayout.ParallelGroup horizontal1 = layout
                                .createParallelGroup()
                                .addComponent(m_scaleBox, width, width, width)
                                .addComponent(m_bundleBox, width, width, width)
                                .addComponent(m_focusBox, width, width, width)
                                .addComponent(m_buttonApply);

                // do not stretch vertically
                GroupLayout.SequentialGroup verticalButtons = layout
                                .createSequentialGroup()
                                .addComponent(m_scaleBox,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_bundleBox,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(m_focusBox,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.PREFERRED_SIZE);

                GroupLayout.ParallelGroup vertical0 = layout
                                .createParallelGroup()
                                .addComponent(m_transferPicker)
                                .addGroup(verticalButtons);

                GroupLayout.ParallelGroup vertical1 = layout
                                .createParallelGroup()
                                .addComponent(m_boxNormalize)
                                .addComponent(glue).addComponent(m_boxForce);

                GroupLayout.ParallelGroup vertical2 = layout
                                .createParallelGroup()
                                .addComponent(m_boxAutoApply)
                                .addComponent(m_buttonApply);

                // Set up the main sequential layouts
                GroupLayout.SequentialGroup horizontal = layout
                                .createSequentialGroup()
                                .addGroup(horizontal0)
                                .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(horizontal1);

                GroupLayout.SequentialGroup vertical = layout
                                .createSequentialGroup()
                                .addGroup(vertical0)
                                .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(vertical1)
                                .addPreferredGap(
                                                LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(vertical2);

                // add everything to the layout
                layout.setHorizontalGroup(horizontal);
                layout.setVerticalGroup(vertical);

                // Listen to the events
                m_focusAdapter = new FocusAdapter();
                m_bundleAdapter = new ColorModeAdapter();
                m_scaleBox.addActionListener(new ScaleAdapter());
                m_focusBox.addActionListener(m_focusAdapter);
                m_bundleBox.addActionListener(m_bundleAdapter);

                // create an empty memento
                m_memento = new Memento();
                m_memento.histogramData = null;
                m_memento.currentBundle = null;
                m_memento.map = m_bundleMap;
                m_memento.scale = (HistogramPainter.Scale) m_scaleBox
                                .getSelectedItem();
        }

        /**
         * Used to determine the longest of the ComboBoxes.
         *
         * @return the largest dimension
         */
        private int getLongestComboBox() {
                int max = (int) m_scaleBox.getPreferredSize().getWidth();

                if ((int) m_bundleBox.getPreferredSize().getWidth() > max) {
                        max = (int) m_bundleBox.getPreferredSize().getWidth();
                }
                if ((int) m_focusBox.getPreferredSize().getWidth() > max) {
                        max = (int) m_focusBox.getPreferredSize().getWidth();
                }

                return max;
        }

        /**
         * Get the current state of this control.
         *
         * @return the current state
         */
        public final Memento getState() {
                return m_memento;
        }

        /**
         * Set the state of the control back.
         *
         * @param memento
         *                the state to set back to
         *
         * @return the current state
         */
        public final Memento setState(final Memento memento) {
                Memento oldMemento = m_memento;

                m_memento = memento;

                // data
                m_transferPicker.setData(m_memento.histogramData);

                // selected scale
                m_scaleBox.setSelectedItem(m_memento.scale);

                // list of bundles
                // remove the listener so that we do not get constant events
                m_bundleBox.removeActionListener(m_bundleAdapter);

                m_bundleMap = m_memento.map;

                m_bundleBox.removeAllItems();
                for (TransferFunctionBundle b : m_bundleMap.keySet()) {
                        m_bundleBox.addItem(b);
                }

                m_bundleBox.setSelectedItem(m_memento.currentBundle);

                m_bundleBox.addActionListener(m_bundleAdapter);

                // current bundle
                setActiveBundle(m_memento.currentBundle);

                return oldMemento;
        }

        /**
         * Convenience function to call getBundle with current mode.
         *
         * @return the bundle of the current mode
         */
        public final TransferFunctionBundle getBundle() {
                return m_currentBundle;
        }

        /**
         * Create a new memento for that can than be used to set the state.<br>
         *
         * I.e. if you want to set different data, first create an memento and
         * then put it in the control.
         *
         * @param bundles
         *                a list of the bundles to display, the first element of
         *                this list will be active first
         * @param data
         *                the data for the histogram
         *
         * @return the new memento
         */
        public final Memento createMemento(
                        final List<TransferFunctionBundle> bundles,
                        final int[] data) {
                return createMemento(bundles, data, bundles.get(0));
        }

        /**
         * Create a new memento for that can than be used to set the state.<br>
         *
         * I.e. if you want to set different data, first create an memento and
         * then put it in the control.
         *
         * @param bundles
         *                a list of the bundles to display, the first element of
         *                this list will be active first
         * @param data
         *                the data for the histogram
         * @param current
         *                the bundle from the bundles list that should be active
         *                when this memento is put to use
         *
         * @return the new memento
         */
        public final Memento createMemento(
                        final List<TransferFunctionBundle> bundles,
                        final int[] data, final TransferFunctionBundle current) {

                if (!bundles.contains(current))
                        throw new IllegalArgumentException(
                                        "The current bundle must be part of the bundles list");

                Memento memento = new Memento();

                // set up the map
                memento.map = new HashMap<TransferFunctionBundle, TransferFunctionColor>();
                for (TransferFunctionBundle b : bundles) {
                        memento.map.put(b, b.getKeys().iterator().next());
                }

                // use the currently set scale for the new memento
                memento.scale = (HistogramPainter.Scale) m_scaleBox
                                .getSelectedItem();

                // the data
                memento.histogramData = data;

                // the current bundle
                memento.currentBundle = current;

                return memento;
        }

        /**
         * Sets the mode this panel operates in and that of all its children.
         *
         * @param bundle
         *                the new active bundle
         */
        private void setActiveBundle(final TransferFunctionBundle bundle) {

                // Only change if something happend
                if (bundle == m_currentBundle) {
                        return;
                }

                m_currentBundle = bundle;

                m_transferPicker.setFunctions(m_currentBundle);

                Set<TransferFunctionColor> content = m_currentBundle.getKeys();
                TransferFunctionColor focus = m_bundleMap.get(m_currentBundle);

                // change contents or focus box
                // remove the adapter to not set focus to first inserted item
                m_focusBox.removeActionListener(m_focusAdapter);
                m_focusBox.removeAllItems();

                for (TransferFunctionColor color : content) {
                        m_focusBox.addItem(color);
                }

                m_focusBox.addActionListener(m_focusAdapter);

                m_focusBox.setSelectedItem(focus);
        }

        public final void setNormalize(final boolean value) {
                m_boxNormalize.setSelected(value);
                m_eventService.publish(new NormalizationChgEvent(m_boxNormalize
                                .isSelected()));
        }

        /**
         * Check wheter the force option is currently given.
         *
         * @return true if force checkbox is selected.
         */
        public final boolean isForce() {
                return m_boxForce.isSelected();
        }

        /**
         * Check wheter the autoapply option is currently given.
         *
         * @return true if autoapply checkbox is selected.
         */
        public final boolean isAutoApply() {
                return m_boxAutoApply.isSelected();
        }

        /**
         * {@inheritDoc}
         *
         * @see javax.swing.JComponent#getPreferredSize()
         */
        @Override
        public final Dimension getPreferredSize() {
                return m_preferredSize;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.knime.knip.core.ui.event.EventServiceClient#setEventService(EventService)
         */
        @Override
        public final void setEventService(final EventService eventService) {
                m_eventService = eventService;
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#setParent(Component)
         */
        @Override
        public void setParent(final Component parent) {
                // ignore
        }

        /**
         * {@inheritDoc}
         *
         * @see ViewerComponent#getPosition()
         */
        @Override
        public final Position getPosition() {
                return Position.SOUTH;
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

}
