package org.kniplib.ops.img;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labeling.LabelingType;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.type.NativeType;

import org.kniplib.types.ConnectedType;

/**
 * nD Connected Component Analysis.
 *
 * @author hornm, dietzc University of Konstanz
 */
public class CCA<T extends NativeType<T> & Comparable<T>, I extends RandomAccessibleInterval<T> & IterableInterval<T>, LL extends RandomAccessibleInterval<LabelingType<Integer>> & IterableInterval<LabelingType<Integer>>>
                extends AbstractRegionGrowing<T, Integer, I, LL> {

        private Cursor<T> srcCur;

        private RandomAccess<T> srcRA;

        private Integer m_labelNumber;

        private final T m_background;

        /**
         * @param ctype
         * @param background
         * @param mode
         */
        public CCA(ConnectedType ctype, T background) {
                super(ctype, GrowingMode.ASYNCHRONOUS, false);
                m_background = background;
                m_labelNumber = 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void initRegionGrowing(I srcImg) {
                srcCur = srcImg.localizingCursor();
                srcRA = srcImg.randomAccess();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer nextSeedPosition(int[] seedPos) {
                while (srcCur.hasNext()) {
                        srcCur.fwd();
                        if (srcCur.get().compareTo(m_background) != 0) {
                                srcCur.localize(seedPos);
                                return m_labelNumber;
                        }
                }
                return null;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected boolean includeInRegion(int[] oldPos, int[] nextPos,
                        Integer label) {
                srcRA.setPosition(nextPos);
                return srcRA.get().compareTo(m_background) != 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void queueProcessed() {
                m_labelNumber++;

        }

        /**
         * Sets the current label number counter to 1.
         */
        public void resetLabelNumber() {
                m_labelNumber = 1;
        }

        @Override
        public UnaryOperation<I, LL> copy() {
                return new CCA<T, I, LL>(m_ctype, m_background.copy());
        }

}
