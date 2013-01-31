package org.knime.knip.core.ui.imgviewer.events;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.ImageMetadata;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;
import net.imglib2.type.Type;

import org.knime.knip.core.data.img.ImageMetadataImpl;

public class ImgWithMetadataChgEvent<T extends Type<T>> extends
                IntervalWithMetadataChgEvent<T> {

        private final ImageMetadata m_imageMetaData;

        public ImgWithMetadataChgEvent(RandomAccessibleInterval<T> interval,
                        Named name, Sourced source, CalibratedSpace cspace,
                        ImageMetadata imageMetaData) {
                super(interval, name, source, cspace);
                m_imageMetaData = imageMetaData;
        }

        /**
         *
         * @return metadata of the image. This might be an empty instance of
         *         {@link ImageMetadataImpl}
         */
        public ImageMetadata getImgMetaData() {
                return m_imageMetaData;
        }
}
