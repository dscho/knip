package org.knime.knip.core.ops.bittype;

import java.util.Collection;

import net.imglib2.RandomAccess;
import net.imglib2.meta.ImgPlus;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.type.logic.BitType;

public class PositionsToBitTypeImage implements UnaryOperation<Collection<long[]>, ImgPlus<BitType>> {

    @Override
    public ImgPlus<BitType> compute(final Collection<long[]> op1, final ImgPlus<BitType> res) {
        final RandomAccess<BitType> resAccess = res.randomAccess();
        for (final long[] lm : op1) {
            resAccess.setPosition(lm);
            resAccess.get().set(true);
        }
        return res;
    }

    @Override
    public UnaryOperation<Collection<long[]>, ImgPlus<BitType>> copy() {
        return new PositionsToBitTypeImage();
    }
}
