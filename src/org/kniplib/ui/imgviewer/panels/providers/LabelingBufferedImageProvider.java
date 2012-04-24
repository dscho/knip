package org.kniplib.ui.imgviewer.panels.providers;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import net.imglib2.labeling.Labeling;
import net.imglib2.labeling.LabelingType;

import org.kniplib.awt.renderer.LabelingRenderer;
import org.kniplib.ui.event.EventListener;
import org.kniplib.ui.imgviewer.events.AWTImageChgEvent;
import org.kniplib.ui.imgviewer.events.ImgChgEvent;
import org.kniplib.ui.imgviewer.events.LabelPanelVisibleLabelsChgEvent;
import org.kniplib.ui.imgviewer.events.RulebasedLabelFilter.Operator;

/**
 * Creates an awt image from a plane selection, labeling and labeling renderer.
 * Propagates {@link AWTImageChgEvent}.
 *
 * @author hornm, University of Konstanz
 */
public class LabelingBufferedImageProvider<L extends Comparable<L>> extends
                AWTImageProvider<LabelingType<L>, Labeling<L>> {

        /**
	 *
	 */
        private static final long serialVersionUID = 1L;

        protected Set<String> m_activeLabels;

        protected Operator m_operator;

        public LabelingBufferedImageProvider(int cacheSize) {
                super(cacheSize);
        }

        @Override
        @EventListener
        public void onUpdated(ImgChgEvent<Labeling<L>> e) {
                super.onUpdated(e);
        }

        @Override
        protected int generateHashCode() {

                int hash = super.generateHashCode();

                if (m_activeLabels != null) {
                        hash *= 31;
                        hash += m_activeLabels.hashCode();
                        hash *= 31;
                        hash += m_operator.ordinal();
                }

                return hash;
        }

        @Override
        protected Image createImage() {

                return ((LabelingRenderer<L>) m_renderer).render(m_src,
                                m_sel.getPlaneDimIndex1(),
                                m_sel.getPlaneDimIndex2(), m_sel.getPlanePos(),
                                1.0, m_activeLabels, m_operator);
        }

        @EventListener
        public void onUpdate(LabelPanelVisibleLabelsChgEvent e) {
                m_activeLabels = e.getLabels();
                m_operator = e.getOperator();
                super.renderAndCacheImg();

        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                super.saveComponentConfiguration(out);
                out.writeObject(m_activeLabels);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                super.loadComponentConfiguration(in);
                m_activeLabels = (Set<String>) in.readObject();
        }
}
