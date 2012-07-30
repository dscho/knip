package org.knime.knip.core.io;

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
 * This Reader reads the Till Photonics *.pst files. Usally this images are
 * 12bit images. This Reader doesn't support to read subimages or just specified
 * images it only can read one image after another.
 *
 * @author Christian Lutz
 *
 */
public final class PSTReader extends FormatReader {

        // -- Constructor --

        /** Constructs a new PST reader. */
        public PSTReader() {
                super("Till Photonics", "pst");
                domains = new String[] { FormatTools.GRAPHICS_DOMAIN };
        }

        @Override
        public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
                        throws FormatException, IOException {
                FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w,
                                h);

                // reads the next buf.length bytes
                in.read(buf);

                // swap bytes here, or, better, set core[0].littleEndian to true
                // byte tmp;
                // for (int i = 0; i < buf.length; i += 2) {
                // tmp = buf[i];
                // buf[i] = buf[i + 1];
                // buf[i + 1] = tmp;
                // }
                //
                return buf;
        }

        @Override
        protected void initFile(String id) throws FormatException, IOException {

                super.initFile(id);
                in = new RandomAccessInputStream(id);

                // read meta date from *.inf file
                int length = Location.getMappedId(id).length();
                String infName = Location.getMappedId(id).substring(0,
                                length - 3)
                                + "inf";

                File infFile = new File(infName);
                if (infFile.exists()) {
                        readInf(infFile);
                } else {
                        throw new IOException(
                                        "Couldn't find corresponding *.inf file with meta data");
                }

        }

        /**
         * reads the inf-file containing information about array size(width,
         * height and number of time-frames)
         *
         * @param inf_file
         */
        private void readInf(File inf_file) throws FormatException {
                FileReader fr = null;
                BufferedReader br = null;

                int width = -1;
                int height = -1;
                int frames = -1;

                LOGGER.info("Read .inf file");

                try {
                        fr = new FileReader(inf_file);
                        br = new BufferedReader(fr);

                        Pattern width_p = Pattern.compile("Width=[0-9]+",
                                        Pattern.CASE_INSENSITIVE);
                        Pattern height_p = Pattern.compile("Height=[0-9]+",
                                        Pattern.CASE_INSENSITIVE);
                        Pattern frames_p = Pattern.compile("Frames=[0-9]+",
                                        Pattern.CASE_INSENSITIVE);

                        String line = br.readLine();
                        while (line != null) {
                                Matcher width_m = width_p.matcher(line);
                                if (width_m.matches()) {
                                        String[] n = line.split("=");
                                        width = Integer.valueOf(n[1]);
                                }

                                Matcher height_m = height_p.matcher(line);
                                if (height_m.matches()) {
                                        String[] n = line.split("=");
                                        height = Integer.valueOf(n[1]);
                                }

                                Matcher frames_m = frames_p.matcher(line);
                                if (frames_m.matches()) {
                                        String[] n = line.split("=");
                                        frames = Integer.valueOf(n[1]);
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
                                System.out.println(e);
                        }
                }

                if (width == -1 || height == -1 || frames == -1)
                        throw new FormatException(
                                        "Couldn't find height, width in the metafile");

                // set metadata for this image
                core[0].sizeX = width;
                core[0].sizeY = height;
                core[0].sizeT = frames;
                core[0].sizeC = 1;
                core[0].imageCount = frames;
                core[0].sizeZ = 1;

                core[0].rgb = false;
                core[0].pixelType = FormatTools.INT16;
                core[0].littleEndian = true;
                core[0].dimensionOrder = "XYZCT";

        }

}
