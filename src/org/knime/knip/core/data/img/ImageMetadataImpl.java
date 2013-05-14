package org.knime.knip.core.data.img;

import java.util.ArrayList;

import net.imglib2.display.ColorTable;
import net.imglib2.meta.ImageMetadata;

public class ImageMetadataImpl implements ImageMetadata {

    private int validBits;

    private final ArrayList<Double> channelMin;

    private final ArrayList<Double> channelMax;

    private int compositeChannelCount = 1;

    private final ArrayList<ColorTable> lut;

    public ImageMetadataImpl() {
        this.channelMin = new ArrayList<Double>();
        this.channelMax = new ArrayList<Double>();

        this.lut = new ArrayList<ColorTable>();
    }

    @Override
    public int getValidBits() {
        return validBits;
    }

    @Override
    public void setValidBits(final int bits) {
        validBits = bits;
    }

    @Override
    public double getChannelMinimum(final int c) {
        if ((c < 0) || (c >= channelMin.size())) {
            return Double.NaN;
        }
        final Double d = channelMin.get(c);
        return d == null ? Double.NaN : d;
    }

    @Override
    public void setChannelMinimum(final int c, final double min) {
        if (c < 0) {
            throw new IllegalArgumentException("Invalid channel: " + c);
        }
        if (c >= channelMin.size()) {
            channelMin.ensureCapacity(c + 1);
            for (int i = channelMin.size(); i <= c; i++) {
                channelMin.add(null);
            }
        }
        channelMin.set(c, min);
    }

    @Override
    public double getChannelMaximum(final int c) {
        if ((c < 0) || (c >= channelMax.size())) {
            return Double.NaN;
        }
        final Double d = channelMax.get(c);
        return d == null ? Double.NaN : d;
    }

    @Override
    public void setChannelMaximum(final int c, final double max) {
        if (c < 0) {
            throw new IllegalArgumentException("Invalid channel: " + c);
        }
        if (c >= channelMax.size()) {
            channelMax.ensureCapacity(c + 1);
            for (int i = channelMax.size(); i <= c; i++) {
                channelMax.add(null);
            }
        }
        channelMax.set(c, max);
    }

    @Override
    public int getCompositeChannelCount() {
        return compositeChannelCount;
    }

    @Override
    public void setCompositeChannelCount(final int value) {
        compositeChannelCount = value;
    }

    @Override
    public void initializeColorTables(final int count) {
        lut.ensureCapacity(count);
        lut.clear();
        for (int i = 0; i < count; i++) {
            lut.add(null);
        }
    }

    @Override
    public int getColorTableCount() {
        return lut.size();
    }

    @Override
    public ColorTable getColorTable(final int no) {
        return lut.get(no);
    }

    @Override
    public void setColorTable(final ColorTable colorTable, final int no) {
        lut.set(no, colorTable);
    }

}
