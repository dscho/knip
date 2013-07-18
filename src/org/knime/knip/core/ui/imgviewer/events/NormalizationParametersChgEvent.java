package org.knime.knip.core.ui.imgviewer.events;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.iterableinterval.unary.MinMax;
import net.imglib2.ops.operation.real.unary.Normalize;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import org.knime.knip.core.ui.event.KNIPEvent;

/**
 * @author dietzc, hornm, schoenenbergerf (University of Konstanz)
 * 
 *         Event message object providing the information weather an {@link Img} of {@link RealType} should be
 *         normalized and if the number of the saturation in %
 * 
 * 
 */
public class NormalizationParametersChgEvent implements Externalizable, KNIPEvent {

    /* Value of the saturation in % */
    private double m_saturation;

    /* Weather the img shall be normalized or not */
    private boolean m_isNormalized;

    /**
     * Empty constructor for serialization
     */
    public NormalizationParametersChgEvent() {
        this(0.0, false);
    }

    @Override
    public ExecutionPriority getExecutionOrder() {
        return ExecutionPriority.NORMAL;
    }

    /**
     * implements object equality {@inheritDoc}
     */
    @Override
    public <E extends KNIPEvent> boolean isRedundant(final E thatEvent) {
        return this.equals(thatEvent);
    }

    /**
     * Constructor for the message object NormalizationParameters
     * 
     * @param saturation Saturation value for the normalization
     * 
     * @param isNormalized Weather the image shall be normalized or not
     */
    public NormalizationParametersChgEvent(final double saturation, final boolean isNormalized) {
        m_saturation = saturation;
        m_isNormalized = isNormalized;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 31 + (m_isNormalized ? 1 : 2);
        final long bits = Double.doubleToLongBits(m_saturation);
        hash = (hash * 31) + (int)(bits ^ (bits >>> 32));
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
     * Helper method to get the actual normalization factor and the boolean variable to check wether the {@link Img} of
     * type {@link RealType} shall be normalized or not. If image should not be normalized the normalization parameters
     * will be set to 1 respecticly 2
     * 
     * @param src {@link Img} of {@link RealType} which shall be normalized
     * @param sel {@link PlaneSelectionEvent} the selected plane in the source {@link Img}
     * 
     * @return [0]: the normalization factor, [1]: the local minimum
     */
    public <T extends RealType<T>> double[] getNormalizationParameters(final RandomAccessibleInterval<T> src,
                                                                       final PlaneSelectionEvent sel) {
        if (!m_isNormalized) {
            return new double[]{1.0, Views.iterable(src).firstElement().getMinValue()};
        } else {
            final T element = src.randomAccess().get().createVariable();
            final ValuePair<T, T> oldMinMax =
                    Operations.compute(new MinMax<T>(m_saturation, element),
                                       Views.iterable(SubsetOperations.subsetview(src, sel.getInterval(src))));
            return new double[]{
                    Normalize.normalizationFactor(oldMinMax.a.getRealDouble(), oldMinMax.b.getRealDouble(),
                                                  element.getMinValue(), element.getMaxValue()),
                    oldMinMax.a.getRealDouble()};
        }

    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        m_saturation = in.readDouble();
        m_isNormalized = in.readBoolean();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeDouble(m_saturation);
        out.writeBoolean(m_isNormalized);
    }
}
