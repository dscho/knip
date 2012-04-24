package org.kniplib.ops.img;

import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.image.BinaryOperationAssignment;
import net.imglib2.ops.operation.binary.real.RealXor;
import net.imglib2.type.logic.BitType;

import org.kniplib.types.ConnectedType;

/**
 * Extracts the outline of a given connected component in an {@link Img} of
 * {@link BitType}. The outline is here defined as all pixels, which are next to
 * the pixels which are on the border of the connected component. Please be
 * aware that for a correct calculation of the Perimeter only one connected
 * component should be contained in the {@link Img} of {@link BitType}
 * 
 * @author Christian Dietz
 * 
 */
public class ExtractOutlineImg<K extends RandomAccessibleInterval<BitType> & IterableInterval<BitType>>
                implements UnaryOperation<K, K> {

        private final BinaryOperationAssignment<BitType, BitType, BitType> m_imgManWith;

        private final UnaryOperation<K, K> m_op;

        private final boolean m_outlineInsideSegment;

        public ExtractOutlineImg(final boolean outlineInsideSegment) {
                m_outlineInsideSegment = outlineInsideSegment;
                m_imgManWith = new BinaryOperationAssignment<BitType, BitType, BitType>(
                                new RealXor<BitType, BitType, BitType>());
                m_op = m_outlineInsideSegment ? new Erode<K>(
                                ConnectedType.EIGHT_CONNECTED, 1)
                                : new Dilate<K>(ConnectedType.FOUR_CONNECTED, 1);
        }

        @Override
        public K compute(final K op, final K r) {
                if (op.numDimensions() != 2) {
                        throw new IllegalArgumentException(
                                        "Operation only permitted on two dimensions");
                }

                // This produces black results
                // if (!m_outlineInsideSegment) {
                // new ExpandAndCenterImg<BitType, K>(1).compute(op, op);
                // }

                m_op.compute(op, r);
                m_imgManWith.compute(op, r, r);
                return r;
        }

        @Override
        public UnaryOperation<K, K> copy() {
                return new ExtractOutlineImg<K>(m_outlineInsideSegment);
        }
}
