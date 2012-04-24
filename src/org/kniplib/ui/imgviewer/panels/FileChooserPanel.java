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
 *   16 Nov 2009 (hornm): created
 */
package org.kniplib.ui.imgviewer.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.kniplib.ui.event.EventService;
import org.kniplib.ui.event.EventServiceClient;
import org.kniplib.ui.imgviewer.events.FileChooserSelectedFilesChgEvent;

/**
 *
 * Allows the selection of multiple files. Including an image preview an the
 * meta data.
 *
 * @author hornm, University of Konstanz
 */
@SuppressWarnings("serial")
public class FileChooserPanel extends JPanel implements EventServiceClient {

        private final JButton m_addButton;

        private final JButton m_remButton;

        private final JButton m_addAllButton;

        private final JButton m_remAllButton;

        private final FileTree m_dirTree;

        private final JList m_fileList;

        private final JList m_selectedFileList;

        private final FileListModel m_fileListModel;

        private final TitledBorder m_selectedFileListBorder;

        private final FileListModel m_selectedFileListModel;

        private File m_directory;

        private final JTextField m_filterTextField;

        private final JComboBox m_pathComboBox;

        private List<ChangeListener> m_listeners;

        private final ImagePreviewPanel m_imagePreviewPanel;

        private EventService m_eventService;

