package org.knime.knip.core.ops.metadata;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.Type;

public class DimSwapper<T extends Type<T>> implements
                UnaryOutputOperation<Img<T>, Img<T>> {

        private final int[] m_backMapping;

        private final long[] m_srcOffset;

        private final long[] m_srcSize;

        /**
         * <pre>
         * mapping[0] = 1; // X &lt;- Y, Y becomes X
         * mapping[1] = 2; // Y &lt;- C, C becomes Y
         * mapping[2] = 0; // C &lt;- X, X becomes C
         * </pre>
         *
         * @param backMapping
         */
        public DimSwapper(final int[] backMapping) {
                this(backMapping, new long[backMapping.length],
                                new long[backMapping.length]);
        }

        /**
         *
         * @param backMapping
         * @param srcOffset
         *                Offset in source coordinates.
         * @param srcSize
         *                Size in source coordinates.
         */
        public DimSwapper(final int[] backMapping, final long[] srcOffset,
                        final long[] srcSize) {
                m_backMapping = backMapping;
                m_srcOffset = srcOffset;
                m_srcSize = srcSize;
        }

        @Override
        public Img<T> compute(Img<T> op, Img<T> r) {
                if (r.numDimensions() != op.numDimensions()) {
                        throw new IllegalArgumentException(
                                        "Intervals not compatible");
                }
                final int nDims = r.numDimensions();
                for (int i = 0; i < nDims; i++) {
                        if (m_backMapping[i] >= nDims)
                                throw new IllegalArgumentException(
                                                "Channel mapping is out of bounds");
                }
                final RandomAccess<T> opc = op.randomAccess();
                final Cursor<T> rc = r.localizingCursor();
                while (rc.hasNext()) {
                        rc.fwd();
                        for (int i = 0; i < nDims; i++) {
                                opc.setPosition(rc.getLongPosition(i)
                                                + m_srcOffset[i],
                                                m_backMapping[i]);
                        }
                        rc.get().set(opc.get());
                }

                return r;
        }

        @Override
        public UnaryOutputOperation<Img<T>, Img<T>> copy() {
                return new DimSwapper<T>(m_backMapping.clone(), m_srcOffset,
                                m_srcSize);
        }

        @Override
        public UnaryObjectFactory<Img<T>, Img<T>> bufferFactory() {
                return new UnaryObjectFactory<Img<T>, Img<T>>() {

                        @Override
                        public Img<T> instantiate(Img<T> op) {
                                final long[] size = m_srcSize.clone();
                                for (int i = 0; i < size.length; i++) {
                                        if (size[i] <= 0)
                                                size[i] = op.dimension(m_backMapping[i]);
                                }
                                return op.factory()
                                                .create(size,
                                                                op.firstElement()
                                                                                .createVariable());
                        }
                };
        }
}
