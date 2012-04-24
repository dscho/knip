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
 *   2 Mar 2010 (hornm): created
 */
package org.kniplib.io;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.PlanarAccess;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgFactory;
import net.imglib2.io.ImgIOException;
import net.imglib2.iterator.IntervalIterator;
import net.imglib2.meta.Axes;
import net.imglib2.meta.AxisType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.IntervalIndexer;
import ome.xml.model.primitives.PositiveFloat;

import org.apache.commons.logging.LogFactory;
import org.kniplib.awt.renderer.RGBImgRenderer;
import org.kniplib.io.reference.ImgSource;
import org.kniplib.ops.interval.MergeIntervals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link ImgSource} to read image referenced by a
 * filename/location (see {@link ImageFileReference}).
 *
 * TODO: metadata (imgInfo?!) different storage strategies (currently only
 * PlaneImg read)
 *
 * @author hornm, University of Konstanz
 */
public class BioFormatsImgSource<V> implements ImgSource {

        /**
         * ID of the source
         */
        public static final String SOURCE_ID = "fileimgsource";

        /* Logger */
        private final Logger LOGGER = LoggerFactory
                        .getLogger(BioFormatsImgSource.class);

        /* Bioformats image reader */
        private IFormatReader m_reader = null;

        private static ClassList<IFormatReader> m_classList = null;

        /* the current image reference */
        private String m_ref;

        /* Metadata of the img */
        private IMetadata m_meta;

        /* Interval merger */
        private final MergeIntervals m_intervalMerger = new MergeIntervals();

        /**
         * Constructor for the BioFormats ImageSource
         */
        public BioFormatsImgSource() {
                if (m_classList == null) {
                        try {
                                m_classList = new ClassList<IFormatReader>(
                                                "readers.txt",
                                                IFormatReader.class,
                                                BioFormatsImgSource.class);
                        } catch (IOException e) {
                                m_classList = ImageReader
                                                .getDefaultReaderClasses();
                        }
                }
        }

