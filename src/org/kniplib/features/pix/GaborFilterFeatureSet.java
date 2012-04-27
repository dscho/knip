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
 *   10 May 2010 (hornm): created
 */
package org.kniplib.features.pix;

import net.imglib2.Cursor;
import net.imglib2.ExtendedRandomAccessibleInterval;
import net.imglib2.RandomAccess;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.ops.image.UnaryConstantRightAssignment;
import net.imglib2.ops.image.UnaryOperationAssignment;
import net.imglib2.ops.operation.binary.real.RealAdd;
import net.imglib2.ops.operation.unary.mixed.ComplexImaginaryToRealAdapter;
import net.imglib2.ops.operation.unary.mixed.ComplexRealToRealAdapter;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.real.DoubleType;

import org.kniplib.features.FeatureSet;
import org.kniplib.features.FeatureTargetListener;
import org.kniplib.filter.linear.Gabor;
import org.kniplib.ops.fft.DirectConvolver;
import org.kniplib.ops.fft.ImageConvolution;
import org.kniplib.ops.fft.ImgLibImageConvolution;
import org.kniplib.ops.img.ImgConvert;
import org.kniplib.ops.img.ImgNormalize;
import org.kniplib.ops.img.ImgUtils;
import org.kniplib.ops.iterable.Sum;
import org.kniplib.types.ImgConversionTypes;

/**
 *
 * @author hornm, University of Konstanz
 * @param <T>
 *                image type
 */
public class GaborFilterFeatureSet<T extends RealType<T>> implements FeatureSet {

        private Img<DoubleType>[] m_filters;

        private Cursor<DoubleType>[] m_filterCursors;

        private String[] m_names;

        private Img<DoubleType>[] m_convolvedImgs;

        private RandomAccess<DoubleType>[] m_convolvedImgRandAccess;

        private RandomAccess<DoubleType> m_tmpRandAccess;

        private int m_currentAngID;

        private int m_numFeatures;

        private int m_halfNumAng;

        /* settings fields, to be serialized to restore this factory */
        private boolean m_precalcFeatures = true;

        private double[] m_scales;

        private double[] m_frequencies;

        private double[] m_elongations;

        private int m_radius;

        private int m_numAng;

        private long[] m_pos;

        private Img<T> m_img;

        private boolean m_averageAngles = false;

        /**
         * The user must not call this constructor.
         */
        public GaborFilterFeatureSet() {
                // no-op
        }

        public GaborFilterFeatureSet(final double[] scales,
                        final double[] frequencies, final double[] elongations,
                        final int radius, final int numAng) {
                this(scales, frequencies, elongations, radius, numAng, true,
                                false);
        }

        /**
         * @param scales
         * @param frequencies
         * @param elongations
         * @param radius
         * @param numAng
         * @param precalcFeatures
         * @param averageAngles
         *                averages over the given number of angles, thus,
         *                updating the angle has no effect
         */
        public GaborFilterFeatureSet(final double[] scales,
                        final double[] frequencies, final double[] elongations,
                        final int radius, final int numAng,
                        boolean precalcFeatures, boolean averageAngles) {
                super();

                m_scales = scales;
                m_frequencies = frequencies;
                m_elongations = elongations;
                m_numAng = numAng;
                m_precalcFeatures = precalcFeatures;
                m_radius = radius;
                m_averageAngles = averageAngles;

                init();
        }

