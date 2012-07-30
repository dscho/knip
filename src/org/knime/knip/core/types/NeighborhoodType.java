package org.knime.knip.core.types;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.region.localneighborhood.AbstractNeighborhood;
import net.imglib2.algorithm.region.localneighborhood.BufferedRectangularNeighborhood;
import net.imglib2.algorithm.region.localneighborhood.HyperSphereNeighborhood;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.Type;

public enum NeighborhoodType {
        RECTANGULAR, SPHERICAL;

        /**
         * This is a not so beautiful way to initialize neighborhoods: actually
         * here it's not possible to set different spans in different dimensions
         */
        public static <T extends Type<T>, IN extends RandomAccessibleInterval<T>> AbstractNeighborhood<T, IN> getNeighborhood(
                        final NeighborhoodType type, int numDims,
                        OutOfBoundsFactory<T, IN> outofbounds, final int radius) {

                AbstractNeighborhood<T, IN> neighborhood = null;
                switch (type) {
                case RECTANGULAR:
                        long[] roiSpan = new long[numDims];

                        Arrays.fill(roiSpan, radius);
                        neighborhood = new BufferedRectangularNeighborhood<T, IN>(
                                        numDims, outofbounds, roiSpan);
                        break;
                case SPHERICAL:
                        neighborhood = new HyperSphereNeighborhood<T, IN>(
                                        numDims, outofbounds, radius);
                        break;
                default:
                        throw new IllegalArgumentException(
                                        "Neighborhood type can't be found");
                }

                return neighborhood;
        }
}
