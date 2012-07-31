package org.knime.knip.core.algorithm.extendedem;

class StatsTmp {
        public double count = 0;
        public double sum = 0;
        public double sumSq = 0;
        public double stdDev = Double.NaN;
        public double mean = Double.NaN;
        public double min = Double.NaN;
        public double max = Double.NaN;

        public void add(final double value, final double n) {

                sum += value * n;
                sumSq += value * value * n;
                count += n;
                if (Double.isNaN(min)) {
                        min = max = value;
                } else if (value < min) {
                        min = value;
                } else if (value > max) {
                        max = value;
                }
        }

        public void calculateDerived() {

                mean = Double.NaN;
                stdDev = Double.NaN;
                if (count > 0) {
                        mean = sum / count;
                        stdDev = Double.POSITIVE_INFINITY;
                        if (count > 1) {
                                stdDev = sumSq - (sum * sum) / count;
                                stdDev /= (count - 1);
                                if (stdDev < 0) {
                                        // System.err.println("Warning: stdDev value = "
                                        // + stdDev
                                        // + " -- rounded to zero.");
                                        stdDev = 0;
                                }
                                stdDev = Math.sqrt(stdDev);
                        }
                }
        }
}