        @SuppressWarnings("unchecked")
        private void init() {

                m_halfNumAng = m_numAng / 2;

                // the total number of filters including the varied angles
                int numFilters = m_halfNumAng * m_scales.length
                                * m_frequencies.length * m_elongations.length
                                * 2;

                // create filter for the individual parameters
                m_filters = new Img[numFilters];
                m_names = new String[numFilters / m_halfNumAng];

                // calculating the filter for each angle, scale, frequency,
                // elongation
                // and symmetry (even or odd).
                UnaryOperationAssignment<ComplexDoubleType, DoubleType> realAssignment = new UnaryOperationAssignment(
                                new ComplexRealToRealAdapter());

                UnaryOperationAssignment<ComplexDoubleType, DoubleType> imaginaryAssignment = new UnaryOperationAssignment(
                                new ComplexImaginaryToRealAdapter());

                int filterIndex = 0;
                for (int a = 0; a < m_halfNumAng; a++) {
                        float ang = -a * (float) Math.PI
                                        * (1.0f / m_halfNumAng);
                        for (int s = 0; s < m_scales.length; s++) {
                                for (int f = 0; f < m_frequencies.length; f++) {
                                        for (int e = 0; e < m_elongations.length; e++) {
                                                Gabor g = new Gabor(
                                                                m_radius,
                                                                ang,
                                                                m_scales[s],
                                                                m_frequencies[f],
                                                                m_elongations[e]);
                                                for (int evenodd = 0; evenodd < 2; evenodd++) {

                                                        try {
                                                                m_filters[filterIndex] = g
                                                                                .factory()
                                                                                .imgFactory(new DoubleType())
                                                                                .create(g,
                                                                                                new DoubleType());

                                                                if (evenodd == 0) {
                                                                        realAssignment.compute(
                                                                                        g,
                                                                                        m_filters[filterIndex]);
                                                                } else {
                                                                        imaginaryAssignment
                                                                                        .compute(g,
                                                                                                        m_filters[filterIndex]);
                                                                }

                                                        } catch (IncompatibleTypeException e1) {
                                                                // TODO handle
                                                                // exception
                                                                e1.printStackTrace();
                                                        }

                                                        if (evenodd == 0) {
                                                                double sub = new Sum<DoubleType>()
                                                                                .compute(m_filters[filterIndex],
                                                                                                new DoubleType())
                                                                                .get()
                                                                                / (g.dimension(0) * g
                                                                                                .dimension(1));
                                                                new UnaryConstantRightAssignment(
                                                                                new RealAdd())
                                                                                .compute(m_filters[filterIndex],
                                                                                                new DoubleType(
                                                                                                                (float) -sub),
                                                                                                m_filters[filterIndex]);

                                                                // m_filters[filterIndex]
                                                                // .uAdd(-m_filters[filterIndex].aSum()
                                                                // /
                                                                // (m_filters[filterIndex]
                                                                // .size(0) *
                                                                // m_filters[filterIndex]
                                                                // .size(1)));
                                                        }

                                                        // System.out.println(filterIndex
                                                        // + " "
                                                        // + getFilterIndex(s,
                                                        // f, e, evenodd == 0,
                                                        // a));

                                                        m_names[filterIndex++
                                                                        % (m_filters.length / m_halfNumAng)] = "Gabor[s="
                                                                        + m_scales[s]
                                                                        + ";f="
                                                                        + m_frequencies[f]
                                                                        + ";e="
                                                                        + m_elongations[e]
                                                                        + ";"
                                                                        + (evenodd == 0 ? "even"
                                                                                        : "odd")
                                                                        + "]";

                                                }
                                        }

                                }
                        }

                }

                if (!m_precalcFeatures) {
                        m_filterCursors = new Cursor[m_filters.length];
                        for (int i = 0; i < m_filters.length; i++) {
                                m_filterCursors[i] = m_filters[i]
                                                .localizingCursor();
                        }
                }

                m_numFeatures = m_filters.length / m_halfNumAng;

        }

        // private int getFilterIndex(final int scale, final int freq, final int
        // elon,
        // final boolean even, final int angle) {
        // return angle * m_scales.length * m_frequencies.length
        // * m_elongations.length * 2 + scale * m_frequencies.length
        // * m_elongations.length * 2 + freq * m_elongations.length * 2
        // + elon * 2 + (even ? 0 : 1);
        // }

        @FeatureTargetListener
        public void angleUpdated(Double angle) {
                m_currentAngID = (int) (Math.round(angle / (2 * Math.PI)
                                * m_numAng))
                                % (m_numAng);

        }

        @FeatureTargetListener
        public void imgUpdated(Img<T> img) {
                m_img = img;
                if (m_precalcFeatures) {
                        fftConvolveImage();
                } else {

                        ImgConvert<T, DoubleType> imgConvert = new ImgConvert<T, DoubleType>(
                                        img.firstElement().createVariable(),
                                        new DoubleType(),
                                        ImgConversionTypes.DIRECT);
                        Img<DoubleType> srcImg = imgConvert.compute(img,
                                        imgConvert.createEmptyOutput(img));
                        DoubleType min = srcImg.firstElement().createVariable();
                        min.setReal(min.getMinValue());

                        OutOfBoundsConstantValueFactory<DoubleType, Img<DoubleType>> oobf = new OutOfBoundsConstantValueFactory<DoubleType, Img<DoubleType>>(
                                        min);
                        m_tmpRandAccess = new ExtendedRandomAccessibleInterval<DoubleType, Img<DoubleType>>(
                                        srcImg, oobf).randomAccess();
                }
        }

