package org.knime.knip.core.awt;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ARGBScreenImage;
import net.imglib2.display.projectors.Abstract2DProjector;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.awt.lookup.LookupTable;
import org.knime.knip.core.awt.parametersupport.RendererWithLookupTable;
import org.knime.knip.core.awt.specializedrendering.Projector2D;
import org.knime.knip.core.awt.specializedrendering.RealGreyARGBByLookupTableConverter;

/**
 * Renders an image by using a lookup table.<br>
 *
 * @author muethingc
 */
public class Real2ColorByLookupTableRenderer<T extends RealType<T>> extends
                ProjectingRenderer<T> implements
                RendererWithLookupTable<T, ARGBType> {

        /**
         * A simple class that can be injected in the converter so that we will
         * always get some result.
         */
        private class SimpleTable implements LookupTable<T, ARGBType> {

                @Override
                public final ARGBType lookup(final T value) {
                        return new ARGBType(1);
                }
        }

        private final RealGreyARGBByLookupTableConverter<T> m_converter;

        /**
         * Set up a new instance.<br>
         *
         * By default this instance uses a simple lookup table that will always
         * return 1 for all values.
         * 
         * @param service
         *                the EventService that should be used
         */
        public Real2ColorByLookupTableRenderer() {
                // set up a primitive converter so that we don't have to do null
                // checks
                m_converter = new RealGreyARGBByLookupTableConverter<T>(
                                new SimpleTable());
        }

        @Override
        public void setLookupTable(final LookupTable<T, ARGBType> table) {
                if (table == null)
                        throw new NullPointerException();

                m_converter.setLookupTable(table);
        }

        @Override
        public final String toString() {
                return ("Transfer Function Renderer");
        }

        @Override
        protected Abstract2DProjector<T, ARGBType> getProjector(int dimX,
                        int dimY, RandomAccessibleInterval<T> source,
                        ARGBScreenImage target) {
                return new Projector2D<T, ARGBType>(dimX, dimY, source, target,
                                m_converter);
        }
}
