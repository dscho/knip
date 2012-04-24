package org.kniplib.ops.img;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.subimg.SubImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IterableRandomAccessibleInterval;

public class ExpandAndCenterImg<T extends RealType<T>, K extends IterableInterval<T> & RandomAccessibleInterval<T>>
                implements UnaryOperation<K, K> {

        private int m_borderSize;

        private long[] m_subMin;

        public ExpandAndCenterImg(int borderSize) {
                m_borderSize = borderSize;
                m_subMin = new long[] { m_borderSize, m_borderSize };
        }

        @Override
        public K compute(K op, K r) {

                IterableInterval<T> resSub = new IterableRandomAccessibleInterval<T>(
                                SubImg.getView(op,
                                                new FinalInterval(
                                                                m_subMin,
                                                                new long[] {
                                                                                r.max(0)
                                                                                                - m_borderSize,
                                                                                r.max(1)
                                                                                                - m_borderSize }),
                                                false));

                Cursor<T> resCursor = resSub.cursor();
                Cursor<T> srcCursor = op.cursor();

                while (srcCursor.hasNext()) {
                        resCursor.fwd();
                        srcCursor.fwd();

                        resCursor.get().set(srcCursor.get());
                }
                return r;
        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new ExpandAndCenterImg<T, K>(m_borderSize);

        }
}