        @FeatureTargetListener
        public void posUpdated(long[] pos) {
                m_pos = pos;
        }

        private final long[] m_kernelRadii = new long[] { m_radius, m_radius };

        /**
         * {@inheritDoc}
         */
        @Override
        public double value(int id) {
                double res = -Double.MAX_VALUE; // if (x < m_filterSizeX / 2 ||
                                                // y
                                                // <
                // m_filterSizeY / 2
                // || x >= getFeatureTarget().getSizeX() - m_filterSizeX / 2
                // || y >= getFeatureTarget().getSizeY() - m_filterSizeY / 2) {
                // // cursor.getType().set((float)
                // cursor.getType().getMinValue());
                // return res;
                // }
                if (m_precalcFeatures) {
                        // res = (float) m_convolvedImgs[m_currentAngID %
                        // m_numAng *
                        // m_numFeatures
                        // + getCurrentFeatureID()].get(x - m_filterSizeX / 2, y
                        // - m_filterSizeY / 2);
                        if (m_averageAngles) {
                                for (int a = 0; a < m_halfNumAng; a++) {
                                        m_tmpRandAccess = m_convolvedImgRandAccess[a
                                                        % m_halfNumAng
                                                        * m_numFeatures + id];
                                        m_tmpRandAccess.setPosition(m_pos[0], 0);
                                        m_tmpRandAccess.setPosition(m_pos[1], 1);
                                        res = Math.max(m_tmpRandAccess.get()
                                                        .getRealFloat(), res);
                                        res = Math.max(-m_tmpRandAccess.get()
                                                        .getRealFloat(), res);
                                }

                        } else {
                                m_tmpRandAccess = m_convolvedImgRandAccess[m_currentAngID
                                                % m_halfNumAng
                                                * m_numFeatures
                                                + id];
                                m_tmpRandAccess.setPosition(m_pos[0], 0);
                                m_tmpRandAccess.setPosition(m_pos[1], 1);
                                res = m_tmpRandAccess.get().getRealFloat();

                                // if the current filter is odd and the choosen
                                // angle bigger
                                // than
                                // numAng (only covering a half circle due to
                                // the symmetry of
                                // the
                                // filters)
                                if (id % 2 == 1
                                                && m_currentAngID >= m_halfNumAng) {
                                        res *= -1;
                                }
                        }

                } else {
                        if (m_averageAngles) {
                                for (int a = 0; a < m_halfNumAng; a++) {
                                        double tmp = DirectConvolver
                                                        .convolve(m_tmpRandAccess,
                                                                        m_filterCursors[a
                                                                                        % m_halfNumAng
                                                                                        * m_numFeatures
                                                                                        + id],
                                                                        m_pos,
                                                                        m_kernelRadii);
                                        res = Math.max(tmp, res);
                                        res = Math.max(-tmp, res);
                                }
                        } else {
                                res = DirectConvolver.convolve(m_tmpRandAccess,
                                                m_filterCursors[m_currentAngID
                                                                % m_halfNumAng
                                                                * m_numFeatures
                                                                + id], m_pos,
                                                m_kernelRadii);

                                // if the current filter is odd and the choosen
                                // angle bigger
                                // than
                                // numAng (only covering a half circle due to
                                // the symmetry of
                                // the
                                // filters)
                                if (id % 2 == 1
                                                && m_currentAngID >= m_halfNumAng) {
                                        res *= -1;
                                }
                        }
                }

                return res;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String name(int id) {
                return m_names[id];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int numFeatures() {
                return m_names.length;
        }

        // TODO: Is this right?!?!

        @SuppressWarnings("unchecked")
        private void fftConvolveImage() {

                m_convolvedImgs = new Img[m_filters.length];
                m_convolvedImgRandAccess = new RandomAccess[m_filters.length];

                ImageConvolution<DoubleType, DoubleType, Img<DoubleType>, Img<DoubleType>> ics = new ImgLibImageConvolution<DoubleType, DoubleType>(
                                (int) Math.ceil(Runtime.getRuntime()
                                                .availableProcessors() / 2));
                // ImageConvolution<DoubleType, DoubleType> ics = new
                // SSTImageConvolution<DoubleType, DoubleType>();
                ImgConvert<T, DoubleType> imgConvert = new ImgConvert<T, DoubleType>(
                                m_img.firstElement().createVariable(),
                                new DoubleType(), ImgConversionTypes.DIRECT);
                Img<DoubleType> srcImg = imgConvert.compute(m_img,
                                imgConvert.createEmptyOutput(m_img));

                for (int i = 0; i < m_filters.length; i++) {
                        ics.setKernel(m_filters[i]);
                        m_convolvedImgs[i] = ics.compute(srcImg,
                                        ImgUtils.createEmptyImg(srcImg));
                        m_convolvedImgRandAccess[i] = m_convolvedImgs[i]
                                        .randomAccess();

                        // Image<DoubleType> img = m_convolvedImgs[i].clone();
                        // ContrastEnhancer.enhance(ContrastEnhancementType.NORMALIZE,
                        // 0,
                        // img.createLocalizableCursor());
                        // AWTImageTools.showInFrame(img, "conv", .7);
                }
        }

        public void showFilterBank() {

                for (int i = 0; i < m_filters.length; i++) {
                        Img<DoubleType> img = m_filters[i];
                        int angIdx = i / (m_filters.length / m_numAng);
                        double ang = angIdx * Math.PI * (1.0 / m_halfNumAng);
                        String name = m_names[i
                                        % (m_filters.length / m_halfNumAng)]
                                        + "angIdx:" + angIdx + ";ang:" + ang;

                        // ContrastEnhancer.enhance(ContrastEnhancementType.NORMALIZE,
                        // 0,
                        // img.createLocalizableCursor(), img.getNumPixels());
                        new ImgNormalize<DoubleType, Img<DoubleType>>(0)
                                        .compute(img, img);

                        org.kniplib.awt.AWTImageTools.showInFrame(img, name, 5);
                        System.out.println(name
                                        + ": "
                                        + new Sum<DoubleType>().compute(img,
                                                        new DoubleType()).get());

                }

        }

        // /**
        // * {@inheritDoc}
        // */
        // @Override
        // public void readExternal(ObjectInput in) throws IOException,
        // ClassNotFoundException {
        // super.readExternal(in);
        //
        // m_precalcFeatures = in.readBoolean();
        // m_radius = in.readInt();
        // m_numAng = in.readInt();
        //
        // m_scales = new double[in.readInt()];
        // for (int i = 0; i < m_scales.length; i++) {
        // m_scales[i] = in.readDouble();
        // }
        //
        // m_frequencies = new double[in.readInt()];
        // for (int i = 0; i < m_frequencies.length; i++) {
        // m_frequencies[i] = in.readDouble();
        // }
        //
        // m_elongations = new double[in.readInt()];
        // for (int i = 0; i < m_elongations.length; i++) {
        // m_elongations[i] = in.readDouble();
        // }
        // init();
        // }

        // /**
        // * {@inheritDoc}
        // */
        // @Override
        // public void writeExternal(ObjectOutput out) throws IOException {
        // super.writeExternal(out);
        // out.writeBoolean(m_precalcFeatures);
        // out.writeInt(m_radius);
        // out.writeInt(m_numAng);
        //
        // out.writeInt(m_scales.length);
        // for (int i = 0; i < m_scales.length; i++) {
        // out.writeDouble(m_scales[i]);
        // }
        //
        // out.writeInt(m_frequencies.length);
        // for (int i = 0; i < m_frequencies.length; i++) {
        // out.writeDouble(m_frequencies[i]);
        // }
        //
        // out.writeInt(m_elongations.length);
        // for (int i = 0; i < m_elongations.length; i++) {
        // out.writeDouble(m_elongations[i]);
        // }
        //
        // }

        /**
         * {@inheritDoc}
         */
        @Override
        public String featureSetId() {
                return "Gabor Filter Feature Factory";
        };

        public static <T extends RealType<T>> void main(final String[] args) {

                // final double[] scales, final double[] frequencies,
                // final double[] elongations, final int radius, final int
                // numAng,
                // boolean precalcFeatures

                // Image<T> img = ImageTools
                // .loadImage("/home/hornm/cell_images/ChHauk-GFP-FAK.tif");
                // Image<DoubleType> fimg = new Convert<T, DoubleType>(new
                // DoubleType(),
                // true)
                // .proc(img);

                GaborFilterFeatureSet<DoubleType> gfb = new GaborFilterFeatureSet<DoubleType>(
                                new double[] { .5f, .75f, 1 }, new double[] {
                                                .5f, 1 }, new double[] { 1 },
                                20, 8, true, false);
                // gfb.getFeatureTarget().setImage(fimg);

                gfb.showFilterBank();
                // gfb.setImage(img);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void enable(int id) {
                // nothing to do here

        }
}
