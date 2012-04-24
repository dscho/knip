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
 *   4 May 2011 (hornm): created
 */
package org.kniplib.io.serialization.serializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.NativeImgLabeling;
import net.imglib2.type.numeric.IntegerType;

import org.kniplib.io.serialization.BufferedDataInputStream;
import org.kniplib.io.serialization.BufferedDataOutputStream;

/**
 * 
 * @author hornm, University of Konstanz
 */
public class NativeImgLabelingDeSerializer {

        private static final ImgDeSerializer IMGDESERIALIZER = new ImgDeSerializer();

        /**
         * 
         * Currently supported labeling types are String, Integer, and Short.
         * 
         * @param <? extends Comparable<?>>
         * @param lab
         * @param out
         * @throws IOException
         */
        @SuppressWarnings("unchecked")
        public void serialize(Labeling<? extends IntegerType<?>> lab,
                        BufferedDataOutputStream out) throws IOException {

                IMGDESERIALIZER.serialize(
                                (((NativeImgLabeling<? extends Comparable<?>, ? extends IntegerType<?>>) lab)
                                                .getStorageImg()), out);

                // write the actual mapping
                LabelingMapping<? extends Comparable<?>> map = ((NativeImgLabeling<? extends Comparable<?>, ? extends IntegerType<?>>) lab)
                                .getMapping();
                serializeMapping(map, out);

        }

        public NativeImgLabeling<? extends Comparable<?>, ? extends IntegerType<?>> deserialize(
                        final BufferedDataInputStream in) throws IOException,
                        ClassNotFoundException {

                // Create Labeling
                NativeImgLabeling<? extends Comparable<?>, ? extends IntegerType<?>> res = new NativeImgLabeling(
                                ((Img<? extends IntegerType<?>>) IMGDESERIALIZER
                                                .deserialize(in)));

                deserializeMapping(res.getMapping(), in);

                return res;

        }

        public <L extends Comparable<L>> void serializeMapping(
                        LabelingMapping<L> map, BufferedDataOutputStream out)
                        throws IOException {

                out.writeInt(map.numLists());

                ObjectOutputStream oos = new ObjectOutputStream(
                                (OutputStream) out);

                for (int i = 0; i < map.numLists(); i++) {
                        List<L> list = map.listAtIndex(i);
                        out.writeInt(list.size());
                        for (L type : list) {
                                oos.writeObject(type);
                        }
                }

        }

        @SuppressWarnings("unchecked")
        public synchronized <L extends Comparable<L>> LabelingMapping<L> deserializeMapping(
                        LabelingMapping<L> map, BufferedDataInputStream in)
                        throws IOException {
                int numLabelComb = in.readInt();

                ObjectInputStream ois = new ObjectInputStream((InputStream) in);

                for (int i = 0; i < numLabelComb; i++) {
                        int size = in.readInt();
                        if (size != 0) {
                                List<L> list = new ArrayList<L>(size);
                                for (int j = 0; j < size; j++) {
                                        try {
                                                list.add((L) ois.readObject());
                                        } catch (ClassNotFoundException e) {
                                                throw new IOException(
                                                                "class not found exception");
                                        }
                                }
                                // TODO: We know that the list is sorted. why to
                                // sort?
                                map.intern(list);
                        }
                }

                return map;

        }

}
