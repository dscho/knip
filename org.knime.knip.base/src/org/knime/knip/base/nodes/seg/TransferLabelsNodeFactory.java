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
package org.knime.knip.base.nodes.seg;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.knip.base.data.labeling.LabelingCell;
import org.knime.knip.base.data.labeling.LabelingCellFactory;
import org.knime.knip.base.data.labeling.LabelingValue;
import org.knime.knip.base.node.TwoValuesToCellNodeDialog;
import org.knime.knip.base.node.TwoValuesToCellNodeFactory;
import org.knime.knip.base.node.TwoValuesToCellNodeModel;

/**
 * TODO Auto-generated
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class TransferLabelsNodeFactory<T extends IntegerType<T>, L extends Comparable<L>, II extends IntegerType<II>>
        extends TwoValuesToCellNodeFactory<LabelingValue<L>, LabelingValue<L>> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected TwoValuesToCellNodeDialog<LabelingValue<L>, LabelingValue<L>> createNodeDialog() {
        return new TwoValuesToCellNodeDialog<LabelingValue<L>, LabelingValue<L>>() {
            @Override
            public void addDialogComponents() {
                //
            }

            @Override
            protected String getFirstColumnSelectionLabel() {
                return "Source Labeling";
            }

            @Override
            protected String getSecondColumnSelectionLabel() {
                return "Labeling to be relabeled";
            }
        };
    }

    @Override
    public TwoValuesToCellNodeModel<LabelingValue<L>, LabelingValue<L>, LabelingCell<L>> createNodeModel() {
        return new TwoValuesToCellNodeModel<LabelingValue<L>, LabelingValue<L>, LabelingCell<L>>() {

            private LabelingCellFactory m_labCellFactory;

            @Override
            protected void addSettingsModels(final List<SettingsModel> settingsModels) {
                //

            }

            @Override
            protected LabelingCell<L> compute(final LabelingValue<L> cellValue1, final LabelingValue<L> cellValue2)
                    throws Exception {

                final Img<T> storageImg = ((NativeImgLabeling<L, T>)cellValue2.getLabeling()).getStorageImg().copy();
                final LabelingMapping<L> src = cellValue1.getLabeling().firstElement().getMapping();
                final LabelingMapping<L> target = cellValue2.getLabeling().firstElement().getMapping();

                final LabelingMapping<L> res = new LabelingMapping<L>(storageImg.firstElement().createVariable()) {
                    {
                        //delete empty list
                        super.internedLists.clear();
                        super.listsByIndex.clear();
                    }
                };
                for (int i = 0; i < Math.max(src.numLists(), target.numLists()); i++) {

                    if (i < src.numLists() && i < target.numLists()) {
                        res.intern(src.listAtIndex(i));
                    } else if (i < target.numLists()) {
                        //if we encounter another empty labeling list,
                        //treat it differently, as the result mapping would be to short
                        //otherwise (res.intern(...) would not add the empty labeling to the list)
                        if (target.listAtIndex(i).size() == 0) {
                            //TODO: no solution yet, how to deal with it!!!
                            //should only occur in rare cases
                        }
                        res.intern(target.listAtIndex(i));
                    } else {
                        break;
                    }
                }
                assert target.numLists() == res.numLists();

                Labeling<L> resLab = new NativeImgLabeling<L, T>(storageImg) {
                    {
                        super.mapping = res;
                    }
                };
                return m_labCellFactory.createCell(resLab, cellValue2.getLabelingMetadata());
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected void prepareExecute(final ExecutionContext exec) {
                m_labCellFactory = new LabelingCellFactory(exec);
            }
        };
    }
}
