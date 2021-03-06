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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeLogger;
import org.knime.node.v28.KnimeNodeDocument.KnimeNode;
import org.knime.node2012.KnimeNodeDocument;

/**
 * Helper class essentially to parse and add the xml-file content to XMLBeans objects representing the node description
 * xml (e.g. {@link KnimeNode})
 *
 * @author <a href="mailto:dietzc85@googlemail.com">Christian Dietz</a>
 * @author <a href="mailto:horn_martin@gmx.de">Martin Horn</a>
 * @author <a href="mailto:michael.zinsmaier@googlemail.com">Michael Zinsmaier</a>
 */
public class XMLNodeUtils {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(XMLNodeUtils.class);

    private static final String NAMESPACE = "http://knime.org/node2012";

    /**
     * Adds the xml content of the file *NodeFactory.xml in the same package to the xml bean (KnimeNodeDocument).
     *
     * @param doc
     * @param factoryClass
     */
    @SuppressWarnings("rawtypes")
    public static void addXMLNodeDescriptionTo(final KnimeNodeDocument doc,
                                               final Class<? extends NodeFactory> factoryClass) {

        final ClassLoader loader = factoryClass.getClassLoader();
        InputStream propInStream;
        String path;
        Class<?> clazz = factoryClass;

        do {
            path = clazz.getPackage().getName();
            path = path.replace('.', '/') + "/" + clazz.getSimpleName() + ".xml";

            propInStream = loader.getResourceAsStream(path);
            clazz = clazz.getSuperclass();
        } while ((propInStream == null) && (clazz != Object.class));

        if (propInStream != null) {
            try {
                addXMLNodeDescriptionTo(doc, propInStream);
            } catch (final XmlException e) {
                LOGGER.coding("Node description for node " + factoryClass.getSimpleName()
                        + " can not be read from xml stream.", e);
            } catch (final IOException e) {
                LOGGER.coding("Node description for node " + factoryClass.getSimpleName()
                        + " can not be read from xml file.", e);
            }
        }
    }

    /**
     * Adds the xml content from the stream to the xml bean node description.
     *
     * @param doc
     * @param inStream
     * @throws XmlException
     * @throws IOException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void addXMLNodeDescriptionTo(final KnimeNodeDocument doc, final InputStream inStream)
            throws XmlException, IOException {
        final XmlOptions xmlopts = new XmlOptions();
        final Map map = new HashMap(2);
        map.put("", NAMESPACE);
        xmlopts.setLoadSubstituteNamespaces(map);
        final Map suggestedPrefixes = new HashMap(2);
        suggestedPrefixes.put(NAMESPACE, "");
        xmlopts.setSaveSuggestedPrefixes(suggestedPrefixes);
        final KnimeNodeDocument test = KnimeNodeDocument.Factory.parse(inStream, xmlopts);
        doc.addNewKnimeNode().set(test.getKnimeNode());
    }

    private XMLNodeUtils() {
        // utility class
    }
}
