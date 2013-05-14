package org.knime.knip.core.ops.iterator;

import java.util.Iterator;

import net.imglib2.ops.operation.BinaryOperation;
import net.imglib2.type.numeric.RealType;

/**
 * 
 * @author wilderm, University of Konstanz
 * 
 * @param <T>
 * @param <Integer>
 * @param <TYPE>
 */
public class SigmaFilter<T extends RealType<T>, V extends RealType<V>, TYPE extends Iterator<T>>
		implements BinaryOperation<TYPE, T, V> {

	private final double sigma;
	private final double sigmaFactor;
	private final double m_sigmaMultiplied;
	private final double pixelFraction;
	private final boolean outlierDetection;

	public SigmaFilter(final double sigma, final double sigmaFactor, final double pixelFraction,
			final boolean outlierDetection) {
		this.sigma = sigma;
		this.sigmaFactor = sigmaFactor;
		m_sigmaMultiplied = sigmaFactor * sigma;
		this.pixelFraction = pixelFraction;
		this.outlierDetection = outlierDetection;
	}

	// Input nicht veraendern
	// Output alles reinschreiben
	@Override
	public V compute(final TYPE input, final T val, final V output) {

		final double center = val.getRealDouble();
		double sumAll = 0;
		int ctrAll = 0;
		double sumInRange = 0;
		int ctrInRange = 0;

		while (input.hasNext()) {
			final double pixel = input.next().getRealDouble();
			sumAll += pixel;
			if ((center - m_sigmaMultiplied) < pixel
					&& pixel < (center + m_sigmaMultiplied)) {
				sumInRange += pixel;
				ctrInRange++;
			}
			ctrAll++;
		}

		final int minPixels = (int) Math.floor(ctrAll * pixelFraction);

		if (ctrInRange >= minPixels) {
			output.setReal(sumInRange / ctrInRange);
		} else {
			if(outlierDetection) {
				output.setReal((sumAll - center) / (ctrAll - 1));
			} else {
				output.setReal(sumAll / ctrAll);
			}
		}

		return output;
		// // noch abzufangen
		// // if(input.numDimensions() != 2 ... )
		//
		// // dimensions of the input
		// long xDim = input.dimension(0);
		// long yDim = input.dimension(1);
		//
		// System.out.println("Bildabmessungen(horizontal, vertikal): (" + xDim
		// + " ," + yDim + ")");
		//
		// // defined by user
		// int window = 3;
		//
		// // width of the square window
		// int w = 2 * window + 1;
		//
		// // factor by which sigma is multiplied
		// double factor = 1.0;
		//
		// // defines if outliers should be detected
		// boolean outlierDetection = false;
		// int outlierPixels = 3;
		// if (outlierDetection == true
		// && (window + 1) * (window + 1) < outlierPixels + 2) {
		// outlierDetection = false;
		// }
		//
		// RandomAccess<T> rndAccessIn = input.randomAccess();
		//
		// // the current type
		// T typeIn = rndAccessIn.get();
		//
		// // container for current position
		// int[] pos = new int[input.numDimensions()];
		//
		// // sigma
		// double sigma = calcSigma(input);
		//
		// Cursor<T> cOut = output.localizingCursor();
		// int outliercounter = 0;
		// int notoutliercounter = 0;
		// while (cOut.hasNext()) {
		// cOut.fwd();
		// cOut.localize(pos);
		//
		// rndAccessIn.setPosition(pos);
		// double center = typeIn.getRealDouble();
		//
		// ArrayList<Double> allValues = new ArrayList<Double>();
		// for (int j = pos[0] - window; j <= pos[0] + window; j++) {
		// for (int i = pos[1] - window; i <= pos[1] + window; i++) {
		// if (0 <= j && j < xDim && 0 <= i && i < yDim
		// && (j != pos[0] || i != pos[1])) {
		// if (j == pos[0] && i == pos[1])
		// System.out.println("uncool");
		// // System.out.println("x pos: " + j + " und y pos: " +
		// // i);
		// rndAccessIn.setPosition(new int[] { j, i });
		// allValues.add(typeIn.getRealDouble());
		// }
		// }
		// }

		// double sigma = calcLocalSigma(allValues);
		//
		// ListIterator<Double> allIter = allValues.listIterator();
		// ArrayList<Double> selectedValues = new ArrayList<Double>();
		// while (allIter.hasNext()) {
		// double pixel = allIter.next();
		// if ((center - factor * sigma) < pixel
		// && pixel < (center + factor * sigma)) {
		// selectedValues.add(pixel);
		// }
		// }
		//
		// if (outlierDetection) {
		// if (selectedValues.size() < outlierPixels + 1) {
		// boolean removed = allValues.removeAll(selectedValues);
		// selectedValues = allValues;
		// outliercounter++;
		// } else {
		// selectedValues.add(center);
		// notoutliercounter++;
		// }
		// } else {
		// selectedValues.add(center);
		// }
		// double result = 0;
		// int count = 0;
		// ListIterator<Double> selIter = selectedValues.listIterator();
		// while (selIter.hasNext()) {
		// result += selIter.next();
		// ++count;
		// }
		// cOut.get().setReal(result / count);
		//
		// // System.out.println("Die aktuelle Position: [" + pos[0] + ", " +
		// // pos[1] + "]");
		// }
		// System.out.println("outliers: " + outliercounter
		// + " --- nicht-outliers: " + notoutliercounter);

	}

	// private double calcLocalSigma(ArrayList<Double> allValues) {
	// double sumMean = 0;
	// int n = 0;
	// ListIterator<Double> firstIter = allValues.listIterator();
	// while (firstIter.hasNext()) {
	// sumMean += firstIter.next();
	// ++n;
	// }
	// double mean = sumMean / n;
	// double sumSigma = 0;
	// ListIterator<Double> secondIter = allValues.listIterator();
	// while (secondIter.hasNext()) {
	// double tempValue = secondIter.next() - mean;
	// sumSigma += tempValue * tempValue;
	// }
	// return Math.sqrt(sumSigma / n);
	// }

	// private double calcSigma(TYPE input) {
	// double sumMean = 0;
	// int n = 0;
	// Cursor<T> cMean = input.cursor();
	// while (cMean.hasNext()) {
	// cMean.fwd();
	// sumMean += cMean.get().getRealDouble();
	// ++n;
	// }
	// double mean = sumMean / n;
	// double sumSigma = 0;
	// Cursor<T> cSigma = input.cursor();
	// while (cSigma.hasNext()) {
	// cSigma.fwd();
	// double tempValue = cSigma.get().getRealDouble() - mean;
	// sumSigma += tempValue * tempValue;
	// }
	// return Math.sqrt(sumSigma / n);
	// }

	@Override
	public BinaryOperation<TYPE, T, V> copy() {
		return new SigmaFilter<T, V, TYPE>(sigma,
				sigmaFactor, pixelFraction, outlierDetection);
	}
}
