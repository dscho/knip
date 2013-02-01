package org.knime.knip.core.ops.integralimage;

/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2012 Stephan Preibisch, Stephan Saalfeld, Tobias
 * Pietzsch, Albert Cardona, Barry DeZonia, Curtis Rueden, Lee Kamentsky, Larry
 * Lindsey, Johannes Schindelin, Christian Dietz, Grant Harris, Jean-Yves
 * Tinevez, Steffen Jaensch, Mark Longair, Nick Perry, and Jan Funke.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.ImgFactory;
import net.imglib2.iterator.LocalizingZeroMinIntervalIterator;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.operation.UnaryOutputOperation;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.Unsigned12BitType;
import net.imglib2.type.numeric.real.DoubleType;


/*
 * original authors
 *
 * @author Stephan Preibisch
 * @author Albert Cardona
 *
 * - added sum calculation method
 * - //TODO make integral image same size as input image and use ExtendedRandomAccess with 0 border instead
 */

/**
 * n-dimensional integral image that stores sums using type {@param <T>}. Care
 * must be taken that sums do not overflow the capacity of type {@param <T>}.
 *
 * The integral image will be one pixel larger in each dimension as for easy
 * computation of sums it has to contain "zeros" at the beginning of each
 * dimension. User {@link #bufferFactory()} to create an appropriate image.
 *
 * Sums are done with the precision of {@param <T>} and then set to the integral
 * image type, which may crop the values according to the type's capabilities.
 *
 * @param <R>
 *                The type of the input image.
 * @param <T>
 *                The type of the integral image.
 */
