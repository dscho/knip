package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.events.FileChooserSelectedFilesChgEvent;

public class FileChooserPanel extends JPanel {

        static final long serialVersionUID = 1;
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
        private final ImagePreviewListener m_previewListener;

        private final MacHackedFileChooserPanel m_fileChooser;

        private List<ChangeListener> m_listeners;
        private EventService m_eventService;
        private final JPopupMenu popup = new JPopupMenu();
        private final File defDir;
        private final Preferences filePref = Preferences
                        .userNodeForPackage(getClass());
        private final FileFilter fileFilter;

        // /**
        // * Creates an new file chooser panel with no files filtered.
        // */
        // public FileChooserPanel2() {
        // this(null);
        // }

        /**
         * Creates a new file chooser panel
         *
         * @param fileFilter
         *                available file name extension filters
         */
        public FileChooserPanel(FileFilter fileFilter) {
                String prefDir = filePref.get("Path", "null");
                this.fileFilter = fileFilter;

                defDir = new File(prefDir);
                // System.out.println(defDir.toString());
                // create instances
                m_fileChooser = new MacHackedFileChooserPanel();

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

                ApplyFileView view = new ApplyFileView();
                m_fileChooser.setFileView(view);

                // buttonPan.add(m_imagePreviewPanel);
                // buttonPan.add(Box.createGlue());
                m_fileChooser.setPreferredSize(new Dimension(300, 100));
                JPanel browsePane = new JPanel();
                browsePane.setLayout(new BoxLayout(browsePane, BoxLayout.Y_AXIS));
                browsePane.add(m_fileChooser);

                if (fileFilter != null) {
                        // m_fileChooser.setFileFilter(fileFilter);
                        m_fileChooser.setFileFilter(fileFilter);

                }

                JTabbedPane rightTab = new JTabbedPane();
                m_fileChooser.setMultiSelectionEnabled(true);
                m_fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                m_fileChooser.setControlButtonsAreShown(false);
                m_fileChooser.setPreferredSize(new Dimension(450, 340));
                // center.add(buttonPan);
                // rightTab.setPreferredSize(new Dimension(400, 300));
                // browsePane.setPreferredSize(new Dimension(600, 500));
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
                // add(center);
                add(right);
                m_fileChooser.setComponentPopupMenu(popup);

                JMenuItem clearSelection = new JMenuItem("Remove All");
                clearSelection.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                m_selectedFileListModel.removeAll();
                                fireSelectionChangedEvent();
                        }

                });

                JMenuItem remove = new JMenuItem("Remove Selected");
                remove.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                m_selectedFileListModel.removeMenu(m_fileChooser
                                                .getSelectedFiles());

                                fireSelectionChangedEvent();
                        }
                });

                popup.add(clearSelection);
                popup.add(remove);

                // Show preview and metadata from the selected file
                m_previewListener = new ImagePreviewListener(
                                m_imagePreviewPanel);
                m_fileChooser.addPropertyChangeListener(m_previewListener);

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

                m_fileChooser.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                onAdd();
                        }
                });

                m_selectedFileList.addMouseListener(new MouseListener() {

                        @Override
                        public void mouseClicked(MouseEvent arg0) {
                                if (arg0.getClickCount() == 2) {
                                        onRemove();
                                }

                        }

                        @Override
                        public void mouseEntered(MouseEvent arg0) {
                                // TODO Auto-generated method stub

                        }

                        @Override
                        public void mouseExited(MouseEvent arg0) {
                                // TODO Auto-generated method stub

                        }

                        @Override
                        public void mousePressed(MouseEvent arg0) {
                                // TODO Auto-generated method stub

                        }

                        @Override
                        public void mouseReleased(MouseEvent arg0) {
                                // TODO Auto-generated method stub

                        }
                });

                m_fileChooser.setCurrentDirectory(defDir);
                m_fileChooser.setVisible(false);
                m_fileChooser.setVisible(true);
        }

        /*
         * action for the add button
         */

        private void onAdd() {
                FileFilter ff = m_fileChooser.getFileFilter();
                if (m_fileChooser.getSelectedFiles().length == 0) {
                        JOptionPane.showMessageDialog(
                                        this,
                                        "No files selected. Please select at least one file or directory.",
                                        "Warning", JOptionPane.ERROR_MESSAGE);
                        return;
                }

                if (m_fileChooser.getSelectedFile().isDirectory()) {
                        File[] f = m_fileChooser.getSelectedFile().listFiles();
                        m_selectedFileListModel.addFiles(f, ff);
                } else {
                File[] files = m_fileChooser.getSelectedFiles();
                        m_selectedFileListModel.addFiles(files, ff);
                }
                filePref.put("Path", m_fileChooser.getCurrentDirectory()
                                .toString());
                fireSelectionChangedEvent();


        }

        /*
         *
         * action add all button
         */

        private void onAddAll() {
                // FileFilter f = m_fileChooser.getFileFilter();
                // m_fileChooser.getCurrentDirectory().listFiles((FilenameFilter)
                // f);
                m_selectedFileListModel.addFiles(m_fileChooser
                                .getCurrentDirectory().listFiles(), fileFilter);
                filePref.put("Path", m_fileChooser.getCurrentDirectory()
                                .toString());
                fireSelectionChangedEvent();

        }

        /*
         * when the remove button was pressed
         */
        private void onRemove() {
                m_selectedFileListModel.remove(m_selectedFileList
                                .getSelectedIndices());
                m_selectedFileList.clearSelection();
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
                        m_selectedFileListModel.addFiles(files, fileFilter);

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
                String[] values = new String[m_selectedFileListModel.getSize()];
                for (int i = 0; i < values.length; i++) {
                        values[i] = m_selectedFileListModel
                                        .getAbsolutePathAt(i);
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

                public String getAbsolutePathAt(int idx) {
                        return m_files.get(idx).getAbsolutePath();
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

                public void addFiles(final File[] files, FileFilter fileFilter) {

                        Arrays.sort(files, this);
                        for (File f : files) {

                                if (!f.isDirectory() && !m_files.contains(f)
                                                && fileFilter.accept(f))

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

                public void removeMenu(File[] files) {
                        for (File f : files)
                                m_files.remove(f);

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

                public Boolean contains(File f) {
                        return m_files.contains(f);
                }
        }

        /**
         * Shows a modal dialog to select some files
         *
         * @param file_list
         *                default files in the selected file list
         * @return the file list returns <code>null</code> if the dialog was
         *         closed with cancel, or ESC or X
         */

        public static String[] showFileChooserDialog(FileFilter filter,
                        final String[] file_list) {

                final JDialog dlg = new JDialog();
                dlg.setAlwaysOnTop(true);
                dlg.setModalityType(ModalityType.APPLICATION_MODAL);
                final int[] cancelFlag = new int[] { 0 };

                FileChooserPanel panel = new FileChooserPanel(filter);
                if (file_list != null) {
                        panel.update(file_list);
                }

                JButton ok = new JButton("OK");
                ok.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                cancelFlag[0] = -1;
                                dlg.dispose();

                        }
                });
                JButton cancel = new JButton("Cancel");
                cancel.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                dlg.dispose();
                        }
                });
                JPanel buttons = new JPanel();
                buttons.add(ok);
                buttons.add(cancel);
                dlg.getContentPane().setLayout(new BorderLayout());
                dlg.getContentPane().add(panel, BorderLayout.CENTER);
                dlg.getContentPane().add(buttons, BorderLayout.SOUTH);

                dlg.pack();
                dlg.setVisible(true);

                if (cancelFlag[0] == -1) {
                        return panel.getSelectedFiles();
                } else {
                        return null;
                }
        }

        public FileFilter getFileFilter() {
                return fileFilter;
        }

        class MacHackedFileChooserPanel extends JFileChooser {

                @Override
                protected void firePropertyChange(String propertyName,
                                Object oldValue, Object newValue) {
                        try {
                                super.firePropertyChange(propertyName,
                                                oldValue, newValue);
                        } catch (Exception e) { // This is a hack to avoid
                                                // stupid mac behaviour

                        }

                }
        }

        // a class to show the image preview with only one click (File
        // Selection)
        class ImagePreviewListener implements PropertyChangeListener {
                ImagePreviewPanel m_imagePreviewPanel;

                public ImagePreviewListener(ImagePreviewPanel imagePreviewPanel) {
                        m_imagePreviewPanel = imagePreviewPanel;
                }

                @Override
                public void propertyChange(PropertyChangeEvent e) {
                        String propertyName = e.getPropertyName();
                        File selection = (File) e.getNewValue();
                        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {


                                m_imagePreviewPanel.setImage(selection
                                                .toString());
                        }

                }

        }

        class ApplyFileView extends FileView {
                Icon newicon = new ImageIcon(getClass()
                                .getResource("apply.png"));

                @Override
                public Icon getIcon(File file) {
                        if (file.isDirectory()) {
                                return null;
                        }
                        Icon icon = null;
                        if (m_selectedFileListModel.contains(file)) {
                                icon = newicon;
                        }
                        return icon;

                }
        }

}
