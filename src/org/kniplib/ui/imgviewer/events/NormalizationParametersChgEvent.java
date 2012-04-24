package org.kniplib.ui.imgviewer.events;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.img.Img;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.numeric.RealType;

import org.kniplib.ops.img.ImgNormalize;
import org.kniplib.ui.event.KNIPEvent;

/**
 * @author dietzc, hornm, schoenenbergerf (University of Konstanz)
 *
 *         Event message object providing the information weather an {@link Img}
 *         of {@link RealType} should be normalized and if the number of the
 *         saturation in %
 *
 * @param <T>
 *                The {@link RealType} of the {@link Img} which shall be
 *                normalized
 *
 */
public class NormalizationParametersChgEvent<T extends RealType<T>> implements
                Externalizable, KNIPEvent {

        /* Value of the saturation in % */
        private double m_saturation;

        /* Weather the img shall be normalized or not */
        private boolean m_isNormalized;

        private ImgNormalize<T, Img<T>> m_normalizer;

        /**
         * Empty constructor for serialization
         */
        public NormalizationParametersChgEvent() {
                this(0.0, false);
        }

        /**
         * Constructor for the message object NormalizationParameters
         *
         * @param saturation
         *                Saturation value for the normalization
         *
         * @param isNormalized
         *                Weather the image shall be normalized or not
         */
        public NormalizationParametersChgEvent(double saturation, boolean isNormalized) {
                m_normalizer = new ImgNormalize<T, Img<T>>();
                m_saturation = saturation;
                m_isNormalized = isNormalized;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
                int hash = 31 + (m_isNormalized ? 1 : 2);
                long bits = Double.doubleToLongBits(m_saturation);
                hash = hash * 31 + (int) (bits ^ (bits >>> 32));
                return hash;
        }

        /**
         * @return the saturation
         */
        public final double getSaturation() {
                return m_saturation;
        }

        /**
         * @return weather the {@link Img} shall be normalized or not
         */
        public final boolean isNormalized() {
                return m_isNormalized;
        }

        /**
         * Helper method to get the actual normalization factor and the boolean
         * variable to check weather the {@link Img} of type {@link RealType}
         * shall be normalized or not. If image should not be normalized the
         * normalization parameters will be set to 1 respecticly 2
         *
         * @param src
         *                {@link Img} of {@link RealType} which shall be
         *                normalized
         * @param sel
         *                {@link PlaneSelectionEvent} the selected plane in the
         *                source {@link Img}
         *
         * @return [0]: the normalization factor, [1]: the local minimum
         */
        public double[] getNormalizationParameters(Img<T> src,
                        PlaneSelectionEvent sel) {
                if (!m_isNormalized)
                        return new double[] { 1.0,
                                        src.firstElement().getMinValue() };
                else {
                        return m_normalizer.getNormalizationProperties(
                                        new SubImg<T>(src,
                                                        sel.getInterval(src),
                                                        false), m_saturation);
                }

        }

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                        ClassNotFoundException {
                m_saturation = in.readDouble();
                m_isNormalized = in.readBoolean();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
                out.writeDouble(m_saturation);
                out.writeBoolean(m_isNormalized);
        }
}
