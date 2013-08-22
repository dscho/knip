package org.knime.knip.core.data.img;

import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.DefaultCalibratedSpace;
import net.imglib2.meta.DefaultNamed;
import net.imglib2.meta.DefaultSourced;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.MetadataUtil;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;

/**
 * Implementation of GeneralMetadata
 *
 * @author dietzc
 */
public class GeneralMetadataImpl extends DefaultCalibratedSpace implements GeneralMetadata {

    private final Named m_named;

    private final Sourced m_sourced;

    public GeneralMetadataImpl(final int numDimensions) {
        this.m_named = new DefaultNamed();
        this.m_sourced = new DefaultSourced();
    }

    public GeneralMetadataImpl(final CalibratedSpace<CalibratedAxis> cs, final Named named, final Sourced sourced) {
        this(cs.numDimensions());

        MetadataUtil.copyName(named, m_named);
        MetadataUtil.copySource(sourced, m_sourced);
        MetadataUtil.copyTypedSpace(cs, this);
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

}