        @Override
        public long[] getDimensions(final String ref) throws FormatException,
                        IOException {
                setImageReference(ref);
                final long sizeX = m_reader.getSizeX();
                final long sizeY = m_reader.getSizeY();
                final long sizeZ = m_reader.getSizeZ();
                final long sizeC = m_reader.getEffectiveSizeC();
                final long sizeT = m_reader.getSizeT();
                // final String[] cDimTypes = r.getChannelDimTypes();
                // final int[] cDimLengths = r.getChannelDimLengths();
                final String dimOrder = m_reader.getDimensionOrder();
                final List<Long> dimLengthsList = new ArrayList<Long>();

                // add core dimensions
                for (int i = 0; i < dimOrder.length(); i++) {
                        final char dim = dimOrder.charAt(i);
                        switch (dim) {
                        case 'X':
                                if (sizeX > 1)
                                        dimLengthsList.add(sizeX);
                                break;
                        case 'Y':
                                if (sizeY > 1)
                                        dimLengthsList.add(sizeY);
                                break;
                        case 'Z':
                                if (sizeZ > 1)
                                        dimLengthsList.add(sizeZ);
                                break;
                        case 'T':
                                if (sizeT > 1)
                                        dimLengthsList.add(sizeT);
                                break;
                        case 'C':
                                // for (int c = 0; c < cDimLengths.length; c++)
                                // {
                                // final long len = cDimLengths[c];
                                // if (len > 1)
                                // dimLengthsList.add(len);
                                // }
                                if (sizeC > 1) {
                                        dimLengthsList.add(sizeC);
                                }
                                break;
                        }
                }

                // convert result to primitive array
                final long[] dimLengths = new long[dimLengthsList.size()];
                for (int i = 0; i < dimLengths.length; i++) {
                        dimLengths[i] = dimLengthsList.get(i);
                }
                return dimLengths;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AxisType[] getAxes(String ref) throws Exception {
                setImageReference(ref);
                final int sizeX = m_reader.getSizeX();
                final int sizeY = m_reader.getSizeY();
                final int sizeZ = m_reader.getSizeZ();
                final int sizeC = m_reader.getEffectiveSizeC();
                final int sizeT = m_reader.getSizeT();
                final String dimOrder = m_reader.getDimensionOrder();
                LOGGER.debug("Image Reader Dimension Order: " + dimOrder);

                LogFactory.getLog(getClass()).warn("commons");
                final List<AxisType> dimTypes = new ArrayList<AxisType>();

                // add core dimensions
                for (final char dim : dimOrder.toCharArray()) {
                        switch (dim) {
                        case 'X':
                                if (sizeX > 1)
                                        dimTypes.add(Axes.get("X"));
                                break;
                        case 'Y':
                                if (sizeY > 1)
                                        dimTypes.add(Axes.get("Y"));
                                break;
                        case 'Z':
                                if (sizeZ > 1)
                                        dimTypes.add(Axes.get("Z"));
                                break;
                        case 'T':
                                if (sizeT > 1)
                                        dimTypes.add(Axes.get("T"));
                                break;
                        case 'C':
                                if (sizeC > 1) {
                                        dimTypes.add(Axes.get("C"));
                                }
                                break;
                        }
                }

                return dimTypes.toArray(new AxisType[0]);
        }

        @Override
        public String getName(final String ref) throws Exception {

                int idx = ref.lastIndexOf("\\");
                if (idx == -1)
                        idx = ref.lastIndexOf("/");

                return ref.substring(idx + 1, ref.length());
        }

        /**
         * A temporial function.
         *
         * @return
         *
         */

        public String getOMEXMLMetadata() {
                return MetadataTools.getOMEXML(m_meta);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
                try {
                        m_reader.close();
                } catch (IOException e) {
                        LOGGER.warn("Exception while closing reader");
                }

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
                return "File";
        }

        @Override
        public <T extends RealType<T>> Img<T> getImg(String ref,
                        Interval[] srcIntervals) throws Exception {
                setImageReference(ref);
                try {

                        // Checking weather the srcIntervals are zero. If so,
                        // there is an
                        // one sized array created containing an interval
                        // describing the
                        // complete image
                        if (srcIntervals == null || srcIntervals.length == 0) {
                                srcIntervals = new FinalInterval[] { new FinalInterval(
                                                getDimensions(ref)) };
                        }

                        // Initializing the type of the image
                        @SuppressWarnings("unchecked")
                        T type = (T) makeType(m_reader.getPixelType());
                        Interval[] mergedIntervals = m_intervalMerger
                                        .compute(srcIntervals);

                        // Checking if everything was fine calculated
                        if (mergedIntervals.length != srcIntervals.length)
                                throw new RuntimeException(
                                                "MergedIntervals and SourceIntervals must be of the same size");

                        long[] dims = new long[getDimensions(ref).length];
                        getMergedIntervalDimensionality(mergedIntervals, dims);

                        // Creating the resulting image
                        PlanarImg planarImg = new PlanarImgFactory().create(
                                        dims, (NativeType) type);

                        // The image data is read in
                        readPlanes(srcIntervals, mergedIntervals, getAxes(ref),
                                        planarImg);

                        return planarImg;

                } catch (final FormatException e) {
                        LOGGER.warn("Couldn't read in img "
                                        + m_reader.getCurrentFile()
                                        + " due to a FormatException");
                        throw new ImgIOException(e);
                } catch (final IOException e) {
                        LOGGER.warn("Couldn't read in img "
                                        + m_reader.getCurrentFile()
                                        + " due to a IOException");
                        throw new ImgIOException(e);
                }
        }

        /**
         * {@inheritDoc}
         *
         */
        @Override
        public <T extends RealType<T>> Img<T> getImg(final String ref)
                        throws Exception {
                return getImg(ref, (Interval[]) null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T extends RealType<T>> Img<T> getImg(String ref,
                        Interval interval) throws Exception {
                return getImg(ref, new Interval[] { interval });
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public BufferedImage getThumbnail(String ref, int height)
                        throws Exception {
                long[] dims = getDimensions(ref);
                long[] max = new long[dims.length];
                max[0] = dims[0] - 1;
                max[1] = dims[1] - 1;
                for (int i = 2; i < dims.length; i++) {
                        if (dims[i] == 2 || dims[i] == 3) {
                                max[i] = dims[i] - 1;
                        }
                }

                Img<? extends RealType> img = getImg(ref, new FinalInterval(
                                new long[dims.length], max));
                return new RGBImgRenderer().render(img, 0, 1,
                                new long[dims.length], (double) height
                                                / dims[1]);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends RealType<T>> T getSourceType(String ref)
                        throws Exception {
                setImageReference(ref);
                return (T) makeType(m_reader.getPixelType());
        }

        /**
         * @param ref
         * @return number of images contained in the specified file
         * @throws Exception
         */
        public int getSeriesCount(String ref) throws Exception {
                setImageReference(ref);
                return m_reader.getSeriesCount();
        }

        /**
         * Sets the series to be read next.
         *
         * @param ref
         * @param no
         * @throws Exception
         */
        public void setSeries(String ref, int no) throws Exception {
                setImageReference(ref);
                m_reader.setSeries(no);
        }

        // ///////////////////////////////////////////
        // ///////Helper Methods ////////////////////
        // //////////////////////////////////////////

        /** Converts Bio-Formats pixel type to imglib Type object. */
        public static Object makeType(final int pixelType) {
                final RealType<?> type;
                switch (pixelType) {
                case FormatTools.UINT8:
                        type = new UnsignedByteType();
                        break;
                case FormatTools.INT8:
                        type = new ByteType();
                        break;
                case FormatTools.UINT16:
                        type = new UnsignedShortType();
                        break;
                case FormatTools.INT16:
                        type = new ShortType();
                        break;
                case FormatTools.UINT32:
                        type = new UnsignedIntType();
                        break;
                case FormatTools.INT32:
                        type = new IntType();
                        break;
                case FormatTools.FLOAT:
                        type = new FloatType();
                        break;
                case FormatTools.DOUBLE:
                        type = new DoubleType();
                        break;
                default:
                        type = null;
                }
                return type;
        }

        /*
         * Helper that updates the image reference if necessary.
         */
        private void setImageReference(final String ref)
                        throws FormatException, IOException {

                if (m_reader == null) {
                        m_reader = initializeReader(ref);
                        m_ref = ref;

                } else if (ref.equals(m_ref)
                                && m_reader.getCurrentFile() != null) {
                        return;
                }

                try {
                        m_reader.setId(ref);
                } catch (Exception e) {
                        initializeReader(ref);

                }
                m_ref = ref;
        }

        /* Constructs and initializes a Bio-Formats reader for the given file. */
        private IFormatReader initializeReader(final String id)
                        throws FormatException, IOException {

                IFormatReader r = null;

                r = new ImageReader(m_classList);
                r = new ChannelFiller(r);
                r = new ChannelSeparator(r);

                // attach OME-XML metadata object to reader
                try {
                        final ServiceFactory factory = new ServiceFactory();
                        final OMEXMLService service = factory
                                        .getInstance(OMEXMLService.class);
                        m_meta = service.createOMEXMLMetadata();
                        r.setMetadataStore(m_meta);
                } catch (final ServiceException e) {
                        throw new FormatException(e);
                } catch (final DependencyException e) {
                        throw new FormatException(e);
                }

                r.setId(id);

                return r;
        }

        /*
         * Calculates the overall dimensionality of a set of orthogonal
         * intervals. You might use MergeIntervals Operation to merge your
         * source intervals. Please be sure that your source intervals are
         * orthogonal.
         *
         * @param mergedIntervals Merged intervals from a set of orthogonal,
         * unmerged intervals
         *
         * @param dims Container for the result
         *
         * @return
         */
        private final void getMergedIntervalDimensionality(
                        Interval[] mergedIntervals, long[] dims) {

                if (dims == null)
                        throw new RuntimeException("dims must not be null");

                for (Interval i : mergedIntervals) {
                        if (i.numDimensions() != dims.length)
                                throw new RuntimeException(
                                                "All merged intervals must have the same size");

                        for (int d = 0; d < i.numDimensions(); d++)
                                dims[d] = Math.max(dims[d],
                                                i.min(d) + i.dimension(d));
                }

        }

        /**
         * Reads planes from the given initialized {@link IFormatReader} into
         * the specified {@link Img}.
         */
        private <T extends RealType<T> & NativeType<T>> void readPlanes(
                        Interval[] srcIntervals, Interval[] mergedIntervals,
                        AxisType[] axes, final PlanarImg<T, ?> dest)
                        throws FormatException, IOException {

                // min and max for the iterator are calculated

                long[] base = new long[dest.numDimensions()];
                long[] minSrc = new long[dest.numDimensions()];
                long[] minRes = new long[dest.numDimensions()];
                long[] max = new long[dest.numDimensions()];

                long[] resDims = new long[dest.numDimensions()];
                dest.dimensions(resDims);
                resDims[0] = 1;
                resDims[1] = 1;

                byte[] plane = null;
                IntervalIterator iterator;
                for (int i = 0; i < mergedIntervals.length; i++) {

                        Interval resInterval = mergedIntervals[i];
                        Interval srcInterval = srcIntervals[i];

                        // min & max of interval are resolved to iterate over
                        // the interval
                        resInterval.min(minRes);
                        srcInterval.min(minSrc);
                        srcInterval.max(max);

                        for (int m = 2; m < max.length; m++)
                                max[m] -= minSrc[m];

                        max[0] = 0;
                        max[1] = 0;

                        // Iterating over the planes of the src img
                        iterator = new IntervalIterator(base, max);

                        long[] srcPlanePos = new long[srcInterval
                                        .numDimensions()];
                        long[] resPlanePos = new long[resInterval
                                        .numDimensions()];

                        while (iterator.hasNext()) {
                                iterator.fwd();
                                iterator.localize(srcPlanePos);
                                iterator.localize(resPlanePos);

                                for (int k = 2; k < minSrc.length; k++) {
                                        srcPlanePos[k] += minSrc[k];
                                        resPlanePos[k] += minRes[k];
                                }

                                // Plane number in the source
                                int srcPlaneNo = getPlaneNo(srcPlanePos, axes);
                                if (plane == null)
                                        plane = m_reader.openBytes(srcPlaneNo);
                                else
                                        m_reader.openBytes(srcPlaneNo, plane);

                                // Write the plane into the resulting image

                                populatePlane((int) IntervalIndexer.positionToIndex(
                                                resPlanePos, resDims), plane,
                                                dest);
                        }

                }

                m_reader.close();
        }

        /** Populates plane by reference using {@link PlanarAccess} interface. */
        private <T extends RealType<T> & NativeType<T>> void populatePlane(
                        final int resPlaneNo, final byte[] plane,
                        PlanarImg<T, ?> planarImg) {
                final int pixelType = m_reader.getPixelType();
                final int bpp = FormatTools.getBytesPerPixel(pixelType);
                final boolean fp = FormatTools.isFloatingPoint(pixelType);
                final boolean little = m_reader.isLittleEndian();
                DataTools.fillDataArray(planarImg.getPlane(resPlaneNo)
                                .getCurrentStorageArray(), plane, bpp, fp,
                                little);

        }

        /* Gets the plane number from the 5-dim position array */
        private int getPlaneNo(long[] pos, AxisType[] axes) {

                final long[] zctPos = new long[3];

                int i = 0;
                for (AxisType a : axes) {
                        final char dim = a.getLabel().charAt(0);
                        switch (dim) {
                        case 'Z':
                                zctPos[0] = pos[i];
                                break;
                        case 'C':
                                zctPos[1] = pos[i];
                                break;
                        case 'T':
                                zctPos[2] = pos[i];
                                break;
                        }

                        i++;
                }
                return m_reader.getIndex((int) zctPos[0], (int) zctPos[1],
                                (int) zctPos[2]);
        }

        /** Compiles an N-dimensional list of calibration values. */
        private double[] getCalibration(final IFormatReader r) {
                final long sizeX = r.getSizeX();
                final long sizeY = r.getSizeY();
                final long sizeZ = r.getSizeZ();
                final long sizeT = r.getSizeT();
                final int[] cDimLengths = r.getChannelDimLengths();
                final String dimOrder = r.getDimensionOrder();

                final IMetadata meta = (IMetadata) r.getMetadataStore();
                PositiveFloat xCal = meta.getPixelsPhysicalSizeX(0);
                PositiveFloat yCal = meta.getPixelsPhysicalSizeY(0);
                PositiveFloat zCal = meta.getPixelsPhysicalSizeZ(0);
                PositiveFloat tCal = new PositiveFloat(
                                meta.getPixelsTimeIncrement(0) == null ? Double.NaN
                                                : meta.getPixelsTimeIncrement(0));
                if (xCal == null)
                        xCal = new PositiveFloat(Double.NaN);
                if (yCal == null)
                        yCal = new PositiveFloat(Double.NaN);
                if (zCal == null)
                        zCal = new PositiveFloat(Double.NaN);

                final List<PositiveFloat> calibrationList = new ArrayList<PositiveFloat>();

                // add core dimensions
                for (int i = 0; i < dimOrder.length(); i++) {
                        final char dim = dimOrder.charAt(i);
                        switch (dim) {
                        case 'X':
                                if (sizeX > 1)
                                        calibrationList.add(xCal);
                                break;
                        case 'Y':
                                if (sizeY > 1)
                                        calibrationList.add(yCal);
                                break;
                        case 'Z':
                                if (sizeZ > 1)
                                        calibrationList.add(zCal);
                                break;
                        case 'T':
                                if (sizeT > 1)
                                        calibrationList.add(tCal);
                                break;
                        case 'C':
                                for (int c = 0; c < cDimLengths.length; c++) {
                                        final long len = cDimLengths[c];
                                        if (len > 1)
                                                calibrationList.add(new PositiveFloat(
                                                                Double.NaN));
                                }
                                break;
                        }
                }

                // convert result to primitive array
                final double[] calibration = new double[calibrationList.size()];
                for (int i = 0; i < calibration.length; i++) {
                        calibration[i] = calibrationList.get(i).getValue();
                }
                return calibration;
        }

        @Override
        public double[] getCalibration(String ref) throws Exception {
                setImageReference(ref);
                return getCalibration(m_reader);
        }

        @Override
        public String getSource(String imgRef) throws Exception {
                return imgRef;
        }
}
