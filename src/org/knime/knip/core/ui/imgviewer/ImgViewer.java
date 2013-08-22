package org.knime.knip.core.ui.imgviewer;

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
 *
 * History
 *   29 Jan 2010 (hornm): created
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.DefaultCalibratedAxis;
import net.imglib2.meta.DefaultCalibratedSpace;
import net.imglib2.meta.ImageMetadata;
import net.imglib2.meta.MetadataUtil;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;
import net.imglib2.meta.TypedAxis;
import net.imglib2.meta.TypedSpace;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;

/**
 * A TableCellViewPane providing another view on image objects. It allows to browser through the individual
 * planes/dimensions, enhance contrast, etc.
 *
 * @author dietzc, hornm, University of Konstanz
 * @param <T>
 */
public class ImgViewer<T extends Type<T>, I extends RandomAccessibleInterval<T>> extends JPanel implements
        ViewerComponentContainer {

    /* def */
    private static final long serialVersionUID = 1L;

    /* Panels of the viewer accoding to the BorderLayout */
    private JPanel m_southPanels;

    private JPanel m_eastPanels;

    private JPanel m_westPanels;

    private Container m_northPanels;

    private JPanel m_centerPanels;

    /** keep the option panel-references to load and save configurations */
    protected List<ViewerComponent> m_viewerComponents;

    /** EventService of the viewer, unique for each viewer. */
    protected EventService m_eventService;

    public ImgViewer() {
        this(new EventService());

    }

    /**
     * @param nodeModel
     */
    public ImgViewer(final EventService eventService) {

        m_eventService = eventService;
        m_viewerComponents = new ArrayList<ViewerComponent>();
        // content pane
        setLayout(new BorderLayout());

        m_centerPanels = new JPanel();
        m_centerPanels.setLayout(new BoxLayout(m_centerPanels, BoxLayout.Y_AXIS));
        add(m_centerPanels, BorderLayout.CENTER);

        m_southPanels = new JPanel();
        m_southPanels.setLayout(new BoxLayout(m_southPanels, BoxLayout.X_AXIS));
        add(m_southPanels, BorderLayout.SOUTH);

        m_eastPanels = new JPanel();
        m_eastPanels.setLayout(new BoxLayout(m_eastPanels, BoxLayout.Y_AXIS));
        add(m_eastPanels, BorderLayout.EAST);

        m_westPanels = new JPanel();
        m_westPanels.setLayout(new BoxLayout(m_westPanels, BoxLayout.Y_AXIS));
        add(m_westPanels, BorderLayout.WEST);

        m_northPanels = new JPanel();
        m_northPanels.setLayout(new BoxLayout(m_northPanels, BoxLayout.X_AXIS));
        add(m_northPanels, BorderLayout.NORTH);

    }

    /**
     * Adds the panel
     */
    @Override
    public void addViewerComponent(final ViewerComponent panel) {
        addViewerComponent(panel, true);

    }

    /**
     * Adds the {@link ViewerComponent} to the {@link ImgViewer}
     *
     * @param panel {@link ViewerComponent} to be set
     *
     * @param setEventService indicates weather the {@link EventService} of the {@link ImgViewer} shall be set to the
     *            {@link ViewerComponent}
     *
     */
    public void addViewerComponent(final ViewerComponent panel, final boolean setEventService) {

        if (setEventService) {
            panel.setEventService(m_eventService);
        }

        m_viewerComponents.add(panel);

        switch (panel.getPosition()) {
            case CENTER:
                m_centerPanels.add(panel);
                break;
            case NORTH:
                m_northPanels.add(panel);
                break;
            case SOUTH:
                m_southPanels.add(panel);
                break;
            case EAST:
                m_eastPanels.add(panel);
                break;
            case WEST:
                m_westPanels.add(panel);
                break;
            default: // hidden

        }

    }

    /**
     * @return the event service used in this particular viewer (e.g. to subscribe other listeners)
     */
    public EventService getEventService() {
        return m_eventService;
    }

    /**
     * Set the current {@link Img} of the viewer
     *
     * @param img {@link Img} to be set
     * @param axes {@link CalibratedSpace} of the {@link Img}
     * @param name {@link Named} of the {@link Img}
     * @param imageMetaData {@link ImageMetadata} might be null if no metadata exists
     */
    public void setImg(final I img, final TypedSpace<? extends TypedAxis> axes, final Named name, final Sourced source,
                       final ImageMetadata imageMetaData) {

        // make sure that at least two dimensions exist
        TypedSpace<? extends TypedAxis> axes2d;
        RandomAccessibleInterval<T> img2d;
        if (img.numDimensions() > 1) {
            axes2d = axes;
            img2d = img;
        } else {
            img2d = Views.addDimension(img, 0, 0);
            DefaultCalibratedSpace out = (DefaultCalibratedSpace)MetadataUtil.copyAndCleanTypedSpace(img2d, new DefaultCalibratedSpace(new DefaultCalibratedAxis(axes.axis(0).type())), new DefaultCalibratedSpace(2));
            axes2d = out;
        }

        if (imageMetaData != null) {
            m_eventService.publish(new ImgWithMetadataChgEvent<T>(img2d, name, source, axes2d, imageMetaData));

        } else {
            m_eventService.publish(new IntervalWithMetadataChgEvent<T>(img2d, name, source, axes2d));

        }
        m_eventService.publish(new ImgRedrawEvent());

    }

}
