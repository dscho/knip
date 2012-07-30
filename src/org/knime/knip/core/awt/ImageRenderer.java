package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ScreenImage;
import net.imglib2.type.Type;

public interface ImageRenderer<T extends Type<T>, SRC extends RandomAccessibleInterval<T>> {

        @Override
        public String toString();

        public ScreenImage render(SRC source, int dimX, int dimY,
                        long[] planePos);

}
