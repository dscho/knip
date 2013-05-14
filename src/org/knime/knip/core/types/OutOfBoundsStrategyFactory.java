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
                                                                                                                        final String strategy, final T val) {
        return getStrategy(strategy, val, val);
    }

    public static <T extends RealType<T>, O extends RealType<O>, IN extends RandomAccessibleInterval<T>> OutOfBoundsFactory<T, IN> getStrategy(
                                                                                                                                               final String strategy, final T val, final O refType) {
        return getStrategy(Enum.valueOf(OutOfBoundsStrategyEnum.class,
                                        strategy), val, val);
    }

    public static <T extends RealType<T>, IN extends RandomAccessibleInterval<T>> OutOfBoundsFactory<T, IN> getStrategy(
                                                                                                                        final OutOfBoundsStrategyEnum strategyEnum, final T val) {
        return getStrategy(strategyEnum, val, val);
    }

    public static <T extends RealType<T>, O extends RealType<O>, IN extends RandomAccessibleInterval<T>> OutOfBoundsFactory<T, IN> getStrategy(
                                                                                                                                               final OutOfBoundsStrategyEnum strategyEnum, final T val, final O refType) {
        final T inValue = val.createVariable();

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
