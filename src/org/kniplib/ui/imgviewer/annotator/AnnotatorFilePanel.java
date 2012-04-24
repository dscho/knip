package org.kniplib.ui.imgviewer.annotator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgPlus;
import net.imglib2.io.ImgIOException;
import net.imglib2.type.numeric.RealType;

import org.kniplib.io.BioFormatsImgSource;
import org.kniplib.io.reference.ImgSource;
import org.kniplib.io.reference.ImgSourcePool;
import org.kniplib.ui.event.EventService;
import org.kniplib.ui.imgviewer.ViewerComponent;
import org.kniplib.ui.imgviewer.annotator.events.AnnotatorFilelistChgEvent;
import org.kniplib.ui.imgviewer.events.ImgChgEvent;
import org.kniplib.ui.imgviewer.events.ImgViewerTextMessageChgEvent;
import org.kniplib.ui.imgviewer.panels.FileChooserPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotatorFilePanel<T extends RealType<T>> extends ViewerComponent {

        /* Logger */
        private final Logger logger = LoggerFactory
                        .getLogger(AnnotatorFilePanel.class);

        public static final String IMAGE_SOURCE_ID = "ANNOTATOR_IMAGE_SOURCE";

        /* */
        private static final long serialVersionUID = 1L;

        /* */
        private JList m_fileList;

        /* */
        private EventService m_eventService;

        /* */
        private String[] m_allFiles;

        /* */
        private boolean m_isAdjusting;

        public AnnotatorFilePanel() {
                super("Selected files", false);
                m_isAdjusting = false;
                m_fileList = new JList();
                m_fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                m_fileList.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(final ListSelectionEvent e) {
                                if (m_isAdjusting)
                                        return;

                                onListSelection(e);
                        }
                });
                setPreferredSize(new Dimension(100, 100));
                setLayout(new BorderLayout());
                add(new JScrollPane(m_fileList), BorderLayout.CENTER);
                JButton browse = new JButton("Browse ...");
                browse.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                m_isAdjusting = true;
                                String[] files = FileChooserPanel
                                                .showFileChooserDialog(
                                                                m_allFiles,
                                                                System.getProperty("user.home"));
                                if (files != null) {
                                        m_allFiles = files;
                                        m_fileList.setListData(m_allFiles);
                                        m_eventService.publish(new AnnotatorFilelistChgEvent(
                                                        files));
                                }
                                m_isAdjusting = false;
                                updateUI();
                        }
                });
                add(browse, BorderLayout.SOUTH);
        }

        @Override
        public void setEventService(EventService eventService) {
                m_eventService = eventService;
                eventService.subscribe(this);
        }

        @Override
        public String getPosition() {
                return BorderLayout.WEST;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void onListSelection(final ListSelectionEvent e2) {
                if (e2.getValueIsAdjusting()) {
                        return;
                }
                try {
                        if (ImgSourcePool.getImgSource(IMAGE_SOURCE_ID) == null) {
                                ImgSourcePool.addImgSource(IMAGE_SOURCE_ID,
                                                new BioFormatsImgSource());
                        }
                        ImgSource source = ImgSourcePool
                                        .getImgSource(IMAGE_SOURCE_ID);

                        String ref = (String) m_fileList.getSelectedValue();

                        m_eventService.publish(new ImgViewerTextMessageChgEvent(
                                        "Load img ... " + ref));

                        ImgPlus<T> imgPlus = new ImgPlus(
                                        source.<T> getImg(ref),
                                        source.getSource(ref),
                                        source.getAxes(ref),
                                        source.getCalibration(ref));

                        m_eventService.publish(new ImgChgEvent<ImgPlus<T>>(
                                        imgPlus, imgPlus, imgPlus));

                } catch (ImgIOException e) {
                        logger.warn("Failed to load image");
                        m_eventService.publish(new ImgViewerTextMessageChgEvent(
                                        "Failed to load image"));
                } catch (IncompatibleTypeException e) {
                        logger.warn("Failed to load image");
                        m_eventService.publish(new ImgViewerTextMessageChgEvent(
                                        "Failed to load image"));
                } catch (Exception e) {
                        logger.warn("Failed to load image");
                        e.printStackTrace();
                }

        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                out.writeInt(m_allFiles.length);
                for (int s = 0; s < m_allFiles.length; s++) {
                        out.writeUTF(m_allFiles[s]);
                }
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException {

                int num = in.readInt();
                m_allFiles = new String[num];
                for (int s = 0; s < num; s++) {
                        m_allFiles[s] = in.readUTF();
                }
                m_isAdjusting = true;
                m_fileList.setListData(m_allFiles);
                m_isAdjusting = false;
                m_fileList.updateUI();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void reset() {
                m_allFiles = new String[0];
                m_isAdjusting = true;
                m_fileList.setListData(m_allFiles);
                m_isAdjusting = false;
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }
}
