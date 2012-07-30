package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.ScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.ARGBType;

public abstract class ProjectingRenderer<T extends Type<T>> implements
                ImageRenderer<T, RandomAccessibleInterval<T>> {

        @Override
        public ScreenImage render(RandomAccessibleInterval<T> source,
                        int dimX, int dimY, long[] planePos) {

                return project2D(source, dimX, dimY, planePos);
        }

        private ScreenImage project2D(
                        final RandomAccessibleInterval<T> source, int dimX,
                        int dimY, long[] planePos) {

                int width = (int) source.dimension(dimX);
                int height = (int) source.dimension(dimY);

                ARGBScreenImage target = new ARGBScreenImage(width, height);
                Abstract2DProjector<T, ARGBType> projector = getProjector(dimX,
                                dimY, source, target);

                projector.setPosition(planePos);
                projector.map();

                return target;
        }


        protected abstract Abstract2DProjector<T, ARGBType> getProjector(
                        int dimX, int dimY, RandomAccessibleInterval<T> source,
                        ARGBScreenImage target);
}
