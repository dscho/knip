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
 */
package org.kniplib.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import loci.common.Location;
import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;

/**
 * 
 * 
 * @author Clemens Müthing (clemens.muething@uni-konstanz.de)
 */
public class RAWReader extends FormatReader {

        /**
         * {@inheritDoc}
         * 
         * @see Object#DATReader()
         */
        public RAWReader() {
                // TODO: wie hieß das jetzt noch mal?
                super("Mesiovis", "raw");
                // TODO: was ist diese domain ??
                domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
        }

        public boolean first = false;

        @Override
        public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
                        throws FormatException, IOException {
                FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w,
                                h);

                // reads the next buf.length bytes
                for (int i = 0; i < buf.length; i += 1) {
                        buf[i] = in.readByte();
                }

                return buf;
        }

        @Override
        protected void initFile(String id) throws FormatException, IOException {

                super.initFile(id);
                in = new RandomAccessInputStream(id);

                // read meta date from *.dat file
                int length = Location.getMappedId(id).length();
                String infName = Location.getMappedId(id).substring(0,
                                length - 3)
                                + "dat";

                File infFile = new File(infName);
                if (infFile.exists()) {
                        readInf(infFile);
                } else {
                        throw new IOException(
                                        "Couldn't find corresponding *.dat file with meta data");
                }

        }

        /**
         * reads the dat-file containing information about array size(width,
         * height and number of time-frames)
         * 
         * @param dat_file
         */
        private void readInf(File dat_file) throws FormatException {
                FileReader fr = null;
                BufferedReader br = null;

                int x = -1;
                int y = -1;
                int z = -1;

                LOGGER.debug("Read .dat file");

                try {
                        fr = new FileReader(dat_file);
                        br = new BufferedReader(fr);

                        Pattern sizeP = Pattern.compile("Resolution.*",
                                        Pattern.CASE_INSENSITIVE);
                        // TODO add pattern for slice thickness
                        // TODO add pattern for type

                        String line = br.readLine();
                        while (line != null) {
                                Matcher sizeM = sizeP.matcher(line);
                                if (sizeM.matches()) {
                                        String[] n = line.split(":");

                                        String[] split = n[1].trim().split(" ");
                                        x = Integer.valueOf(split[0]);
                                        y = Integer.valueOf(split[1]);
                                        z = Integer.valueOf(split[2]);
                                }

                                line = br.readLine();
                        }
                } catch (FileNotFoundException fnfe) {
                        System.out.println(fnfe);
                } catch (IOException ioe) {
                        System.out.println(ioe);
                } finally {
                        try {
                                if (fr != null)
                                        fr.close();
                        } catch (IOException e) {
                                System.err.println(e);
                        }
                }

                if (x == -1 || y == -1 || z == -1) {
                        throw new FormatException(
                                        "Couldn't find height, width in the metafile");
                }

                // set metadata for this image
                core[0].sizeX = x;
                core[0].sizeY = y;
                core[0].sizeZ = z;
                core[0].sizeT = 1;
                core[0].sizeC = 1;
                core[0].imageCount = z; // needed, otherwise the fillmetadata
                                        // methods
                // will fail
                core[0].bitsPerPixel = 1;

                core[0].rgb = false;
                core[0].pixelType = FormatTools.DOUBLE;
                // TODO: Is this little or big endian data?
                core[0].littleEndian = true;
                core[0].dimensionOrder = "XYZCT";
        }

        @Override
        public int getRGBChannelCount() {
                return 1;
        }

}
