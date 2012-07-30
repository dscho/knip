package org.knime.knip.core.awt.converter;

import java.util.HashMap;

import net.imglib2.converter.Converter;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;

public class LabelingTypeARGBConverter<L extends Comparable<L>>
                implements Converter<LabelingType<L>, ARGBType> {


        private final HashMap<Integer, Integer> colorTable;

        public LabelingTypeARGBConverter(HashMap<Integer, Integer> colorTable) {
                this.colorTable = colorTable;
        }


        @Override
        public void convert(LabelingType<L> input, ARGBType output) {

                int labelIndex = input.getMapping()
                                .indexOf(input.getLabeling());

                output.set(colorTable.get(labelIndex));
        }

}
