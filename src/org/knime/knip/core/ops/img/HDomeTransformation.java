package org.knime.knip.core.ops.img;

import net.imglib2.img.Img;
import net.imglib2.ops.BinaryOperation;
import net.imglib2.ops.UnaryOperation;
import net.imglib2.ops.image.BinaryOperationAssignment;
import net.imglib2.ops.image.UnaryOperationAssignment;
import net.imglib2.ops.operation.binary.real.RealSubtract;
import net.imglib2.ops.operation.unary.img.CopyImgOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.types.ConnectedType;
import org.knime.knip.core.util.ImgUtils;

public class HDomeTransformation<T extends RealType<T>> implements
                UnaryOperation<Img<T>, Img<T>> {

        private final ConnectedType m_type;
        private final double m_height;
        private final double m_substractBefore;

        public HDomeTransformation(ConnectedType type, double height,
                        double substractBefore) {
                m_type = type;
                m_height = height;
                m_substractBefore = substractBefore;
        }

        @Override
        public Img<T> compute(Img<T> input, Img<T> output) {

                // calculate the regional maxima that should be
                // subtracted
                // before real run

                if (m_substractBefore > 0.0) {
                        Img<T> noSingular = ImgUtils.createEmptyCopy(input);
                        getRegionalMaxima(input, m_substractBefore, noSingular);
                        // subtract these maxima from the
                        // original image
                        input = subtract(input, noSingular);
                }

                // now calculate the desired regional maxima

                if (m_height > 0.0) {
                        output = getRegionalMaxima(input, m_height, output);
                } else {
                        new CopyImgOperation<T>().compute(input, output);
                }

                return output;
        }

        @Override
        public UnaryOperation<Img<T>, Img<T>> copy() {
                return new HDomeTransformation<T>(m_type, m_height,
                                m_substractBefore);
        }

        private Img<T> getRegionalMaxima(final Img<T> img, double height,
                        Img<T> output) {

                // compute the marker image, i.e. subtract
                // height from image
                UnaryOperation<Img<T>, Img<T>> op = new SubstractConstantOp(
                                height);

                op.compute(img, output);

                // reconstruct the image from the marker and the
                // original image
                GrayscaleReconstructionByDilation<T, T, Img<T>, Img<T>> op2 = new GrayscaleReconstructionByDilation<T, T, Img<T>, Img<T>>(
                                m_type);

                output = op2.compute(img, output);

                // subtract the reconstructed image from the
                // original image to
                // obtain height
                return subtract(img, output);
        }

        /**
         * ATTENTION: Subtrahend will be overwritten.
         */
        private Img<T> subtract(final Img<T> minuend, final Img<T> subtrahend) {

                BinaryOperation<Img<T>, Img<T>, Img<T>> subtract = new SubstractImgFromImgOp();

                subtract.compute(minuend, subtrahend, subtrahend);
                return subtrahend;
        }

        private class SubstractConstantOp implements
                        UnaryOperation<Img<T>, Img<T>> {

                private final double height;

                public SubstractConstantOp(double height) {
                        this.height = height;
                }

                @Override
                public Img<T> compute(Img<T> input, Img<T> output) {
                        new UnaryOperationAssignment<T, T>(
                                        new RealSubtractConstantBounded<T>(
                                                        height)).compute(input,
                                        output);
                        return output;
                }

                @Override
                public UnaryOperation<Img<T>, Img<T>> copy() {
                        return new SubstractConstantOp(height);
                }

        }

        private class RealSubtractConstantBounded<I extends RealType<I>>
                        implements UnaryOperation<I, I> {

                private final double constant;

                public RealSubtractConstantBounded(final double constant) {
                        this.constant = constant;
                }

                @Override
                public I compute(I input, I output) {

                        double val = Math.max(output.getMinValue(),
                                        input.getRealDouble() - this.constant);
                        output.setReal(val);

                        return output;
                }

                @Override
                public UnaryOperation<I, I> copy() {
                        return new RealSubtractConstantBounded<I>(this.constant);
                }

        }

        private class SubstractImgFromImgOp implements
                        BinaryOperation<Img<T>, Img<T>, Img<T>> {

                @Override
                public Img<T> compute(Img<T> input1, Img<T> input2,
                                Img<T> output) {
                        new BinaryOperationAssignment<T, T, T>(
                                        new RealSubtract<T, T, T>()).compute(
                                        input1, input2, output);
                        return output;
                }

                @Override
                public BinaryOperation<Img<T>, Img<T>, Img<T>> copy() {
                        return new SubstractImgFromImgOp();
                }

        }
}
