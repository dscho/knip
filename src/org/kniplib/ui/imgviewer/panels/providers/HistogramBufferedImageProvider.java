package org.kniplib.ui.imgviewer.panels.providers;

import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import net.imglib2.img.Img;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.numeric.RealType;

import org.kniplib.awt.AWTImageTools;
import org.kniplib.ops.iterable.MakeHistogram;
import org.kniplib.ui.imgviewer.events.HistogramChgEvent;

/**
 * Creates an histogram awt image. Pusblishes a {@link HistogramChgEvent}.
 *
 * @author dietzc, hornm, University of Konstanz
 */
public class HistogramBufferedImageProvider<T extends RealType<T>, I extends Img<T>>
                extends AWTImageProvider<T, I> {

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
                int[] hist = new MakeHistogram<T>().compute(
                                new SubImg<T>(m_src, m_sel.getInterval(m_src),
                                                false)).hist();
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
