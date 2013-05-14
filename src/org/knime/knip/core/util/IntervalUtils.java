package org.knime.knip.core.util;

import net.imglib2.Interval;

public class IntervalUtils {

        public static synchronized boolean intervalEquals(final Interval a, final Interval b) {

                if (a.numDimensions() != b.numDimensions()) {
                        return false;
                }

                for (int d = 0; d < a.numDimensions(); d++) {
                        if (a.min(d) != b.min(d) || a.max(d) != b.max(d)) {
                                return false;
                        }
                }

                return true;
        }
}
