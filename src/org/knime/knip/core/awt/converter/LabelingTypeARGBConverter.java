package org.knime.knip.core.awt.converter;

import java.util.HashMap;

import net.imglib2.converter.Converter;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;

public class LabelingTypeARGBConverter<L extends Comparable<L>> implements
                Converter<LabelingType<L>, ARGBType> {

        private final HashMap<Integer, Integer> colorTable;

        public LabelingTypeARGBConverter(final HashMap<Integer, Integer> colorTable) {
                this.colorTable = colorTable;
        }

        @Override
        public void convert(final LabelingType<L> input, final ARGBType output) {
                output.set(colorTable.get(input.getIndex().getInteger()));
        }

}
