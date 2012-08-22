package org.knime.knip.core.ops.filters;

import net.imglib2.algorithm.pde.PeronaMalikAnisotropicDiffusion;
import net.imglib2.algorithm.pde.PeronaMalikAnisotropicDiffusion.DiffusionFunction;
import net.imglib2.img.Img;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.img.unary.ImgCopyOperation;
import net.imglib2.type.numeric.RealType;

public class PeronaMalikAnisotropicDiffusionOp<T extends RealType<T>>
                implements UnaryOperation<Img<T>, Img<T>> {

        private final double m_deltat;
        // Iterations
        private final int m_n;
        // used Difussion Function
        private final DiffusionFunction m_fun;
        // number of threads
        private final int threads;

        /**
         *
         * Constructs a wrapping operation to execute the (elsewhere
         * implemented) Perona & Malik Anisotropic Diffusion scheme. See
         * {@link PeronaMalikAnisotropicDiffusion}.
         *
         * @param deltat
         *                the integration constant for the numerical integration
         *                scheme. Typically less that 1.
         * @param n
         *                the number of Iterations
         * @param fun
         *                the diffusion function to be used
         * @param threads
         *                The number of the threads to be used. Usually 1.
         */
        public PeronaMalikAnisotropicDiffusionOp(double deltat, int n,
                        DiffusionFunction fun, int threads) {
                this.m_deltat = deltat;
                this.m_n = n;
                this.m_fun = fun;
                this.threads = threads;
        }

        @Override
        public Img<T> compute(Img<T> input, Img<T> output) {

                // this is ugly and a hack but needed as the implementation of
                // this
                // algorithms doesn't accept the input img

                ImgCopyOperation<T> copyOp = new ImgCopyOperation<T>();

                // build a new diffusion scheme
                PeronaMalikAnisotropicDiffusion<T> diff = new PeronaMalikAnisotropicDiffusion<T>(
                                copyOp.compute(input, output), this.m_deltat,
                                this.m_fun);

                // set threads //TODO: noch ne "auto"-funktion einbauen, das das
                // autmatisch passiert? bis der fehler gefunden ist...
                diff.setNumThreads(this.threads);

                // do the process n times -> see {@link
                // PeronaMalikAnisotropicDiffusion}
                for (int i = 0; i < this.m_n; i++) {
                        diff.process();
                }

                return output;
        }

        @Override
        public UnaryOperation<Img<T>, Img<T>> copy() {
                return new PeronaMalikAnisotropicDiffusionOp<T>(this.m_deltat,
                                this.m_n, this.m_fun, this.threads);
        }

}
