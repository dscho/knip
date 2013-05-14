package org.knime.knip.core.types;

import net.imglib2.algorithm.region.localneighborhood.HyperSphereShape;
import net.imglib2.algorithm.region.localneighborhood.RectangleShape;
import net.imglib2.algorithm.region.localneighborhood.Shape;

public enum NeighborhoodType {
    RECTANGULAR, SPHERICAL;

    /**
     * This is a not so beautiful way to initialize neighborhoods: actually
     * here it's not possible to set different spans in different dimensions
     */
    public static Shape getNeighborhood(final NeighborhoodType type,
                                        final int radius) {

        switch (type) {
            case RECTANGULAR:
                return new RectangleShape(radius, false);
            case SPHERICAL:
                return new HyperSphereShape(radius);
            default:
                throw new IllegalArgumentException(
                                                   "Neighborhood type can't be found");
        }

    }
}
