package org.kniplib.ops.img;

import java.util.Collection;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.logic.BitType;

public class PositionsToBitTypeImage implements
                UnaryOperation<Collection<long[]>, Img<BitType>> {

        @Override
        public Img<BitType> compute(Collection<long[]> op1, Img<BitType> res) {
                final RandomAccess<BitType> resAccess = res.randomAccess();
                for (final long[] lm : op1) {
                        resAccess.setPosition(lm);
                        resAccess.get().set(true);
                }
                return res;
        }

        @Override
        public UnaryOperation<Collection<long[]>, Img<BitType>> copy() {
                return new PositionsToBitTypeImage();
        }
}
