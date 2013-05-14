package org.knime.knip.core.types;

import java.util.List;

import net.imglib2.labeling.LabelingMapping;
import net.imglib2.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;

public class ConstantLabelingType<T extends Comparable<T>> extends
                LabelingType<T> {

        public ConstantLabelingType(final List<T> labeling) {
                super(labeling);
        }

        /**
         * Constructor for mirroring state with another labeling
         *
         * @param type
         *                Wrapped type
         * @param mapping
         *                Mapping from wrapped type to LabelingList
         * @param generation
         *                Generation of the type
         */
        protected ConstantLabelingType(final IntegerType<?> type,
                        final LabelingMapping<T> mapping,
                        final long[] generation) {
                super(type, mapping, generation);
        }

        // this is the constructor if you want it to read from an array
        public ConstantLabelingType(final IntegerType<?> type,
                        final LabelingMapping<T> mapping) {
                super(type, mapping);
        }

        public ConstantLabelingType(final T value) {
                super(value);
        }

        // this is the constructor if you want it to be a variable
        public ConstantLabelingType() {
                super();
        }

        /**
         * ConstantLabelingType. Nothing happens here
         *
         * @param labeling
         */
        @Override
        public void setLabeling(final List<T> labeling) {
                // do nothing
        }

        /**
         * ConstantLabelingType. Nothing happens here
         *
         * @param labeling
         */
        @Override
        public void setLabeling(final T[] labeling) {
                // do nothing
        }

        /**
         * ConstantLabelingType. Nothing happens here
         *
         * @param labeling
         */
        @Override
        public void setLabel(final T label) {
                // do nothing
        }

        @Override
        public LabelingType<T> copy() {
                return new ConstantLabelingType<T>(getLabeling());
        }

}
