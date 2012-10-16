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
package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelColoringChangeEvent;

/**
 * @author zinsmaie, dietzc, muethingc
 *
 */
public class ColorSelectionFrame extends JFrame {

        private EventService m_eventService;

        private final JColorChooser m_cchoose = new JColorChooser();

        public ColorSelectionFrame(final EventService eventService) {

                setEventService(eventService);

                JButton colorOK = new JButton("OK");
                JButton colorCancel = new JButton("Cancel");
                colorOK.setSize(5, 2);
                colorCancel.setSize(6, 2);

                // Settings for JColorChooser

                m_cchoose.setPreviewPanel(new JPanel());
                m_cchoose.removeChooserPanel(m_cchoose.getChooserPanels()[0]);
                m_cchoose.removeChooserPanel(m_cchoose.getChooserPanels()[1]);

                JPanel buttonPane = new JPanel();
                JPanel colorPane = new JPanel();

                colorPane.add(m_cchoose);
                buttonPane.add(colorOK);
                buttonPane.add(colorCancel);
                colorPane.add(buttonPane);

                this.getContentPane().add(colorPane);
                this.setSize(450, 300);
                this.setTitle("Choose BoundingBox Color");

                colorCancel.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                setVisible(false);
                        }
                });

                colorOK.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(
                                        java.awt.event.ActionEvent evt) {
                                Color newColor = m_cchoose.getColor();
                                SegmentColorTable.setBoundingBoxColor(newColor);

                                setVisible(false);
                                m_eventService.publish(new LabelColoringChangeEvent(
                                                newColor,
                                                SegmentColorTable
                                                                .getColorMapNr()));
                                m_eventService.publish(new ImgRedrawEvent());
                        }
                });
        }

        public void setEventService(final EventService service) {
                if (service == null) {
                        m_eventService = new EventService();
                } else {
                        m_eventService = service;
                }
        }

}
