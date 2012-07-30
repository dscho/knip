package org.knime.knip.core.algorithm.extendedem;

class AttributeStats {
        public int[] nominalCounts;
        public double[] nominalWeights;
        public int totalCount = 0;
        public int missingCount = 0;
        public int uniqueCount = 0;
        public int intCount = 0;
        public int realCount = 0;
        public int distinctCount = 0;
        public Stats numericStats;
        public double SMALL = 1e-6;

        public boolean eq(final double a, final double b) {

                return (a - b < SMALL) && (b - a < SMALL);
        }

        protected void addDistinct(final double value, final int count,
                        final double weight) {

                if (count > 0) {
                        if (count == 1) {
                                uniqueCount++;
                        }
                        if (eq(value, ((int) value))) {
                                intCount += count;
                        } else {
                                realCount += count;
                        }
                        if (nominalCounts != null) {
                                nominalCounts[(int) value] = count;
                                nominalWeights[(int) value] = weight;
                        }
                        if (numericStats != null) {
                                // numericStats.add(value, count);
                                numericStats.add(value, weight);
                                numericStats.calculateDerived();
                        }
                }
                distinctCount++;
        }

}