package org.knime.knip.core.ui.imgviewer.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.labeling.Labeling;

import org.knime.knip.core.awt.SegmentColorTable;
import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.event.EventService;
import org.knime.knip.core.ui.imgviewer.ViewerComponent;
import org.knime.knip.core.ui.imgviewer.events.ForcePlanePosEvent;
import org.knime.knip.core.ui.imgviewer.events.HilitedLabelsChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ImgRedrawEvent;
import org.knime.knip.core.ui.imgviewer.events.IntervalWithMetadataChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelPanelHiliteSelectionChgEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelPanelIsHiliteModeEvent;
import org.knime.knip.core.ui.imgviewer.events.LabelPanelVisibleLabelsChgEvent;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter;
import org.knime.knip.core.ui.imgviewer.events.RulebasedLabelFilter.Operator;

/**
 * Panel to generate a Rulebased LabelFilter.
 *
 * Publishes {@link RulebasedLabelFilter}
 *
 * @author hornm, dietzc, schoenenbergerf, ortweinm University of Konstanz
 */
public class LabelFilterPanel<L extends Comparable<L>> extends ViewerComponent {

        private static final long serialVersionUID = 1L;

        protected JList m_jLabelList;

        protected Vector<L> m_activeLabels;

        protected EventService m_eventService;

        private JScrollPane m_scrollPane;

        private RulebasedLabelFilter<L> m_filter;

        private List<JTextField> m_textFields;

        private JComboBox m_operatorBox;

        private JPanel m_textFieldsPanel;

        private Set<String> m_hilitedLabels;

        private Labeling<L> m_labeling;

        private boolean m_hMode = false; // state of highlighting mode

        private final JTabbedPane m_filterTabbs = new JTabbedPane();

        private JScrollPane m_filters;

        public LabelFilterPanel() {
                this(false);
        }

        public LabelFilterPanel(final boolean enableHilite) {
                super("Labels/Filter", false);
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                m_filter = new RulebasedLabelFilter<L>();

                m_textFields = new ArrayList<JTextField>();

                m_activeLabels = new Vector<L>();

                m_jLabelList = new JList();
                m_jLabelList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

                // TODO
                m_jLabelList.addMouseListener(new MouseAdapter() {
                        /**
                         * {@inheritDoc}
                         */
                        @Override
                        public void mousePressed(MouseEvent evt) {
                                if (evt.getButton() == MouseEvent.BUTTON3) {
                                        showMenu(evt, enableHilite);
                                }
                        }
                });

                m_jLabelList.setCellRenderer(new DefaultListCellRenderer() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public Component getListCellRendererComponent(
                                        final JList list, final Object value,
                                        final int index,
                                        final boolean isSelected,
                                        final boolean cellHasFocus) {
                                Component c = super
                                                .getListCellRendererComponent(
                                                                list, value,
                                                                index,
                                                                isSelected,
                                                                cellHasFocus);

                                c.setForeground(Color.BLACK);

                                if (m_hilitedLabels != null
                                                && m_hilitedLabels.contains(value
                                                                .toString())) {

                                        if (isSelected)
                                                c.setBackground(SegmentColorTable.HILITED_SELECTED);
                                        else
                                                c.setBackground(SegmentColorTable.HILITED);

                                } else if (isSelected) {
                                        c.setBackground(SegmentColorTable.SELECTED);
                                } else {
                                        c.setBackground(SegmentColorTable.STANDARD);
                                }

                                return c;
                        }
                });

                m_scrollPane = new JScrollPane(m_jLabelList);
                m_scrollPane.setPreferredSize(new Dimension(150, 1));
                // m_scrollPane.setSize(new Dimension(150, 1);

                JPanel confirmationPanel = new JPanel();
                confirmationPanel.setLayout(new BoxLayout(confirmationPanel,
                                BoxLayout.X_AXIS));

