/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * --------------------------------------------------------------------- *
 *
 */
package org.knime.knip.base.node;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataValue;
import org.knime.core.node.DynamicNodeFactory;
import org.knime.core.node.NodeSetFactory;
import org.knime.core.node.NodeView;
import org.knime.knip.base.nodes.view.TableCellViewNodeView;
import org.knime.node2012.KnimeNodeDocument;
import org.knime.node2012.KnimeNodeDocument.KnimeNode;

/**
 * Node factory mapping one data value to a data cell. Please note that if this factory is used, the node has to be
 * registered at a extension point using ONLY the {@link NodeSetFactory} class. Registering this class directly will NOT
 * work so far.
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 *
 * @param <VIN>
 * @param <M>
 */
public abstract class GenericValueToCellNodeFactory<VIN extends DataValue, M extends ValueToCellNodeModel<VIN, ? extends DataCell>>
        extends DynamicNodeFactory<M> {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    protected final void addNodeDescription(final KnimeNodeDocument doc) {
        createNodeDescription(doc);

        KnimeNode node = doc.getKnimeNode();
        if (node == null) {
            XMLNodeUtils.addXMLNodeDescriptionTo(doc, this.getClass());
            node = doc.getKnimeNode();
        }

        // Load if possible
        if (node != null) {

            // add description of "this" dialog
            ValueToCellNodeDialog.addTabsDescriptionTo(node.getFullDescription());
            TableCellViewNodeView.addViewDescriptionTo(node.addNewViews());

            if (node.getPorts() == null) {
                ValueToCellNodeDialog.addPortsDescriptionTo(node);
            }

            // Add user stuff
            addNodeDescriptionContent(node);
        }
    }

    /**
     * Overwrite this method to add additional details programmatically to the already existing node description
     * (created either from an xml file or in
     * {@link GenericValueToCellNodeFactory#createNodeDescription(KnimeNodeDocument)}.
     *
     * @param node
     */
    protected void addNodeDescriptionContent(final KnimeNode node) {
        // Nothing to do here
    }

    /**
     * Overwrite this method if you want to create the node description programmatically. A description in the xml file
     * named after the derived class will not be used.
     *
     * @param doc
     */
    protected void createNodeDescription(final KnimeNodeDocument doc) {
        // May be overwritten
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<M> createNodeView(final int viewIndex, final M nodeModel) {
        return new TableCellViewNodeView<M>(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    protected final ValueToCellNodeDialog<VIN> createNodeDialogPane() {
        return createNodeDialog();
    }

    /**
     *
     * @return the new dialog
     */
    protected abstract ValueToCellNodeDialog<VIN> createNodeDialog();

}
