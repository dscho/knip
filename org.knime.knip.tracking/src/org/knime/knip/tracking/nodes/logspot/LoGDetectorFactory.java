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
package org.knime.knip.tracking.nodes.logspot;

import java.util.List;

import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.RealType;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeDialog;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeFactory;
import org.knime.knip.base.node.ImgPlusToImgPlusNodeModel;

/**
 * {@link NodeFactory} for {@link LoGDetectorFactory}
 * 
 * @param <T>
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 */
public class LoGDetectorFactory<T extends RealType<T> & NativeType<T>> extends
		ImgPlusToImgPlusNodeFactory<T, BitType> {

	static SettingsModelIntegerBounded createSpanModel() {
		return new SettingsModelIntegerBounded("radius", 5, 0,
				Integer.MAX_VALUE);
	}

	@Override
	protected ImgPlusToImgPlusNodeDialog<T> createNodeDialog() {
		return new ImgPlusToImgPlusNodeDialog<T>(2, 3, "X", "Y") {

			@Override
			public void addDialogComponents() {
				addDialogComponent("Options", "", new DialogComponentNumber(
						createSpanModel(), "Radius of Spots", 5));
			}
		};
	}

	@Override
	public ImgPlusToImgPlusNodeModel<T, BitType> createNodeModel() {
		return new ImgPlusToImgPlusNodeModel<T, BitType>("X", "Y") {

			private SettingsModelInteger m_radiusModel = createSpanModel();

			@Override
			protected UnaryOutputOperation<ImgPlus<T>, ImgPlus<BitType>> op(
					final ImgPlus<T> imgPlus) {
				return new LoGDetectorOp<T>(m_radiusModel.getIntValue());
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			protected void prepareExecute(final ExecutionContext exec) {
				enableParallelization(false);
				super.prepareExecute(exec);
			}

			@Override
			protected void addSettingsModels(
					final List<SettingsModel> settingsModels) {
				settingsModels.add(m_radiusModel);
			}

			@Override
			protected int getMinDimensions() {
				return 2;
			}
		};
	}
}