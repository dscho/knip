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
 *   12 Nov 2009 (hornm): created
 */
package org.knime.knip.core.ui.imgviewer.panels;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * JTree-Component which represents the file system as a tree.
 * 
 * @author hornm, University of Konstanz
 */
@SuppressWarnings("serial")
public class FileTree extends JTree {

    /**
     * for testing purposes
     * 
     * @param args
     */
    public static void main(final String[] args) {
        final JFrame f = new JFrame("test");
        final FileTree ft = new FileTree();
        ft.expandPath("/home/hornm/cell_images");
        f.setContentPane(ft);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);

    }

    // protected JTree m_tree;
    /*
     * The model for the JTree.
     */
    private final DefaultTreeModel m_model;

    /**
     * Creates a new file tree.
     * 
     */
    public FileTree() {
        super();

        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(
                                                                      new FileNode(new File("Computer")));

        final File[] childs = File.listRoots();

        addNodes(top, childs);

        m_model = new DefaultTreeModel(top);
        setModel(m_model);
        setRootVisible(false);

        putClientProperty("JTree.lineStyle", "Angled");

        final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getOpenIcon());
        setCellRenderer(renderer);

        final DirExpansionListener del = new DirExpansionListener();
        addTreeExpansionListener(del);

        addTreeSelectionListener(new DirSelectionListener());

        getSelectionModel().setSelectionMode(
                                             TreeSelectionModel.SINGLE_TREE_SELECTION);
        setShowsRootHandles(true);
        setEditable(false);

    }

    /**
     * Expands the tree along the given path. If the paths contains not
     * existing directories, the tree will be expanded until a non existing
     * directory appears.
     * 
     * @param path
     *                the path as a String
     */
    public void expandPath(final String path) {
        if (path == null) {
            return;
        }

        String s = path;
        DefaultMutableTreeNode node = null;
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) getModel()
                .getRoot();
        FileNode fnode;

        // operation system dependencies
        if (!System.getProperty("os.name").startsWith("Windows")) {
            s = s.replace("/", "\\");
            s = "/" + s;
        }

        // parsing the path string and creating the tree
        StringTokenizer st = new StringTokenizer(s, "\\");
        String token;

        while ((st != null) && st.hasMoreTokens()) {
            int i;
            final int num = parent.getChildCount();
            token = st.nextToken();
            for (i = 0; i < num; i++) {
                node = (DefaultMutableTreeNode) parent
                        .getChildAt(i);
                fnode = getFileNode(node);
                if (fnode.toString().startsWith(token)) {
                    if (!fnode.expand(node)
                            && (node.getChildCount() == 0)) {
                        parent = (DefaultMutableTreeNode) node
                                .getParent();
                        st = null;
                        break;
                    }
                    parent = node;
                    break;
                }
            }
            if (i >= num) {
                break;
            }

        }
        // expanding the path (JTree)
        expandPath(new TreePath(m_model.getPathToRoot(parent)));
        setSelectionPath(new TreePath(node.getPath()));
        scrollPathToVisible(new TreePath(node.getPath()));

    }

    /*
     * Utility function to add a file list as new nodes to a given root node
     */
    private void addNodes(final DefaultMutableTreeNode root,
                          final File[] childs) {

        DefaultMutableTreeNode node;
        for (int k = 0; k < childs.length; k++) {

            node = new DefaultMutableTreeNode(new FileNode(
                                                           childs[k]));
            root.add(node);
            // node.add(new DefaultMutableTreeNode(new
            // Boolean(true)));
            node.add(new DefaultMutableTreeNode(new String(
                    "Retrieving data ...")));
        }
    }

    /**
     * @param path
     * @return tree
     */
    DefaultMutableTreeNode getTreeNode(final TreePath path) {
        return (DefaultMutableTreeNode) (path.getLastPathComponent());
    }

    /**
     * @param node
     * @return node
     */
    FileNode getFileNode(final DefaultMutableTreeNode node) {
        if (node == null) {
            return null;
        }
        final Object obj = node.getUserObject();
        // if (obj instanceof IconData)
        // obj = ((IconData) obj).getObject();
        if (obj instanceof FileNode) {
            return (FileNode) obj;
        }

        return null;
    }

    /**
     * Make sure expansion is threaded and updating the tree model only
     * occurs within the event dispatching thread.
     * 
     */
    class DirExpansionListener implements TreeExpansionListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void treeExpanded(final TreeExpansionEvent event) {
            final DefaultMutableTreeNode node = getTreeNode(event
                                                            .getPath());
            final FileNode fnode = getFileNode(node);

            final Thread runner = new Thread() {
                @Override
                public void run() {
                    if ((fnode != null) && fnode.expand(node)) {
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                m_model.reload(node);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void treeCollapsed(final TreeExpansionEvent event) {
            //
        }
    }

    /**
     * 
     * @author hornm, University of Konstanz
     */
    class DirSelectionListener implements TreeSelectionListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void valueChanged(final TreeSelectionEvent event) {
            // DefaultMutableTreeNode node =
            // getTreeNode(event.getPath());
            // FileNode fnode = getFileNode(node);
            // if (fnode != null)
            // m_display.setText(fnode.getFile().getAbsolutePath());
            // else
            // m_display.setText("");
        }
    }

}

// class IconCellRenderer extends JLabel implements TreeCellRenderer {
// protected Color m_textSelectionColor;
// protected Color m_textNonSelectionColor;
// protected Color m_bkSelectionColor;
// protected Color m_bkNonSelectionColor;
// protected Color m_borderSelectionColor;
//
// protected boolean m_selected;
//
// public IconCellRenderer() {
// super();
// m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
// m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
// m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
// m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
// m_borderSelectionColor = UIManager
// .getColor("Tree.selectionBorderColor");
// setOpaque(false);
// }
//
// public Component getTreeCellRendererComponent(final JTree tree,
// final Object value, final boolean sel, final boolean expanded,
// final boolean leaf, final int row, final boolean hasFocus)
//
// {
// DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
// Object obj = node.getUserObject();
// setText(obj.toString());
//
// if (obj instanceof Boolean)
// setText("Retrieving data...");
// // setIcon(null);
//
// setFont(tree.getFont());
// setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
// setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
// m_selected = sel;
// return this;
// }
//
// @Override
// public void paintComponent(final Graphics g) {
// Color bColor = getBackground();
//
// g.setColor(bColor);
// int offset = 0;
// g.fillRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
//
// if (m_selected) {
// g.setColor(m_borderSelectionColor);
// g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
// }
// super.paintComponent(g);
// }
// }

// class IconData {
// protected Icon m_icon;
// protected Icon m_expandedIcon;
// protected Object m_data;
//
// public IconData(final Icon icon, final Object data) {
// m_icon = icon;
// m_expandedIcon = null;
// m_data = data;
// }
//
// public IconData(final Icon icon, final Icon expandedIcon, final Object data)
// {
// m_icon = icon;
// m_expandedIcon = expandedIcon;
// m_data = data;
// }
//
// public Icon getIcon() {
// return m_icon;
// }
//
// public Icon getExpandedIcon() {
// return m_expandedIcon != null ? m_expandedIcon : m_icon;
// }
//
// public Object getObject() {
// return m_data;
// }
//
// @Override
// public String toString() {
// return m_data.toString();
// }
// }

/*
 * Utility class representing a file or directory. Serves as the node object of
 * a <code>DefaultMutableTreeNode</code>.
 */

/**
 * A tree node containing the file
 */
class FileNode {
    /**
     * the file
     */
    protected File m_file;

    /**
     * creates a new FileNode object
     * 
     * @param file
     */
    public FileNode(final File file) {
        m_file = file;
    }

    /**
     * 
     * @return the file represented by this node
     */
    public File getFile() {
        return m_file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_file.getName().length() > 0 ? m_file.getName()
                : m_file.getPath();
    }

    /**
     * Expands a given root node, that is retrieving all files/directories
     * contained root directory given by the root node. Adding the retrieved
     * files as new childs to the root node.
     * 
     * @param parent
     *                the root node
     * @return true, if the expansion was successful
     */
    public boolean expand(final DefaultMutableTreeNode parent) {

        DefaultMutableTreeNode flag = null;
        try {
            flag = (DefaultMutableTreeNode) parent.getFirstChild();
        } catch (final NoSuchElementException e) {
            return false;
        }
        // if (flag == null) // No flag
        // return false;
        final Object obj = flag.getUserObject();
        if (!(obj instanceof String))
        {
            return false; // Already expanded
        }

        parent.removeAllChildren(); // Remove Flag

        final File[] files = listFiles();
        if (files == null) {
            return true;
        }

        final Vector<FileNode> v = new Vector<FileNode>();

        for (int k = 0; k < files.length; k++) {
            final File f = files[k];
            if (!(f.isDirectory())) {
                continue;
            }

            final FileNode newNode = new FileNode(f);

            boolean isAdded = false;
            for (int i = 0; i < v.size(); i++) {
                final FileNode nd = v.elementAt(i);
                if (newNode.compareTo(nd) < 0) {
                    v.insertElementAt(newNode, i);
                    isAdded = true;
                    break;
                }
            }
            if (!isAdded) {
                v.addElement(newNode);
            }
        }

        for (int i = 0; i < v.size(); i++) {
            final FileNode nd = v.elementAt(i);
            // IconData idata = new IconData(FileTree.ICON_FOLDER,
                                             // FileTree.ICON_EXPANDEDFOLDER, nd);
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                                                                           nd);
            parent.add(node);

            // if (nd.hasSubDirs())
            // node.add(new DefaultMutableTreeNode(new
            // Boolean(true)));
            if (nd.hasSubDirs()) {
                node.add(new DefaultMutableTreeNode(new String(
                                                               "Retrieving data ...")));
            }
        }

        return true;
    }

    /**
     * 
     * @return true, if this FileNode has subdirectories.
     */

    public boolean hasSubDirs() {
        final File[] files = listFiles();
        if (files == null) {
            return false;
        }
        for (int k = 0; k < files.length; k++) {
            if (files[k].isDirectory()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compares to FileNodes by means of their file names.
     * 
     * @param toCompare
     * @return result
     */
    public int compareTo(final FileNode toCompare) {
        return m_file.getName().compareToIgnoreCase(
                                                    toCompare.m_file.getName());
    }

    /**
     * Lists all files contained in the directory of represented by this
     * file node.
     * 
     * @return the file list if this node is a directory, else
     *         <code>null</code>
     */
    protected File[] listFiles() {
        if (!m_file.isDirectory()) {
            return null;
        }
        try {
            return m_file.listFiles();
        } catch (final Exception ex) {
            JOptionPane.showMessageDialog(
                                          null,
                                          "Error reading directory "
                                                  + m_file.getAbsolutePath(),
                                                  "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }
    }

}