                m_textFieldsPanel = new JPanel();
                m_textFieldsPanel.setLayout(new BoxLayout(m_textFieldsPanel,
                                BoxLayout.Y_AXIS));

                JButton filterButton = new JButton("Filter");
                filterButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                doFilter();
                        }
                });

                JButton addButton = new JButton("+");
                addButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                m_filterTabbs.setSelectedIndex(1);
                                addTextField("");
                        }
                });

                m_operatorBox = new JComboBox(
                                RulebasedLabelFilter.Operator.values());
                m_operatorBox.setSize(new Dimension(40, 22));
                m_operatorBox.setMaximumSize(new Dimension(40, 22));

                confirmationPanel.add(addButton);
                confirmationPanel.add(m_operatorBox);
                confirmationPanel.add(filterButton);

                m_filters = new JScrollPane(m_textFieldsPanel);
                add(m_filterTabbs);
                m_filterTabbs.add("Labels", m_scrollPane);
                m_filterTabbs.add("Filter Rules", m_filters);
                add(confirmationPanel);
        }

        protected void addTextField(String initValue) {
                JPanel oneFieldRow = new JPanel();
                oneFieldRow.add(new JLabel("Rule " + (m_textFields.size() + 1)
                                + ":"));
                oneFieldRow.setLayout(new BoxLayout(oneFieldRow,
                                BoxLayout.X_AXIS));

                JTextField newField = new JTextField(initValue);
                newField.setPreferredSize(new Dimension(70, 20));
                newField.setMaximumSize(new Dimension(70, 20));
                oneFieldRow.add(newField);

                final JButton removeButton = new JButton("-");
                removeButton.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {

                                for (Component p : removeButton.getParent()
                                                .getComponents()) {

                                        if (p instanceof JTextField) {
                                                m_textFields.remove(p);
                                        }

                                }
                                m_textFieldsPanel.remove(removeButton
                                                .getParent());
                                updateUI();
                                doFilter();

                        }
                });

                oneFieldRow.add(removeButton);
                m_textFields.add(newField);
                m_textFieldsPanel.add(oneFieldRow);

                updateUI();

        }

        protected void doFilter() {
                try {
                        Set<String> allLabels = new HashSet<String>();
                        m_filter.clear();
                        for (int i = 0; i < m_textFields.size(); i++) {
                                m_filter.addRules(RulebasedLabelFilter
                                                .formatRegExp(m_textFields.get(
                                                                i).getText()));
                        }
                        m_activeLabels.clear();
                        Collection<L> filtered = m_filter
                                        .filterLabeling(m_labeling
                                                        .firstElement()
                                                        .getMapping()
                                                        .getLabels());
                        m_activeLabels.addAll(filtered);

                        for (L label : filtered) {
                                allLabels.add(label.toString());
                        }

                        // As this is faster than checking all labels
                        if (m_filter.getRules().size() == 0) {
                                m_eventService.publish(new LabelPanelVisibleLabelsChgEvent(
                                                null, null));
                                m_eventService.publish(new ImgRedrawEvent());
                        } else {
                                m_eventService.publish(new LabelPanelVisibleLabelsChgEvent(
                                                allLabels,
                                                (Operator) (m_operatorBox)
                                                                .getSelectedItem()));
                                m_eventService.publish(new ImgRedrawEvent());
                        }

                        Collections.sort(m_activeLabels);
                        m_jLabelList.setListData(m_activeLabels);
                }

                catch (NullPointerException e) {
                        JOptionPane.showMessageDialog(null,
                                        "No image selected", "Error",
                                        JOptionPane.ERROR_MESSAGE, null);
                        return;
                }

        }

        /**
         * @param axes
         * @param name
         */
        @EventListener
        public void onLabelingUpdated(
                        IntervalWithMetadataChgEvent<Labeling<L>> e) {
                m_labeling = e.getInterval();

                m_activeLabels.clear();
                for (L label : m_labeling.firstElement().getMapping()
                                .getLabels()) {
                        if (m_filter.isValid(label))
                                m_activeLabels.add(label);
                }

                Collections.sort(m_activeLabels);
                m_jLabelList.setListData(m_activeLabels);
        }

        @EventListener
        public void onHiliteChanged(HilitedLabelsChgEvent e) {
                m_hilitedLabels = new HashSet<String>(e.getHilitedLabels());
                m_jLabelList.setListData(m_activeLabels);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Position getPosition() {
                return Position.EAST;
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
                m_filter.writeExternal(out);
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {

                m_textFields.clear();
                m_textFieldsPanel.removeAll();
                m_filter = new RulebasedLabelFilter<L>();
                m_filter.readExternal(in);

                for (int s = 0; s < m_filter.getRules().size(); s++) {
                        addTextField(m_filter.getRules().get(s));
                }
        }

        @Override
        public void reset() {
                // Nothing to reset here
        }

        @Override
        public void setParent(Component parent) {
                // Nothing to do here
        }

        /**
         * Shows a contextmenu which contains highlighting options
         *
         * @param evt
         *                Mouse Event
         * @param enableHilite
         */
        public void showMenu(MouseEvent evt, boolean enableHilite) {
                JPopupMenu contextMenu = new JPopupMenu();

                JMenuItem jumpToLabel = new JMenuItem("Jump to label");
                jumpToLabel.addActionListener(new ActionListener() {

                        @SuppressWarnings("unchecked")
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                long[] min = new long[m_labeling
                                                .numDimensions()];

                                m_labeling.getRasterStart((L) m_jLabelList
                                                .getSelectedValue(), min);

                                m_eventService.publish(new ForcePlanePosEvent(
                                                min));
                                m_eventService.publish(new ImgRedrawEvent());
                        }
                });

                JMenuItem filterSelected = new JMenuItem("Filter selected");
                filterSelected.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                StringBuffer buf = new StringBuffer();
                                for (Object o : m_jLabelList
                                                .getSelectedValues()) {
                                        buf.append(o.toString() + "|");
                                }

                                if (buf.length() > 0) {
                                        m_filter.clear();
                                        m_textFieldsPanel.removeAll();
                                        m_textFields.clear();

                                        addTextField(buf.substring(0,
                                                        buf.length() - 1));
                                        doFilter();
                                }
                        }
                });

                JMenuItem clearFilters = new JMenuItem("Clear filters");
                clearFilters.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                                m_filter.clear();
                                m_textFieldsPanel.removeAll();
                                m_textFields.clear();
                                doFilter();
                        }
                });

                contextMenu.add(jumpToLabel);
                contextMenu.addSeparator();
                contextMenu.add(filterSelected);
                contextMenu.add(clearFilters);

                if (enableHilite) {
                        JMenuItem hiliteAll = new JMenuItem("HiLite All");
                        hiliteAll.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        Set<String> selection = new HashSet<String>();
                                        for (L o : m_activeLabels) {
                                                selection.add(o.toString());
                                                m_hilitedLabels.add(o
                                                                .toString());
                                        }

                                        m_eventService.publish(new LabelPanelHiliteSelectionChgEvent(
                                                        selection, true));
                                        m_eventService.publish(new ImgRedrawEvent());
                                }
                        });

                        JMenuItem clearSelected = new JMenuItem(
                                        "Clear Selected Hilite");
                        clearSelected.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        Set<String> selection = new HashSet<String>();
                                        for (Object o : m_jLabelList
                                                        .getSelectedValues()) {
                                                selection.add(o.toString());
                                                m_hilitedLabels.remove(o
                                                                .toString());
                                        }

                                        m_eventService.publish(new LabelPanelHiliteSelectionChgEvent(
                                                        selection, false));
                                        m_eventService.publish(new ImgRedrawEvent());
                                }
                        });

                        JMenuItem hiliteSelected = new JMenuItem(
                                        "HiLite Selected");
                        hiliteSelected.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        Set<String> selection = new HashSet<String>();
                                        for (Object o : m_jLabelList
                                                        .getSelectedValues()) {
                                                selection.add(o.toString());
                                                m_hilitedLabels.add(o
                                                                .toString());
                                        }

                                        m_eventService.publish(new LabelPanelHiliteSelectionChgEvent(
                                                        selection, true));
                                        m_eventService.publish(new ImgRedrawEvent());
                                }
                        });

                        JMenuItem hiliteOnly = new JMenuItem(
                                        "Show HiLited Only");
                        hiliteOnly.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        StringBuffer buf = new StringBuffer();

                                        if (m_hilitedLabels != null) {

                                                for (Object o : m_hilitedLabels) {
                                                        buf.append(o.toString()
                                                                        + "|");
                                                }

                                                if (buf.length() > 0) {
                                                        m_filter.clear();
                                                        m_textFieldsPanel
                                                                        .removeAll();
                                                        m_textFields.clear();

                                                        addTextField(buf.substring(
                                                                        0,
                                                                        buf.length() - 1));
                                                        doFilter();
                                                }
                                        }

                                }
                        });

                        JMenuItem uniliteOnly = new JMenuItem(
                                        "Show UnHiLited Only");
                        uniliteOnly.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        if (m_hilitedLabels != null) {
                                                StringBuffer buf = new StringBuffer();
                                                for (L o : m_labeling
                                                                .getLabels()) {
                                                        if (!m_hilitedLabels
                                                                        .contains(o.toString())) {
                                                                buf.append(o.toString()
                                                                                + "|");
                                                        }
                                                }

                                                if (buf.length() > 0) {
                                                        m_filter.clear();
                                                        m_textFieldsPanel
                                                                        .removeAll();
                                                        m_textFields.clear();
                                                        addTextField(buf.substring(
                                                                        0,
                                                                        buf.length() - 1));
                                                        doFilter();
                                                }
                                        }
                                }
                        });

                        JMenuItem clearAll = new JMenuItem("Clear Hilite");
                        clearAll.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                        Set<String> selection = new HashSet<String>();
                                        for (Object o : m_activeLabels) {
                                                selection.add(o.toString());
                                        }

                                        m_hilitedLabels.clear();
                                        m_eventService.publish(new LabelPanelHiliteSelectionChgEvent(
                                                        selection, false));
                                        m_eventService.publish(new ImgRedrawEvent());
                                }
                        });

                        JCheckBox hiliteMode = new JCheckBox("HiLite mode On");
                        if (m_hMode) {
                                hiliteMode.setSelected(true);
                        }

                        hiliteMode.addChangeListener(new ChangeListener() {

                                @Override
                                public void stateChanged(ChangeEvent e) {
                                        boolean old = m_hMode;
                                        m_hMode = ((JCheckBox) e.getSource())
                                                        .isSelected();

                                        if (old != m_hMode) {
                                                m_eventService.publish(new LabelPanelIsHiliteModeEvent(
                                                                m_hMode));
                                                m_eventService.publish(new ImgRedrawEvent());
                                        }
                                }
                        });

                        contextMenu.addSeparator();
                        contextMenu.add(hiliteMode);
                        contextMenu.addSeparator();

                        contextMenu.add(hiliteSelected);
                        contextMenu.add(hiliteAll);
                        contextMenu.addSeparator();

                        contextMenu.add(clearSelected);
                        contextMenu.add(clearAll);
                        contextMenu.addSeparator();

                        contextMenu.add(hiliteOnly);
                        contextMenu.add(uniliteOnly);

                        /*
                         * Disables some options if no item is selected because
                         * these options need a selected Item
                         */
                        if (m_jLabelList.isSelectionEmpty()) {
                                hiliteSelected.setEnabled(false);
                                clearSelected.setEnabled(false);
                        }
                }

                contextMenu.show(m_jLabelList, evt.getX(), evt.getY());
        }

}
