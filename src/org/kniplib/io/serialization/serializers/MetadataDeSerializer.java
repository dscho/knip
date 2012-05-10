/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   6 Jul 2010 (hornm): created
 */
package org.kniplib.io.serialization.serializers;

import java.io.IOException;

import net.imglib2.meta.Axes;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;

import org.kniplib.io.serialization.BufferedDataInputStream;
import org.kniplib.io.serialization.BufferedDataOutputStream;

/**
 *
 * @author dietzc, hornm, University of Konstanz
 */
public class MetadataDeSerializer {

        public void serializeMetadata(Metadata metadata,
                        BufferedDataOutputStream out) throws IOException {
                serializeCalibratedSpace(metadata, out);
                serializeNamed(metadata, out);
                serializeSourced(metadata, out);
        }

        public void deserializeMetadata(Metadata metadata,
                        final BufferedDataInputStream in) throws IOException {
                deserializeCalibratedSpace(metadata, in);
                deserializeNamed(metadata, in);
                deserializeSourced(metadata, in);
        }

        public void serializeCalibratedSpace(CalibratedSpace metadata,
                        BufferedDataOutputStream out) throws IOException {

                out.writeInt(metadata.numDimensions());
                for (int d = 0; d < metadata.numDimensions(); d++) {
                        char[] label = metadata.axis(d).getLabel()
                                        .toCharArray();
                        out.writeInt(label.length);
                        out.write(label);
                        out.writeDouble(metadata.calibration(d));
                }

        }

        public void deserializeCalibratedSpace(CalibratedSpace metadata,
                        final BufferedDataInputStream in) throws IOException {
                int numDims = in.readInt();
                for (int d = 0; d < numDims; d++) {
                        char[] label = new char[in.readInt()];
                        in.read(label);
                        metadata.setAxis(Axes.get(String.valueOf(label)), d);
                        metadata.setCalibration(in.readDouble(), d);
                }
        }

        public void serializeNamed(Named metadata, BufferedDataOutputStream out)
                        throws IOException {
                char[] name = metadata.getName().toCharArray();
                out.writeInt(name.length);
                out.write(name);
        }

        public void serializeSourced(Sourced metadata,
                        BufferedDataOutputStream out) throws IOException {
                char[] source = metadata.getSource().toCharArray();
                out.writeInt(source.length);
                out.write(source);
        }

        public void deserializeNamed(Named metadata,
                        final BufferedDataInputStream in) throws IOException {
                char[] name = new char[in.readInt()];
                in.read(name);
                metadata.setName(String.valueOf(name));
        }

        public void deserializeSourced(Sourced sourced,
                        BufferedDataInputStream in) throws IOException {
                char[] source = new char[in.readInt()];
                in.read(source);
                sourced.setSource(String.valueOf(source));
        }
}
