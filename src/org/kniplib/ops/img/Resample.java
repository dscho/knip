package org.kniplib.ops.img;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealRandomAccess;
import net.imglib2.img.Img;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Resample<T extends RealType<T>> implements
                UnaryOperation<Img<T>, Img<T>> {

        public enum Mode {
                LINEAR, NEAREST_NEIGHBOR, PERIODICAL;
        }

        private Mode m_mode;

        public Resample(Mode mode) {
                m_mode = mode;
        }

        @Override
        public Img<T> compute(Img<T> op, Img<T> res) {

                InterpolatorFactory<T, RandomAccessible<T>> ifac;
                switch (m_mode) {
                case LINEAR:
                        ifac = new NLinearInterpolatorFactory<T>();
                        break;
                case NEAREST_NEIGHBOR:
                        ifac = new NearestNeighborInterpolatorFactory<T>();
                        break;
                default:

                        RandomAccess<T> srcRA = Views.extendPeriodic(op)
                                        .randomAccess();
                        Cursor<T> resCur = res.localizingCursor();
                        while (resCur.hasNext()) {
                                resCur.fwd();
                                srcRA.setPosition(resCur);
                                resCur.get().set(srcRA.get());
                        }

                        return res;
                }

                final RealRandomAccess<T> inter = ifac
                                .create(Views.extend(
                                                op,
                                                new OutOfBoundsMirrorFactory<T, Img<T>>(
                                                                OutOfBoundsMirrorFactory.Boundary.SINGLE)));

                final Cursor<T> c2 = res.localizingCursor();
                final float[] s = new float[res.numDimensions()];
                for (int i = 0; i < s.length; i++)
                        s[i] = (float) op.dimension(i) / res.dimension(i);
                final long[] d = new long[res.numDimensions()];
                while (c2.hasNext()) {
                        c2.fwd();
                        c2.localize(d);
                        for (int i = 0; i < d.length; i++) {
                                inter.setPosition(s[i] * d[i], i);
                        }

                        c2.get().set(inter.get());

                }

                return res;
        }

        @Override
        public UnaryOperation<Img<T>, Img<T>> copy() {
                return new Resample<T>(m_mode);
        }
}
