package org.knime.knip.core.data.img;

import java.util.ArrayList;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.ImageMetadata;

public class ImageMetadataImpl implements ImageMetadata {

    private int m_validBits;

    private final ArrayList<Double> m_channelMin;

    private final ArrayList<Double> m_channelMax;

    private int m_compositeChannelCount = 1;

    private final ArrayList<ColorTable> m_lut;

    public ImageMetadataImpl() {
        this.m_channelMin = new ArrayList<Double>();
        this.m_channelMax = new ArrayList<Double>();

        this.m_lut = new ArrayList<ColorTable>();
    }

    @Override
    public int getValidBits() {
        return m_validBits;
    }

    @Override
    public void setValidBits(final int bits) {
        m_validBits = bits;
    }

    @Override
    public double getChannelMinimum(final int c) {
        if ((c < 0) || (c >= m_channelMin.size())) {
            return Double.NaN;
        }
        final Double d = m_channelMin.get(c);
        return d == null ? Double.NaN : d;
    }

    @Override
    public void setChannelMinimum(final int c, final double min) {
        if (c < 0) {
            throw new IllegalArgumentException("Invalid channel: " + c);
        }
        if (c >= m_channelMin.size()) {
            m_channelMin.ensureCapacity(c + 1);
            for (int i = m_channelMin.size(); i <= c; i++) {
                m_channelMin.add(null);
            }
        }
        m_channelMin.set(c, min);
    }

    @Override
    public double getChannelMaximum(final int c) {
        if ((c < 0) || (c >= m_channelMax.size())) {
            return Double.NaN;
        }
        final Double d = m_channelMax.get(c);
        return d == null ? Double.NaN : d;
    }

    @Override
    public void setChannelMaximum(final int c, final double max) {
        if (c < 0) {
            throw new IllegalArgumentException("Invalid channel: " + c);
        }
        if (c >= m_channelMax.size()) {
            m_channelMax.ensureCapacity(c + 1);
            for (int i = m_channelMax.size(); i <= c; i++) {
                m_channelMax.add(null);
            }
        }
        m_channelMax.set(c, max);
    }

    @Override
    public int getCompositeChannelCount() {
        return m_compositeChannelCount;
    }

    @Override
    public void setCompositeChannelCount(final int value) {
        m_compositeChannelCount = value;
    }

    @Override
    public void initializeColorTables(final int count) {
        m_lut.ensureCapacity(count);
        m_lut.clear();
        for (int i = 0; i < count; i++) {
            m_lut.add(null);
        }
    }

    @Override
    public int getColorTableCount() {
        return m_lut.size();
    }

    @Override
    public ColorTable getColorTable(final int no) {
        return m_lut.get(no);
    }

    @Override
    public void setColorTable(final ColorTable colorTable, final int no) {
        m_lut.set(no, colorTable);
    }

}