        /**
         * Creates a new FileChooserPanel.
         */
        public FileChooserPanel() {

                // create instances
                m_addButton = new JButton("add >>");
                m_addAllButton = new JButton("add all >>");
                m_remButton = new JButton("remove");
                m_remAllButton = new JButton("    remove all    ");

                m_dirTree = new FileTree();
                m_selectedFileList = new JList();
                m_fileList = new JList();

                m_fileListModel = new FileListModel();
                m_selectedFileListModel = new FileListModel();
                m_selectedFileListBorder = BorderFactory
                                .createTitledBorder("Selected files");

                final JScrollPane jspDirTree = new JScrollPane(m_dirTree);
                final JScrollPane jspFileList = new JScrollPane(m_fileList);
                final JScrollPane jspSelFileList = new JScrollPane(
                                m_selectedFileList);

                m_pathComboBox = new JComboBox();
                m_filterTextField = new JTextField(10);
                JButton help = new JButton("?");

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

                JPanel pathPanel = new JPanel();
                pathPanel.add(new JLabel("Path: "));
                pathPanel.add(m_pathComboBox);
                left.add(pathPanel, BorderLayout.NORTH);

                JSplitPane browsePane = new JSplitPane(
                                JSplitPane.HORIZONTAL_SPLIT);
                browsePane.add(jspDirTree);
                browsePane.add(jspFileList);

                JPanel filterPanel = new JPanel();
                filterPanel.add(new JLabel("Pattern: "));
                filterPanel.add(m_filterTextField);
                filterPanel.add(help);
                left.add(filterPanel, BorderLayout.SOUTH);

                JPanel buttonPan = new JPanel();
                buttonPan.setLayout(new BoxLayout(buttonPan, BoxLayout.Y_AXIS));
                buttonPan.add(Box.createVerticalStrut(20));
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
                buttonPan.add(m_remButton);
                buttonPan.add(Box.createVerticalStrut(20));
                m_remAllButton.setMaximumSize(new Dimension(150, 25));
                m_remAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                buttonPan.add(m_remAllButton);
                buttonPan.add(Box.createVerticalStrut(20));

                m_imagePreviewPanel = new ImagePreviewPanel();

                buttonPan.add(m_imagePreviewPanel);
                // buttonPan.add(Box.createGlue());

                center.add(buttonPan);
                left.add(browsePane);
                right.add(jspSelFileList);

                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                add(left);
                add(center);
                add(right);

                // configure the components

                m_pathComboBox.setPreferredSize(new Dimension(200,
                                m_pathComboBox.getPreferredSize().height));
                // m_pathComboBox.setRenderer(new ConvenientComboBoxRenderer());
                m_pathComboBox.setEditable(true);

                jspFileList.setPreferredSize(new Dimension(150, 155));
                jspDirTree.setPreferredSize(new Dimension(150, 155));
                jspSelFileList.setPreferredSize(new Dimension(150, 155));

                m_fileList.setMinimumSize(new Dimension(150, 155));
                m_selectedFileList.setMinimumSize(new Dimension(150, 155));
                m_dirTree.setMinimumSize(new Dimension(150, 155));

                m_fileList.setPrototypeCellValue(new String(
                                "12345678901234567890"));
                m_selectedFileList.setPrototypeCellValue(new String(
                                "12345678901234567890"));

                m_fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                m_fileList.setModel(m_fileListModel);
                m_selectedFileList
                                .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                m_selectedFileList.setModel(m_selectedFileListModel);

                // add the listeners

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

                m_dirTree.addTreeSelectionListener(new TreeSelectionListener() {

                        @Override
                        public void valueChanged(final TreeSelectionEvent e) {
                                DefaultMutableTreeNode node = m_dirTree
                                                .getTreeNode(e.getPath());
                                FileNode fnode = m_dirTree.getFileNode(node);
                                if (fnode != null) {
                                        m_directory = fnode.getFile();
                                        m_pathComboBox.getEditor()
                                                        .setItem(m_directory
                                                                        .getAbsolutePath());
                                        // m_pathTextField.setText(m_directory.getAbsolutePath());

                                        updateFileList();
                                }
                        }
                });

                m_fileList.addListSelectionListener(new ListSelectionListener() {

                        @Override
                        public void valueChanged(final ListSelectionEvent arg0) {
                                if (!arg0.getValueIsAdjusting()) {
                                        m_imagePreviewPanel
                                                        .setImage(m_fileListModel
                                                                        .getFile(m_fileList
                                                                                        .getSelectedIndex())
                                                                        .getAbsolutePath());
                                }
                        }

                });

                m_filterTextField.addKeyListener(new KeyListener() {

                        @Override
                        public void keyPressed(final KeyEvent e) {
                                // nothing to do
                        }

                        @Override
                        public void keyReleased(final KeyEvent e) {
                                updateFileList();

                        }

                        @Override
                        public void keyTyped(final KeyEvent e) {
                                // nothing to do
                        }

                });
                m_filterTextField.setText("*");
                help.setEnabled(false);
                help.setToolTipText("<html>"
                                + "* = any string<br>? = any character<br>"
                                + "</html>");
                help.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(final MouseEvent e) {
                                JComponent c = (JComponent) e.getComponent();
                                Action action = c.getActionMap().get("postTip");
                                // it is also possible to use own Timer to
                                // display
                                // ToolTip with custom delay, but here we just
                                // display it immediately
                                if (action != null) {
                                        action.actionPerformed(new ActionEvent(
                                                        c,
                                                        ActionEvent.ACTION_PERFORMED,
                                                        "postTip"));
                                }
                        }

                });

                m_pathComboBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(final ActionEvent e) {
                                onPathChange();
                        }
                });

        }

        /*
         * action for the add button
         */

        private void onAdd() {
                int[] indices = m_fileList.getSelectedIndices();
                File[] files = new File[indices.length];
                for (int i = 0; i < indices.length; i++) {
                        files[i] = m_fileListModel.getFile(indices[i]);
                }
                m_selectedFileListModel.addFiles(files);
                fireSelectionChangedEvent();

        }

        /*
         *
         * action add all button
         */

        private void onAddAll() {
                m_selectedFileListModel.addFiles(m_fileListModel.getAllFiles());
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

        /*
         * action if a different path was typed
         */
        private void onPathChange() {
                m_dirTree.expandPath(m_pathComboBox.getEditor().getItem()
                                .toString());
                // m_dirTree.expandPath(m_pathTextField.getText());
        }

        /*
         * Updates the file list, basically after selecting another directory.
         */
        private void updateFileList() {

                if (m_directory == null) {
                        return;
                }
                m_fileListModel.removeAll();
                try {
                        final Pattern p = Pattern
                                        .compile(makeRegEx(m_filterTextField
                                                        .getText()));

                        if (m_directory.exists()) {
                                File[] list = m_directory
                                                .listFiles(new FileFilter() {
                                                        @Override
                                                        public boolean accept(
                                                                        final File f) {

                                                                if (f.isDirectory()) {
                                                                        return false;
                                                                }
                                                                if (m_filterTextField
                                                                                .getText()
                                                                                .equals("*")) {
                                                                        return true;
                                                                }
                                                                return p.matcher(
                                                                                f.getName())
                                                                                .matches();

                                                                // if
                                                                // (f.getName().contains(m_filterTextField.getText()))
                                                                // {
                                                                // return true;
                                                                // } else {
                                                                // return false;
                                                                // }
                                                        }
                                                });
                                m_fileListModel.addFiles(list != null ? list
                                                : new File[0]);
                        }

                } catch (PatternSyntaxException e) {
                        //
                }

        }

        /* Helper which makes a java regular expression */
        private String makeRegEx(String txt) {

                txt = txt.replaceAll("\\*", ".*");
                txt = txt.replaceAll("\\?", ".");
                txt = txt.replaceAll("\\(", "\\\\\\(");
                txt = txt.replaceAll("\\)", "\\\\\\)");

                return txt;

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
                        m_dirTree.expandPath(files[files.length - 1]
                                        .getParent());

                }

                if (m_eventService != null)
                        m_eventService.publish(new FileChooserSelectedFilesChgEvent(
                                        getSelectedFiles()));
        }

        /**
         * Adds a listener which gets informed whenever the file selection
         * changes.
         *
         * @param listener
         *                the listener
         */
        public void addChangeListener(final ChangeListener listener) {
                if (m_listeners == null) {
                        m_listeners = new ArrayList<ChangeListener>();
                }
                m_listeners.add(listener);
        }

        /**
         * Removes the given listener from this panel.
         *
         * @param listener
         *                the listener.
         */
        public void removeChangeListener(final ChangeListener listener) {
                if (m_listeners != null) {
                        m_listeners.remove(listener);
                }
        }

        /**
         * Removes all listeners.
         */
        public void removeAllColumnFilterChangeListener() {
                if (m_listeners != null) {
                        m_listeners.clear();
                }
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
         * The list of the selected files
         *
         * @return files
         *
         */
        public String[] getSelectedFiles() {
                String[] values = new String[m_selectedFileListModel.getSize()];
                for (int i = 0; i < values.length; i++) {
                        values[i] = m_selectedFileListModel.getFile(i)
                                        .getAbsolutePath();
                }
                return values;
        }

        /**
         * Sets the currently selected directory.
         *
         * @param path
         *
         */
        public void setCurrentDirectory(String path) {
                m_pathComboBox.getEditor().setItem(path);
                // m_pathTextField.setText(path);
                onPathChange();
        }

        /**
         * Delivers the current selected directory.
         *
         * @return current dir
         */
        public String getCurrentDirectory() {
                return m_pathComboBox.getEditor().getItem().toString();
        }

        /**
         * Sets the directory history.
         *
         * @param dirs
         *
         */
        public void setDirectoryHistory(String[] dirs) {
                for (String d : dirs) {
                        m_pathComboBox.addItem(d);
                }
                // m_pathTextField.setText(path);
                // onPathChange();
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

                public File getFile(final int index) {
                        return m_files.get(index);
                }

                public File[] getAllFiles() {

                        return m_files.toArray(new File[getSize()]);

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

        /**
         * Shows a modal dialog to select some files
         *
         * @return the file list
         */

        public static String[] showFileChooserDialog() {
                return showFileChooserDialog(null, null);
        }

        /**
         * Shows a modal dialog to select some files
         *
         * @param file_list
         *                default files in the selected file list
         * @return the file list returns <code>null</code> if the dialog was
         *         closed with cancel, or ESC or X
         */

        public static String[] showFileChooserDialog(final String[] file_list,
                        final String directory) {

                final JDialog dlg = new JDialog();
                final int[] cancelFlag = new int[] { 0 };
                dlg.setModal(true);
                FileChooserPanel panel = new FileChooserPanel();
                if (file_list != null) {
                        panel.update(file_list);
                }
                if (directory != null) {
                        panel.setCurrentDirectory(directory);
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

        /**
         * {@inheritDoc}
         */
        @Override
        public void setEventService(EventService eventService) {
                m_eventService = eventService;

        }

}
