package org.kniplib.types;

/**
 * Neighborhood types.
 * 
 * @author hornm
 */
public enum ConnectedType {
        /**
         * Touching voxels without diagonal neighbors.
         */
        FOUR_CONNECTED,

        /**
         * All touching voxels.
         */
        EIGHT_CONNECTED;

}
