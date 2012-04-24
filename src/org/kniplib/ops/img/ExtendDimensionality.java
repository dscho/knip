package org.kniplib.ops.img;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.UnaryOutputOperation;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

/**
 * @author schoenen
 * @param <T>
 */
public class ExtendDimensionality<T extends Type<T> & NativeType<T>> implements
                UnaryOutputOperation<Img<T>, Img<T>> {

        private final int m_numDimensions;

        public ExtendDimensionality(int numDimensions) {
                m_numDimensions = numDimensions;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Img<T> createEmptyOutput(Img<T> op) {
                if (m_numDimensions < op.numDimensions())
                        throw new Error("Can only increase dimensionality.");
                long dim[] = new long[m_numDimensions];
                Arrays.fill(dim, 1);
                for (int i = 0; i < op.numDimensions(); i++) {
                        dim[i] = op.dimension(i);
                }
                return new ArrayImgFactory<T>().create(dim, op.firstElement()
                                .createVariable());
        }

        /**
         * {@inheritDoc}
         * 
         * @return
         */
        @Override
        public Img<T> compute(Img<T> op, Img<T> r) {
                if (m_numDimensions < op.numDimensions())
                        throw new IllegalArgumentException(
                                        "Can only increase dimensionality.");
                if (r.size() == op.size()) {
                        // TODO iteration order
                        Cursor<T> cs = op.cursor();
                        Cursor<T> cr = r.cursor();
                        while (cs.hasNext()) {
                                cs.next();
                                cr.next();
                                cr.get().set(cs.get());
                        }
                }

                return r;
        }

        @Override
        public UnaryOutputOperation<Img<T>, Img<T>> copy() {
                return new ExtendDimensionality<T>(m_numDimensions);
        }

        @Override
        public Img<T> compute(Img<T> in) {
                return compute(in, createEmptyOutput(in));
        }
}
