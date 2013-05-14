package org.knime.knip.core.io.externalization;

import net.imglib2.img.planar.PlanarImg.PlanarContainerSampler;

public class PlanarImgContainerSamplerImpl implements PlanarContainerSampler {

    private int m_currentSliceIndex = -1;

    public PlanarImgContainerSamplerImpl() {

    }

    public PlanarImgContainerSamplerImpl(final int startIndex) {
        m_currentSliceIndex = startIndex;
    }

    @Override
    public int getCurrentSliceIndex() {
        return m_currentSliceIndex;
    }

    public int fwd() {
        return m_currentSliceIndex++;
    }

    public void setCurrentSlice(final int slice) {
        m_currentSliceIndex = slice;
    }

}
