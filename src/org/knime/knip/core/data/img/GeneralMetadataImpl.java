package org.knime.knip.core.data.img;

import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;
import net.imglib2.ops.operation.metadata.unary.CopyCalibratedSpace;
import net.imglib2.ops.operation.metadata.unary.CopyNamed;
import net.imglib2.ops.operation.metadata.unary.CopySourced;
import net.imglib2.ops.util.metadata.CalibratedSpaceImpl;
import net.imglib2.ops.util.metadata.NamedImpl;
import net.imglib2.ops.util.metadata.SourcedImpl;

/**
 * Implementation of GeneralMetadata
 *
 * @author dietzc
 */
public class GeneralMetadataImpl implements GeneralMetadata {

        private final CalibratedSpace m_cs;
        private final Named m_named;
        private final Sourced m_sourced;

        public GeneralMetadataImpl(int numDimensions) {
                this.m_cs = new CalibratedSpaceImpl(numDimensions);
                this.m_named = new NamedImpl();
                this.m_sourced = new SourcedImpl();
        }

        public GeneralMetadataImpl(CalibratedSpace cs, Named named,
                        Sourced sourced) {
                this(cs.numDimensions());

                new CopyNamed<Named>().compute(named, m_named);
                new CopySourced<Sourced>().compute(sourced, m_sourced);
                new CopyCalibratedSpace<CalibratedSpace>().compute(cs, m_cs);
        }

        public GeneralMetadataImpl(CalibratedSpace cs, Metadata metadata) {
                this(cs, metadata, metadata);
        }

        public GeneralMetadataImpl(CalibratedSpace space,
                        GeneralMetadata metadata) {
                this(space, metadata, metadata);
        }

        public GeneralMetadataImpl(GeneralMetadata metadata) {
                this(metadata, metadata, metadata);
        }

        public GeneralMetadataImpl(Metadata metadata) {
                this(metadata, metadata, metadata);
        }

        @Override
        public int getAxisIndex(final AxisType axis) {
                return m_cs.getAxisIndex(axis);
        }

        @Override
        public AxisType axis(final int d) {
                return m_cs.axis(d);
        }

        @Override
        public void axes(final AxisType[] target) {
                m_cs.axes(target);
        }

        @Override
        public void setAxis(final AxisType axis, final int d) {
                m_cs.setAxis(axis, d);
        }

        @Override
        public double calibration(final int d) {
                return m_cs.calibration(d);
        }

        @Override
        public void calibration(final double[] target) {
                m_cs.calibration(target);
        }

        @Override
        public void calibration(final float[] target) {
                m_cs.calibration(target);
        }

        @Override
        public void setCalibration(final double value, final int d) {
                m_cs.setCalibration(value, d);
        }

        @Override
        public void setCalibration(final double[] cal) {
                m_cs.setCalibration(cal);
        }

        @Override
        public void setCalibration(final float[] cal) {
                m_cs.setCalibration(cal);
        }

        @Override
        public String getName() {
                return m_named.getName();
        }

        @Override
        public void setName(String name) {
                m_named.setName(name);
        }

        @Override
        public String getSource() {
                return m_sourced.getSource();
        }

        @Override
        public void setSource(String source) {
                this.m_sourced.setSource(source);
        }

        @Override
        public int numDimensions() {
                return m_cs.numDimensions();
        }

}