public class IntegralImageOp<R extends RealType<R>, T extends RealType<T>>
		implements
                UnaryOutputOperation<RandomAccessibleInterval<R>, RandomAccessibleInterval<T>> {

	private final ImgFactory<T> m_factory;
	private final T m_type;

	public IntegralImageOp(ImgFactory<T> factory, T type) {
		m_factory = factory;
		m_type = type;
	}

	public IntegralImageOp(ImgFactory<T> factory) {
		m_factory = factory;
		m_type = null;
	}

	@Override
	public UnaryObjectFactory<RandomAccessibleInterval<R>, RandomAccessibleInterval<T>> bufferFactory() {
		return new UnaryObjectFactory<RandomAccessibleInterval<R>, RandomAccessibleInterval<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public RandomAccessibleInterval<T> instantiate(
					RandomAccessibleInterval<R> input) {

				T type = m_type;
				R probe = input.randomAccess().get();

				if (m_type == null) {
					if (probe instanceof LongType
							|| probe instanceof Unsigned12BitType) {
						type = (T) new LongType();
					} else if (probe instanceof IntegerType) {
						type = (T) new IntType();
					} else {
						type = (T) new DoubleType();
					}
				}

				// the size of the first dimension is changed
				final int numDimensions = input.numDimensions();
				final long integralSize[] = new long[numDimensions];

				for (int d = 0; d < numDimensions; ++d)
                                        integralSize[d] = input.dimension(d) + 1;

				return m_factory.create(integralSize, type);
			}
		};
	}

	@Override
	public UnaryOutputOperation<RandomAccessibleInterval<R>, RandomAccessibleInterval<T>> copy() {
		return new IntegralImageOp<R, T>(m_factory, m_type);
	}

	@Override
	public RandomAccessibleInterval<T> compute(
			RandomAccessibleInterval<R> input,
			RandomAccessibleInterval<T> output) {

		// the following methods alter output
		if (output.numDimensions() == 1) {
			process_1D(input, output);
		} else {
			process_nD_initialDimension(input, output);
			process_nD_remainingDimensions(input, output);
		}

		return output;
	}

	public void process_1D(RandomAccessibleInterval<R> input,
			RandomAccessibleInterval<T> output) {
		final T tmpVar = output.randomAccess().get().createVariable();
		final T sum = output.randomAccess().get().createVariable();

		// the size of dimension 0
		final long size = output.dimension(0);

		final RandomAccess<R> cursorIn = input.randomAccess();
		final RandomAccess<T> cursorOut = output.randomAccess();

		cursorIn.setPosition(0, 0);
		cursorOut.setPosition(1, 0);

		// compute the first pixel
		sum.setReal(cursorIn.get().getRealDouble());
		cursorOut.get().set(sum);

		for (long i = 2; i < size; ++i) {
			cursorIn.fwd(0);
			cursorOut.fwd(0);

			tmpVar.setReal(cursorIn.get().getRealDouble());
			sum.add(tmpVar);
			cursorOut.get().set(sum);
		}
	}

	private void process_nD_initialDimension(RandomAccessibleInterval<R> input,
			RandomAccessibleInterval<T> output) {

		int numDimensions = output.numDimensions();
		final long[] fakeSize = new long[numDimensions - 1];

		// location for the input location
		final long[] tmpIn = new long[numDimensions];

		// location for the integral location
		final long[] tmpOut = new long[numDimensions];

		// the size of dimension 0
		final long size = output.dimension(0);

		for (int d = 1; d < numDimensions; ++d)
			fakeSize[d - 1] = output.dimension(d);

		final LocalizingZeroMinIntervalIterator cursorDim = new LocalizingZeroMinIntervalIterator(
				fakeSize);

		final RandomAccess<R> cursorIn = input.randomAccess();
		final RandomAccess<T> cursorOut = output.randomAccess();

		final T tmpVar = output.randomAccess().get().createVariable();
		final T sum = output.randomAccess().get().createVariable();

		// iterate over all dimensions except the one we are computing the
		// integral in, which is dim=0 here
		main: while (cursorDim.hasNext()) {
			cursorDim.fwd();

			// get all dimensions except the one we are currently doing the
			// integral on
			cursorDim.localize(fakeSize);

			tmpIn[0] = 0;
			tmpOut[0] = 1;

			for (int d = 1; d < numDimensions; ++d) {
				tmpIn[d] = fakeSize[d - 1] - 1;
				tmpOut[d] = fakeSize[d - 1];

				// all entries of position 0 are 0
				if (tmpOut[d] == 0)
					continue main;
			}

			// set the cursor to the beginning of the correct line
			cursorIn.setPosition(tmpIn);

			// set the cursor in the integral image to the right position
			cursorOut.setPosition(tmpOut);

			// integrate over the line
			integrateLineDim0(cursorIn, cursorOut, sum, tmpVar, size);
		}
	}

	private void process_nD_remainingDimensions(
			RandomAccessibleInterval<R> input,
			RandomAccessibleInterval<T> output) {

		int numDimensions = output.numDimensions();

		for (int d = 1; d < numDimensions; ++d) {
			final long[] fakeSize = new long[numDimensions - 1];
			final long[] tmp = new long[numDimensions];

			// the size of dimension d
			final long size = output.dimension(d);

			// get all dimensions except the one we are currently doing the
			// integral on
			int countDim = 0;
			for (int e = 0; e < numDimensions; ++e)
				if (e != d)
					fakeSize[countDim++] = output.dimension(e);

			final LocalizingZeroMinIntervalIterator cursorDim = new LocalizingZeroMinIntervalIterator(
					fakeSize);

			final RandomAccess<T> cursor = output.randomAccess();
			final T sum = output.randomAccess().get().createVariable();

			while (cursorDim.hasNext()) {
				cursorDim.fwd();

				// get all dimensions except the one we are currently doing the
				// integral on
				cursorDim.localize(fakeSize);

				tmp[d] = 1;
				countDim = 0;
				for (int e = 0; e < numDimensions; ++e)
					if (e != d)
						tmp[e] = fakeSize[countDim++];

				// update the cursor in the input image to the current dimension
				// position
				cursor.setPosition(tmp);

				// sum up line
				integrateLine(d, cursor, sum, size);
			}
		}
	}


	protected void integrateLineDim0(final RandomAccess<R> cursorIn,
			final RandomAccess<T> cursorOut, final T sum, final T tmpVar,
			final long size) {
		// compute the first pixel
		sum.setReal(cursorIn.get().getRealDouble());
		cursorOut.get().set(sum);

		for (long i = 2; i < size; ++i) {
			cursorIn.fwd(0);
			cursorOut.fwd(0);

			tmpVar.setReal(cursorIn.get().getRealDouble());
			sum.add(tmpVar);
			cursorOut.get().set(sum);
		}
	}

	protected void integrateLine(final int d, final RandomAccess<T> cursor,
			final T sum, final long size) {
		// init sum on first pixel that is not zero
		sum.set(cursor.get());

		for (long i = 2; i < size; ++i) {
			cursor.fwd(d);

			sum.add(cursor.get());
			cursor.get().set(sum);
		}
	}

        public static <T extends RealType<T>> T getSum(Localizable p1,
                        Localizable p2,
                        RandomAccessibleInterval<T> integralImage)
                        throws IncompatibleTypeException {

                if (p1.numDimensions() != p2.numDimensions()
                                || p1.numDimensions() != integralImage
                                                .numDimensions()) {
                        throw new IncompatibleTypeException(integralImage,
                                        "the two positions and the integral image do not have the same dimensionality");
                }

                // implemented according to
                // http://en.wikipedia.org/wiki/Summed_area_table high
                // dimensional variant

                RandomAccess<T> ra = integralImage.randomAccess();
                int d = p1.numDimensions();
                boolean[] p;
                long[] position = new long[d];
                double sum = 0;

                for (int i = 0; i < Math.pow(2, d); i++) {
                        p = getBinaryRep(i, d);
                        int ones = 0;

                        for (int j = 0; j < p.length; j++) {
                                if (p[j]) { // = 1
                                        ones++;
                                        // +1 because the integral image
                                        // contains a zero column
                                        position[j] = p2.getLongPosition(j) + 1l;
                                } else { // = 0
                                        // no +1 because integrating from 3..5
                                        // inc. 3 & 5 means [5] - [2]
                                        position[j] = p1.getLongPosition(j);
                                }
                        }

                        ra.setPosition(position);
                        int sign = (int) Math.pow(-1, d - ones);

                        sum += sign * ra.get().getRealDouble();
                }

                T result = integralImage.randomAccess().get().createVariable();
                result.setReal(sum);

                return result;
        }

        // gives as {0,1}^d all binary combinations 0,0,..,0 ...
        // 1,1,...,1
        private static boolean[] getBinaryRep(int i, int d) {
                char[] tmp = Long.toBinaryString(i).toCharArray();
                boolean[] p = new boolean[d];
                for (int pos = 0; pos < tmp.length; pos++) {
                        if (tmp[pos] == '1') {
                                p[tmp.length - (pos + 1)] = true;
                        }
                }

                return p;
        }


}
