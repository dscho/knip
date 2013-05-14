package org.knime.knip.core.ops.filters;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.pde.PeronaMalikAnisotropicDiffusion;
import net.imglib2.algorithm.pde.PeronaMalikAnisotropicDiffusion.DiffusionFunction;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.ops.operation.UnaryOperation;
import net.imglib2.ops.operation.iterableinterval.unary.IterableIntervalCopy;
import net.imglib2.ops.operation.subset.views.ImgView;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class PeronaMalikAnisotropicDiffusionOp<T extends RealType<T> & NativeType<T>, I extends RandomAccessibleInterval<T>>
                implements UnaryOperation<I, I> {

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
        public PeronaMalikAnisotropicDiffusionOp(final double deltat, final int n,
                        final DiffusionFunction fun, final int threads) {
                this.m_deltat = deltat;
                this.m_n = n;
                this.m_fun = fun;
                this.threads = threads;
        }

        @Override
        public I compute(final I input, final I output) {

                // this is ugly and a hack but needed as the implementation of
                // this
                // algorithms doesn't accept the input img
                final ImgView<T> out = new ImgView<T>(output,
                                new ArrayImgFactory<T>());

                new IterableIntervalCopy<T>().compute(Views.iterable(input),
                                out);

                // build a new diffusion scheme
                final PeronaMalikAnisotropicDiffusion<T> diff = new PeronaMalikAnisotropicDiffusion<T>(
                                out, this.m_deltat, this.m_fun);

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
        public UnaryOperation<I, I> copy() {
                return new PeronaMalikAnisotropicDiffusionOp<T, I>(
                                this.m_deltat, this.m_n, this.m_fun,
                                this.threads);
        }

}
