package org.knime.knip.core.ui.imgviewer.panels.providers;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.ops.operation.Operations;
import net.imglib2.ops.operation.SubsetOperations;
import net.imglib2.ops.operation.iterableinterval.unary.MakeHistogram;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import org.knime.knip.core.awt.AWTImageTools;
import org.knime.knip.core.ui.imgviewer.events.HistogramChgEvent;

/**
 * Creates an histogram AWTImage. Publishes a {@link HistogramChgEvent}.
 *
 * @author dietzc, hornm, University of Konstanz
 */
public class HistogramBufferedImageProvider<T extends RealType<T>> extends
                AWTImageProvider<T> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private final int m_histHeight;

        public HistogramBufferedImageProvider(int cacheSize, int histHeight) {
                super(cacheSize);

                m_histHeight = histHeight;
        }

        @Override
        protected Image createImage() {
                int[] hist = Operations
                                .compute(new MakeHistogram<T>(),
                                                Views.iterable(SubsetOperations
                                                                .subsetview(m_src,
                                                                                m_sel.getInterval(m_src))))
                                .hist();
                m_eventService.publish(new HistogramChgEvent(hist));
                return AWTImageTools.drawHistogram(hist, m_histHeight);

        }

        @Override
        public void saveComponentConfiguration(ObjectOutput out)
                        throws IOException {
                super.saveComponentConfiguration(out);
        }

        @Override
        public void loadComponentConfiguration(ObjectInput in)
                        throws IOException, ClassNotFoundException {
                super.loadComponentConfiguration(in);
        }

}
