package org.knime.knip.core.ops.misc;

import net.imglib2.img.Img;
import net.imglib2.ops.operation.BinaryOutputOperation;
import net.imglib2.type.numeric.RealType;

import org.knime.knip.core.data.labeling.Signature;

public class PolarImgToSignature<T extends RealType<T>> implements
                BinaryOutputOperation<Img<T>, long[], Signature> {

        private int m_maxLineVariance;

        private boolean m_doSmooth;

        public PolarImgToSignature(boolean doSmooth, int maxLineVariance) {
                m_maxLineVariance = maxLineVariance;
                m_doSmooth = doSmooth;
        }

        @Override
        public Signature createEmptyOutput(Img<T> op0, long[] op1) {
                Signature sig = new Signature(op0, m_maxLineVariance);
                sig.setCentre(op1);
                return sig;
        }

        @Override
        public Signature compute(Img<T> op0, long[] op1, Signature r) {
                if (m_doSmooth) {
                        r.lowPassFilter(10);
                }
                return r;
        }

        @Override
        public BinaryOutputOperation<Img<T>, long[], Signature> copy() {
                return new PolarImgToSignature<T>(m_doSmooth, m_maxLineVariance);
        }

        @Override
        public Signature compute(Img<T> in1, long[] in2) {
                return compute(in1, in2, createEmptyOutput(in1, in2));
        }
}
