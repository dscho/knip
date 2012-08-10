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
        public static <T extends RealType<T>, IN extends RandomAccessibleInterval<T>> OutOfBoundsFactory<T, IN> getStrategy(
                        OutOfBoundsStrategyEnum strategyEnum, T type) {
                switch (strategyEnum) {
                case MIN_VALUE:
                        T value = type.createVariable();
                        value.setReal(value.getMinValue());
                        return new OutOfBoundsConstantValueFactory<T, IN>(value);
                case MAX_VALUE:
                        value = type.createVariable();
                        value.setReal(value.getMaxValue());
                        return new OutOfBoundsConstantValueFactory<T, IN>(value);
                case ZERO_VALUE:
                        value = type.createVariable();
                        value.setReal(0.0);
                        return new OutOfBoundsConstantValueFactory<T, IN>(value);
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