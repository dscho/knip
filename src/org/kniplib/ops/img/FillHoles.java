package org.kniplib.ops.img;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.logic.BitType;

import org.kniplib.exceptions.ImageNotCompatibleException;
import org.kniplib.ops.interval.FloodFill;
import org.kniplib.types.ConnectedType;

public final class FillHoles<K extends RandomAccessible<BitType> & IterableInterval<BitType>>
                implements UnaryOperation<K, K> {

        public FillHoles() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public final K compute(final K op, final K r) {
                if (!r.equalIterationOrder(op)) {
                        throw new ImageNotCompatibleException(
                                        "Intervals are not compatible!");
                }
                FloodFill<BitType, K> ff = new FloodFill<BitType, K>(
                                ConnectedType.FOUR_CONNECTED);
                long[] dim = new long[r.numDimensions()];
                r.dimensions(dim);
                Cursor<BitType> rc = r.cursor();
                Cursor<BitType> opc = op.localizingCursor();
                // Fill with non background marker
                while (rc.hasNext()) {
                        rc.next().setOne();
                }
                rc.reset();
                boolean border;
                // Flood fill from every background border voxel
                while (rc.hasNext()) {
                        rc.next();
                        opc.next();
                        if (rc.get().get() && !opc.get().get()) {
                                border = false;
                                for (int i = 0; i < r.numDimensions(); i++) {
                                        if (rc.getLongPosition(i) == 0
                                                        || rc.getLongPosition(i) == dim[i] - 1) {
                                                border = true;
                                                break;
                                        }
                                }
                                if (border)
                                        ff.compute(op, rc, r);
                        }
                }
                return r;
        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new FillHoles<K>();
        }

}
