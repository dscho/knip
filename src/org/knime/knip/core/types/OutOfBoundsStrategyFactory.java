package org.knime.knip.core.types;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.outofbounds.OutOfBoundsBorderFactory;
import net.imglib2.outofbounds.OutOfBoundsConstantValueFactory;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorExpWindowingFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsPeriodicFactory;
import net.imglib2.type.numeric.RealType;

public class OutOfBoundsStrategyFactory {
        /**
         *
         * @param <T>
         *                Type of the factory. Will be copied and value (min,
         *                max, zero) will be set if needed
         * @param <O>
         *                Type of the factory. Type to determine min or max
         * @param <IN>
         *                Type of the RandomAccessibleInterval to be wrapped
         * @param strategyEnum
         * @param inValue
         * @param refType
         * @return
         */
        public static <T extends RealType<T>, O extends RealType<O>, IN extends RandomAccessibleInterval<T>> OutOfBoundsFactory<T, IN> getStrategy(
                        OutOfBoundsStrategyEnum strategyEnum, T val, O refType) {
                T inValue = val.createVariable();

                switch (strategyEnum) {
                case MIN_VALUE:
                        inValue.setReal(refType.getMinValue());
                        return new OutOfBoundsConstantValueFactory<T, IN>(
                                        inValue);
                case MAX_VALUE:
                        inValue.setReal(refType.getMaxValue());
                        return new OutOfBoundsConstantValueFactory<T, IN>(
                                        inValue);
                case ZERO_VALUE:
                        inValue.setReal(0.0);
                        return new OutOfBoundsConstantValueFactory<T, IN>(
                                        inValue);
                case MIRROR_SINGLE:
                        return new OutOfBoundsMirrorFactory<T, IN>(
                                        OutOfBoundsMirrorFactory.Boundary.SINGLE);
                case MIRROR_DOUBLE:
                        return new OutOfBoundsMirrorFactory<T, IN>(
                                        OutOfBoundsMirrorFactory.Boundary.DOUBLE);
                case PERIODIC:
                        return new OutOfBoundsPeriodicFactory<T, IN>();
                case BORDER:
                        return new OutOfBoundsBorderFactory<T, IN>();
                case FADE_OUT:
                        return new OutOfBoundsMirrorExpWindowingFactory<T, IN>();
                default:
                        throw new IllegalArgumentException(
                                        "Unknown OutOfBounds factory type");
                }
        }
}
