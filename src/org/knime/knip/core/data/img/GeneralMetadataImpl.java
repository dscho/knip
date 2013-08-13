package org.knime.knip.core.data.img;

import net.imglib2.meta.AxisType;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.DefaultCalibratedSpace;
import net.imglib2.meta.DefaultNamed;
import net.imglib2.meta.DefaultSourced;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;
import net.imglib2.ops.operation.metadata.unary.CopyCalibratedSpace;
import net.imglib2.ops.operation.metadata.unary.CopyNamed;
import net.imglib2.ops.operation.metadata.unary.CopySourced;

/**
 * Implementation of GeneralMetadata
 *
 * @author dietzc
 */
public class GeneralMetadataImpl implements GeneralMetadata {

    private final CalibratedSpace<CalibratedAxis> m_cs;

    private final Named m_named;

    private final Sourced m_sourced;

    public GeneralMetadataImpl(final int numDimensions) {
        this.m_cs = new DefaultCalibratedSpace(numDimensions);
        this.m_named = new DefaultNamed();
        this.m_sourced = new DefaultSourced();
    }

    public GeneralMetadataImpl(final CalibratedSpace<CalibratedAxis> cs, final Named named, final Sourced sourced) {
        this(cs.numDimensions());

        new CopyNamed<Named>().compute(named, m_named);
        new CopySourced<Sourced>().compute(sourced, m_sourced);
        new CopyCalibratedSpace<CalibratedSpace<CalibratedAxis>>().compute(cs, m_cs);
    }

    public GeneralMetadataImpl(final CalibratedSpace<CalibratedAxis> cs, final Metadata metadata) {
        this(cs, metadata, metadata);
    }

    public GeneralMetadataImpl(final CalibratedSpace<CalibratedAxis> space, final GeneralMetadata metadata) {
        this(space, metadata, metadata);
    }

    public GeneralMetadataImpl(final GeneralMetadata metadata) {
        this(metadata, metadata, metadata);
    }

    public GeneralMetadataImpl(final Metadata metadata) {
        this(metadata, metadata, metadata);
    }

    @Override
    public int dimensionIndex(final AxisType type) {
        return m_cs.dimensionIndex(type);
    }

    @Override
    public CalibratedAxis axis(final int d) {
        return m_cs.axis(d);
    }

    @Override
    public void axes(final CalibratedAxis[] target) {
        m_cs.axes(target);
    }

    @Override
    public void setAxis(final CalibratedAxis axis, final int d) {
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
    public void setName(final String name) {
        m_named.setName(name);
    }

    @Override
    public String getSource() {
        return m_sourced.getSource();
    }

    @Override
    public void setSource(final String source) {
        this.m_sourced.setSource(source);
    }

    @Override
    public int numDimensions() {
        return m_cs.numDimensions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String unit(final int d) {
        return m_cs.unit(d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnit(final String unit, final int d) {
        m_cs.setUnit(unit, d);
    }

}
