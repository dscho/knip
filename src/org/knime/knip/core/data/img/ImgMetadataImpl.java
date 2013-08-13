package org.knime.knip.core.data.img;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.Axes;
import net.imglib2.meta.CalibratedAxis;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.DefaultCalibratedAxis;
import net.imglib2.meta.DefaultCalibratedSpace;
import net.imglib2.meta.DefaultNamed;
import net.imglib2.meta.DefaultSourced;
import net.imglib2.meta.ImageMetadata;
import net.imglib2.meta.Metadata;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;
import net.imglib2.ops.operation.metadata.unary.CopyImageMetadata;

/**
 *
 * @author dietzc, University of Konstanz
 */
public final class ImgMetadataImpl extends GeneralMetadataImpl implements ImgMetadata {

    private final ImageMetadata m_imgMetadata;

    /**
     * @param cs
     * @param named
     * @param source
     * @param imageMetadata
     */
    public ImgMetadataImpl(final CalibratedSpace<CalibratedAxis> cs, final Named named, final Sourced source,
                           final ImageMetadata imageMetadata) {
        super(cs, named, source);
        m_imgMetadata = new CopyImageMetadata<ImageMetadata>().compute(imageMetadata, new ImageMetadataImpl());

    }

    /**
     * @param numDims
     */
    public ImgMetadataImpl(final int numDims) {
        super(numDims);
        m_imgMetadata = new ImageMetadataImpl();
    }

    /**
     * @param metadata
     */
    public ImgMetadataImpl(final Metadata metadata) {
        this(metadata, metadata, metadata, metadata);
    }

    /**
     * @param cSpace
     * @param img
     */
    public ImgMetadataImpl(final CalibratedSpace<CalibratedAxis> cSpace, final Metadata img) {
        this(cSpace, img, img, img);
    }

    /**
     * @param generalMetadata
     * @param imageMetadata
     */
    public ImgMetadataImpl(final GeneralMetadata generalMetadata, final ImageMetadata imageMetadata) {
        this(generalMetadata, generalMetadata, generalMetadata, imageMetadata);
    }

    /**
     * @param metadata
     */
    public ImgMetadataImpl(final GeneralMetadata metadata) {
        this(metadata, metadata, metadata, new ImageMetadataImpl());
    }

    /**
     * Convenience constructor
     *
     * @param name the name
     * @param source the source
     * @param axes the axes
     */
    public ImgMetadataImpl(final String name, final String source, final String... axes) {
        super(new DefaultCalibratedSpace(createCalibratedAxis(axes)), new DefaultNamed(name),
                new DefaultSourced(source));
        m_imgMetadata = new ImageMetadataImpl();
    }


    /**
     * @param axes
     * @return
     */
    private static List<CalibratedAxis> createCalibratedAxis(final String[] axes) {
        List<CalibratedAxis> list = new ArrayList<CalibratedAxis>();
        for (int s = 0; s < axes.length; s++) {
            list.add(new DefaultCalibratedAxis(Axes.get(axes[s])));
        }
        return list;
    }

    @Override
    public int getValidBits() {
        return m_imgMetadata.getValidBits();
    }

    @Override
    public void setValidBits(final int bits) {
        m_imgMetadata.setValidBits(bits);
    }

    @Override
    public double getChannelMinimum(final int c) {
        return m_imgMetadata.getChannelMinimum(c);
    }

    @Override
    public void setChannelMinimum(final int c, final double min) {
        m_imgMetadata.setChannelMinimum(c, min);
    }

    @Override
    public double getChannelMaximum(final int c) {
        return m_imgMetadata.getChannelMaximum(c);
    }

    @Override
    public void setChannelMaximum(final int c, final double max) {
        m_imgMetadata.setChannelMaximum(c, max);
    }

    @Override
    public int getCompositeChannelCount() {
        return m_imgMetadata.getCompositeChannelCount();
    }

    @Override
    public void setCompositeChannelCount(final int count) {
        m_imgMetadata.setCompositeChannelCount(count);
    }

    @Override
    public void initializeColorTables(final int count) {
        m_imgMetadata.initializeColorTables(count);
    }

    @Override
    public int getColorTableCount() {
        return m_imgMetadata.getColorTableCount();
    }

    @Override
    public ColorTable getColorTable(final int no) {
        return m_imgMetadata.getColorTable(no);
    }

    @Override
    public void setColorTable(final ColorTable colorTable, final int no) {
        m_imgMetadata.setColorTable(colorTable, no);
    }
}
