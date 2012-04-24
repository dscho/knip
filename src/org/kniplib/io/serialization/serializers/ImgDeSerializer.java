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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgPlus;
import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.DataAccess;
import net.imglib2.img.list.ListImg;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

import org.kniplib.io.serialization.BufferedDataInputStream;
import org.kniplib.io.serialization.BufferedDataOutputStream;

/**
 * 
 * @author hornm, dietzc University of Konstanz
 */
public class ImgDeSerializer {

        private static NativeImgDeSerializer NATIVEIMGDESERIALIZER = new NativeImgDeSerializer();

        // private static ListImgDeSerializer LISTIMGDESERIALIZER = new
        // ListImgDeSerializer();

        private enum SupportedImgs {
                NATIVEIMG, LISTIMG;
        };

        public synchronized void serialize(Img<? extends Type<?>> img,
                        BufferedDataOutputStream out) throws IOException {

                img = findSourceImg(img);

                if (img instanceof NativeImg) {
                        out.writeInt(SupportedImgs.NATIVEIMG.ordinal());
                        NATIVEIMGDESERIALIZER
                                        .serialize(out,
                                                        (NativeImg<? extends NativeType<?>, ? extends DataAccess>) img);
                } else if (img instanceof ListImg) {
                        out.writeInt(SupportedImgs.LISTIMG.ordinal());
                        serializeListImg(out, (ListImg<? extends Type<?>>) img);
                } else {
                        throw new IllegalArgumentException(
                                        "This type of image is can't be serialized, yet! Supported types are: NativeImg, ListImg");
                }

        }

        public Img<? extends Type<?>> findSourceImg(Img<? extends Type<?>> img) {

                while (img instanceof ImgPlus) {
                        img = ((ImgPlus<? extends Type<?>>) img).getImg();
                }

                return img;
        }

        public Img<? extends Type<?>> deserialize(
                        final BufferedDataInputStream in) throws IOException,
                        ClassNotFoundException {

                switch (SupportedImgs.values()[in.readInt()]) {
                case NATIVEIMG:
                        return NATIVEIMGDESERIALIZER.deserialize(in);
                case LISTIMG:
                        return deserializeListImg(in);
                default:
                        throw new IllegalArgumentException(
                                        "This type of image is can't be deserialized, yet! Supported types are: NativeImg, ListImg");
                }

        }

        /* serialization issues */
        public static synchronized void writeClass(
                        BufferedDataOutputStream out, String className)
                        throws IOException {
                out.writeInt(className.length());
                out.writeChars(className);
        }

        /* serialization issues */
        public static synchronized Object readClass(BufferedDataInputStream in)
                        throws IOException {
                char[] chars = new char[in.readInt()];
                in.read(chars);

                try {
                        return Class.forName(String.copyValueOf(chars))
                                        .newInstance();
                } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                }
        }

        /*
         * LISTIMG
         */
        private void serializeListImg(BufferedDataOutputStream out,
                        ListImg<? extends Type<?>> img) throws IOException {

                writeClass(out, img.firstElement().getClass().getName());

                // write dimensions
                out.writeInt(img.numDimensions());
                for (int i = 0; i < img.numDimensions(); i++) {
                        out.writeLong(img.dimension(i));
                }
                out.writeLong(img.size());

                ObjectOutputStream stream = new ObjectOutputStream(out);
                Cursor<? extends Type<?>> cursor = img.cursor();

                while (cursor.hasNext()) {
                        stream.writeObject(cursor.next());
                }

        }

        private Img<? extends Type<?>> deserializeListImg(
                        BufferedDataInputStream in) throws IOException,
                        ClassNotFoundException {

                // RealType< ? > type = ( RealType< ? > ) readClass( in );

                long[] dims = new long[in.readInt()];
                in.read(dims);

                long numElements = in.readLong();

                ObjectInputStream stream = new ObjectInputStream(in);

                List list = new ArrayList((int) numElements);
                for (int i = 0; i < numElements; i++) {
                        list.add(stream.readObject());
                }

                return new ListImg(list, dims);
        }
}
