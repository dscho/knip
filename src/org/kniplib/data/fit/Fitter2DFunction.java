package org.kniplib.data.fit;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;

public abstract class Fitter2DFunction<T extends RealType<T>> implements
                ParametricBivariateRealFunction {

        /**
         * Initialize the function, return initial parameter guesses
         * 
         * @param Img
         *                <T> img The input image
         * @return double[] Array holding initial parameter guesses
         */
        public abstract double[] init(IterableInterval<T> img);

        /**
         * Get the number of free parameters
         * 
         * @return int Number of parameters
         */
        abstract int noParameters();
}
