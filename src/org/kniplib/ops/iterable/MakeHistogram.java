package org.kniplib.ops.iterable;

import java.util.Iterator;

import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.numeric.RealType;

import org.kniplib.data.statistics.KNIPLibHistogram;

public final class MakeHistogram<T extends RealType<T>> implements
                UnaryOutputOperation<Iterable<T>, KNIPLibHistogram> {

        int m_numBins = 0;

        public MakeHistogram() {
                this(-1);
        }

        public MakeHistogram(int numBins) {
                m_numBins = numBins;
        }

        @Override
        public final KNIPLibHistogram createEmptyOutput(Iterable<T> op) {
                return m_numBins <= 0 ? new KNIPLibHistogram(op.iterator()
                                .next().createVariable())
                                : new KNIPLibHistogram(m_numBins, op.iterator()
                                                .next().createVariable());
        }

        @Override
        public final KNIPLibHistogram compute(Iterable<T> op, KNIPLibHistogram r) {
                final Iterator<T> it = op.iterator();
                r.clear();
                while (it.hasNext()) {
                        r.incByValue(it.next().getRealDouble());
                }

                return r;
        }

        @Override
        public KNIPLibHistogram compute(Iterable<T> op) {
                return compute(op, createEmptyOutput(op));
        }

        @Override
        public UnaryOutputOperation<Iterable<T>, KNIPLibHistogram> copy() {
                return new MakeHistogram<T>(m_numBins);
        }
}
