package org.knime.knip.core.ui.imgviewer.panels.infobars;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.labeling.LabelingType;
import net.imglib2.meta.TypedAxis;
import net.imglib2.meta.TypedSpace;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.core.ui.event.EventListener;
import org.knime.knip.core.ui.imgviewer.events.ImgAndLabelingChgEvent;
import org.knime.knip.core.ui.imgviewer.events.ViewClosedEvent;

/**
 *
 *
 *
 * @author dietzc
 */
public class ImgLabelingViewInfoPanel<T extends RealType<T>, L extends Comparable<L>> extends
        ViewInfoPanel<LabelingType<L>> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private RandomAccess<T> m_imgRA = null;

    private String m_imageInfo = "";

    public ImgLabelingViewInfoPanel() {
    }

    @Override
    protected String updateMouseLabel(final StringBuffer buffer, final Interval interval, final TypedSpace<? extends TypedAxis> axes,
                                      final RandomAccess<LabelingType<L>> rndAccess, final long[] coords) {

        if ((interval == null) || (m_imgRA == null)) {
            return "";
        }

        buffer.setLength(0);

        for (int i = 0; i < coords.length; i++) {
            buffer.append(" ");
            if (i < interval.numDimensions()) {
                buffer.append(axes != null ? axes.axis(i).type().getLabel() : i);
            }
            if (coords[i] == -1) {
                buffer.append("[ Not set ];");
            } else {
                buffer.append("[" + (coords[i] + 1) + "/" + interval.dimension(i) + "];");
            }
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        final StringBuffer valueBuffer = new StringBuffer();
        if ((coords[0] != -1) && (coords[1] != -1) && (coords.length == m_imgRA.numDimensions())) {
            rndAccess.setPosition(coords);
            m_rndAccess.setPosition(coords);
            m_imgRA.setPosition(coords);
            valueBuffer.append("Img: [" + m_imgRA.get().toString() + "]");

            valueBuffer.append(" Labeling: [");
            if (m_rndAccess.get().getLabeling().size() > 0) {
                for (final L label : m_rndAccess.get().getLabeling()) {
                    valueBuffer.append(label.toString() + ";");
                }
                valueBuffer.deleteCharAt(valueBuffer.length() - 1);
                valueBuffer.append("]");
            } else {
                valueBuffer.append("EmptyLabel]");
            }

        } else {
            valueBuffer.append("Not set");
        }

        buffer.append("; value=");
        buffer.append(valueBuffer.toString());

        return buffer.toString();
    }

    @Override
    protected String updateImageLabel(final StringBuffer buffer, final Interval interval,
                                      final RandomAccess<LabelingType<L>> rndAccess, final String imgName) {

        buffer.setLength(0);
        if ((imgName != null) && (imgName.length() > 0)) {
            buffer.append(imgName);
        }

        m_imageInfo = buffer.toString();

        if ((interval == null) || (m_imgRA == null)) {
            return "loading..";
        } else {
            return m_imageInfo;
        }
    }

    /**
     * @param lab
     * @param axes
     * @param name
     */
    @EventListener
    public void onImgChanged(final ImgAndLabelingChgEvent<T, L> e) {
        m_imgRA =
                Views.extendValue(e.getRandomAccessibleInterval(), e.getIterableInterval().firstElement())
                        .randomAccess();

        super.manualTextUpdate("", m_imageInfo);
    }

    @Override
    @EventListener
    public void onClose(final ViewClosedEvent ev) {
        super.onClose(ev);
        m_imgRA = null;
    }

    @Override
    public void saveComponentConfiguration(final ObjectOutput out) throws IOException {
        // Nothing to do here
    }

    @Override
    public void loadComponentConfiguration(final ObjectInput in) throws IOException {
        // Nothing to do here
    }

}