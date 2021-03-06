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
package org.knime.knip.io.nodes.annotation;

import java.util.List;

import javax.swing.JPanel;

import org.knime.core.data.DataTable;
import org.knime.knip.core.ui.imgviewer.annotator.RowColKey;
import org.knime.knip.core.ui.imgviewer.overlay.Overlay;

/**
 * Decouples visual annotator components from the component that uses them.
 * Allows you to use the implemented component in dialogs, views, ... see e.g.
 * {@link DialogComponentAnnotatorView}.<br>
 * <br>
 * A AnnotatorView allows to create/alter annotations for sources from a table. A
 * source can have zero or one associated annotations. The AnnotatorView allows
 * to edit and create these annotations.
 * 
 * 
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael
 *         Zinsmaier</a>
 */
public interface AnnotatorView<T> {

	/**
	 * @return a panel that holds all components and functionality to annotate
	 *         images
	 */
	public JPanel getAnnotatorPanel();

	/**
	 * @return a list of the source names of all sources that have been annotated
	 * by the view (may also be a change of a existing annotation).
	 */
	public List<RowColKey> getIdentifiersOfManagedSources();

	/**
	 * @param key
	 *            (table based) source identifier for the associated annotation data structure (e.g. a {@link Overlay})
	 * @return the annotation data structure that is associated with the source name
	 *         or <code>null</code> if no annotation exists.
	 */
	public T getAnnotation(RowColKey key);

	/**
	 * Adds an already existing annotation to the AnnotatorView. A source that can be
	 * addressed with the given identifier has to exist in the inputTable. This method exist to
	 * allow recreation after serialization.
	 * 
	 * @param srcName
	 *            source name of the associated source
	 * @param annotation
	 *            annotation data structure
	 */
	public void setAnnotation(RowColKey key, T annotation);


	/**
	 * Deletes all managed annotations.
	 */
	public void reset();

	/**
	 * Sets the input table. The input table holds the sources that can be
	 * annotated using this component.
	 * 
	 * @param inputTable
	 *            a table that contains some image columns.
	 */
	public void setInputTable(final DataTable inputTable);

}
