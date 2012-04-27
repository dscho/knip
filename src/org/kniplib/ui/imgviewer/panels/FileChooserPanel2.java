package org.kniplib.ui.imgviewer.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.kniplib.ui.event.EventService;
import org.kniplib.ui.imgviewer.events.FileChooserSelectedFilesChgEvent;

public class FileChooserPanel2 extends JPanel {

        // list and its model keeping the selected files
        private final JList m_selectedFileList;
        private final FileListModel m_selectedFileListModel;
        private final TitledBorder m_selectedFileListBorder;

        // buttons to selected/remove files
        private final JButton m_addButton;
        private final JButton m_remButton;
        private final JButton m_addAllButton;
        private final JButton m_remAllButton;

        private final ImagePreviewPanel m_imagePreviewPanel;

        private final JFileChooser m_FileChooser;

        private List<ChangeListener> m_listeners;
        private EventService m_eventService;

        public FileChooserPanel2() {
                // create instances
                m_FileChooser = new JFileChooser();
                m_addButton = new JButton("add");
                m_addAllButton = new JButton("add all");
                m_remButton = new JButton("remove");
                m_remAllButton = new JButton("    remove all    ");
                m_selectedFileList = new JList();
                m_selectedFileListModel = new FileListModel();
                m_selectedFileListBorder = BorderFactory
                                .createTitledBorder("Selected files");
                final JScrollPane jspSelFileList = new JScrollPane(
                                m_selectedFileList);
                m_selectedFileList.setModel(m_selectedFileListModel);

                // arrange the components
                final JPanel left = new JPanel();
                left.setBorder(BorderFactory.createTitledBorder("File browser"));
                final JPanel right = new JPanel();
                right.setBorder(m_selectedFileListBorder);
                final JPanel center = new JPanel();
                center.setBorder(BorderFactory.createTitledBorder("Selection"));

                left.setLayout(new BorderLayout());
                right.setLayout(new BorderLayout());
                center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

                JPanel buttonPan = new JPanel();
                buttonPan.setLayout(new BoxLayout(buttonPan, BoxLayout.X_AXIS));
                buttonPan.add(Box.createVerticalStrut(20));
                JPanel delButtonPan = new JPanel();
                delButtonPan.setLayout(new BoxLayout(delButtonPan,
                                BoxLayout.X_AXIS));
                delButtonPan.add(Box.createVerticalStrut(20));
                m_addButton.setMaximumSize(new Dimension(150, 25));
                m_addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPan.add(m_addButton);
                buttonPan.add(Box.createVerticalStrut(20));
                m_addAllButton.setMaximumSize(new Dimension(150, 25));
                m_addAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPan.add(m_addAllButton);
                buttonPan.add(Box.createVerticalStrut(20));
                m_remButton.setMaximumSize(new Dimension(150, 25));
                m_remButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                delButtonPan.add(m_remButton);
                delButtonPan.add(Box.createVerticalStrut(20));
                m_remAllButton.setMaximumSize(new Dimension(150, 25));
                m_remAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                delButtonPan.add(m_remAllButton);
                delButtonPan.add(Box.createVerticalStrut(20));

                m_imagePreviewPanel = new ImagePreviewPanel();

                // buttonPan.add(m_imagePreviewPanel);
                // buttonPan.add(Box.createGlue());
                m_FileChooser.setPreferredSize(new Dimension(300, 100));
                JPanel browsePane = new JPanel();
                browsePane.setLayout(new BoxLayout(browsePane, BoxLayout.Y_AXIS));
                browsePane.add(m_FileChooser);

                m_FileChooser.setFileFilter(new FileNameExtensionFilter(
                                "images", "jpg", "png", "tif", "jpeg", "bmp"));
                JTabbedPane rightTab = new JTabbedPane();
                m_FileChooser.setMultiSelectionEnabled(true);
                m_FileChooser.setControlButtonsAreShown(false);
                m_FileChooser.setPreferredSize(new Dimension(450, 340));
                // center.add(buttonPan);
                rightTab.setPreferredSize(new Dimension(400, 300));
                browsePane.setPreferredSize(new Dimension(600, 500));
                right.add(rightTab);
                left.add(browsePane);
                browsePane.add(buttonPan);

                JPanel selectedPane = new JPanel();
                selectedPane.setLayout(new BoxLayout(selectedPane,
                                BoxLayout.Y_AXIS));
                selectedPane.add(jspSelFileList);
                selectedPane.add(delButtonPan);

                // browsePane.add(m_addAllButton);
                rightTab.add("Selected Files", selectedPane);
                rightTab.add("Preview/Meta-Data", m_imagePreviewPanel);

                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                add(left);
                add(center);
                add(right);

                m_FileChooser.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                System.out.println("Action");
                                File selectedFile = m_FileChooser
                                                .getSelectedFile();
                                System.out.println(selectedFile.getParent());
                                System.out.println(selectedFile.getName());
                                m_imagePreviewPanel.setImage(selectedFile
                                                .toString());

                        }
                });

                m_addButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                onAdd();
                        }
                });

                m_addAllButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                onAddAll();
                        }
                });

                m_remButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                onRemove();
                        }
                });

                m_remAllButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                onRemoveAll();
                        }
                });
        }

        /*
         * action for the add button
         */

        private void onAdd() {

                File[] files = m_FileChooser.getSelectedFiles();
                m_selectedFileListModel.addFiles(files);

                fireSelectionChangedEvent();

        }

        /*
         *
         * action add all button
         */

        private void onAddAll() {
                m_selectedFileListModel.addFiles(m_FileChooser
                                .getCurrentDirectory().listFiles());
                fireSelectionChangedEvent();
        }

        /*
         * when the remove button was pressed
         */
        private void onRemove() {
                m_selectedFileListModel.remove(m_selectedFileList
                                .getSelectedIndices());
                fireSelectionChangedEvent();
        }

        /*
         * when the remove all button was pressed
         */
        private void onRemoveAll() {
                m_selectedFileListModel.removeAll();
                fireSelectionChangedEvent();
        }

        private void fireSelectionChangedEvent() {
                if (m_listeners != null) {
                        for (ChangeListener listener : m_listeners) {
                                listener.stateChanged(new ChangeEvent(this));
                        }
                }
                m_selectedFileListBorder.setTitle("Selected files ("
                                + m_selectedFileListModel.getSize() + ")");
                repaint();

                if (m_eventService != null)
                        m_eventService.publish(new FileChooserSelectedFilesChgEvent(
                                        getSelectedFiles()));

        }

        /**
         * Updates the selected files list after removing or adding files
         *
         * @param selectedFiles
         */

        public void update(final String[] selectedFiles) {
                // applying the model settings to the components
                if (selectedFiles.length > 0) {
                        File[] files = new File[selectedFiles.length];
                        for (int i = 0; i < selectedFiles.length; i++) {
                                files[i] = new File(selectedFiles[i]);
                        }
                        m_selectedFileListModel.removeAll();
                        m_selectedFileListModel.addFiles(files);

                }

                if (m_eventService != null)
                        m_eventService.publish(new FileChooserSelectedFilesChgEvent(
                                        getSelectedFiles()));
        }

        /**
         * The list of the selected files
         *
         * @return files
         *
         */
        public String[] getSelectedFiles() {
                String[] values = new String[m_FileChooser.getSelectedFiles().length];
                for (int i = 0; i < values.length; i++) {
                        values[i] = m_FileChooser.getSelectedFiles()[i]
                                        .toString();
                }
                return values;
        }

        private class FileListModel implements ListModel, Comparator<File> {

                private final Vector<File> m_files = new Vector<File>();

                private final ArrayList<ListDataListener> listener = new ArrayList<ListDataListener>();

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void addListDataListener(final ListDataListener arg0) {
                        listener.add(arg0);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public Object getElementAt(final int arg0) {
                        return m_files.get(arg0).getName();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public int getSize() {
                        return m_files.size();
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void removeListDataListener(final ListDataListener arg0) {
                        listener.remove(arg0);

                }

                public void addFiles(final File[] files) {

                        Arrays.sort(files, this);
                        for (File f : files) {
                                m_files.add(f);
                        }
                        notifyListDataListener();

                }

                public void remove(final int[] indices) {
                        for (int i = indices.length - 1; i >= 0; i--) {
                                m_files.remove(indices[i]);
                        }
                        notifyListDataListener();
                }

                public void removeAll() {
                        m_files.clear();
                        notifyListDataListener();
                }

                private void notifyListDataListener() {
                        for (ListDataListener l : listener) {
                                l.contentsChanged(new ListDataEvent(this,
                                                ListDataEvent.CONTENTS_CHANGED,
                                                0, getSize()));
                        }

                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public int compare(final File o1, final File o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                }
        }

        public static void main(String[] args) {

                JFrame f = new JFrame();
                f.add(new FileChooserPanel2());

                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.pack();
                f.setVisible(true);

        }

}
