package org.kniplib.ui.imgviewer.panels.infobars;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.RandomAccess;
import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.kniplib.ui.event.EventListener;
import org.kniplib.ui.imgviewer.events.ImgAndLabelingChgEvent;

/**
 *
 *
 *
 * @author dietzc
 */
public class ImgLabelingViewInfoPanel<T extends RealType<T>, L extends Comparable<L>>
                extends ViewInfoPanel<LabelingType<L>, Labeling<L>> {

        /**
	 *
	 */
        private static final long serialVersionUID = 1L;

        private RandomAccess<T> m_imgRA = null;

        /**
         * {@inheritDoc}
         */
        @Override
        protected String updateLabel(Labeling<L> labeling, StringBuffer buffer,
                        CalibratedSpace axes,
                        RandomAccess<LabelingType<L>> rndAccess, long[] coords) {
                if (labeling == null || m_imgRA == null)
                        return "Loading ...";

                buffer.setLength(0);

                for (int i = 0; i < coords.length; i++) {
                        buffer.append(" ");
                        if (i < labeling.numDimensions()) {
                                buffer.append(axes != null ? axes.axis(i)
                                                .getLabel() : i);
                        }
                        if (coords[i] == -1) {
                                buffer.append("[ Not set ];");
                        } else {
                                buffer.append("[" + (coords[i] + 1) + "/"
                                                + labeling.dimension(i) + "];");
                        }
                }
                if (buffer.length() > 0) {
                        buffer.deleteCharAt(buffer.length() - 1);
                }

                String val;
                if (coords[0] != -1 && coords[1] != -1
                                && coords.length == m_imgRA.numDimensions()) {
                        rndAccess.setPosition(coords);
                        m_rndAccess.setPosition(coords);
                        m_imgRA.setPosition(coords);
                        val = "Img: [" + m_imgRA.get().toString() + "]";

                        val += " Labeling: [";
                        if (m_rndAccess.get().getLabeling().size() > 0) {
                                for (L label : m_rndAccess.get().getLabeling()) {
                                        val += label.toString() + ";";
                                }
                                val = val.substring(0, val.length() - 1);
                                val += "]";
                        } else {
                                val += "EmptyLabel]";
                        }

                } else {
                        val = "Not set";
                }

                buffer.append("; value=");
                buffer.append(val);
                // buffer.append("; type=");
                // buffer.append(rndAccess.get().getClass().getSimpleName());

                return buffer.toString();
        }

        /**
         * @param lab
         * @param axes
         * @param name
         */
        @EventListener
        public void onImgChanged(ImgAndLabelingChgEvent<T, L> e) {
                m_imgRA = Views.extendValue(e.getInterval(),
                                e.getInterval().firstElement()).randomAccess();
        }

        public ImgLabelingViewInfoPanel() {
                //
        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                // Nothing to do here
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException {
                // Nothing to do here
        }

}
