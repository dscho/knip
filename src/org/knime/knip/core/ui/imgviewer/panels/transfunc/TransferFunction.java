package org.knime.knip.core.ui.imgviewer.panels.transfunc;

/**
 * A simple TransferFunction that maps values from the range [0, 1] to values in the range [0, 1].<br>
 * 
 * @author muethingc
 *
 */
public interface TransferFunction {

    /**
     * Get the corresponding value at pos.<br>
     * 
     * @param pos the position to check. Must be between from the range [0.0, 1.0].
     * @return the corresponding value. Will be of value [0.0, 1.0].
     */
    public double getValueAt(final double pos);

    /**
     * Get a deep copy of this instance.<br>
     * 
     * @return a nice copy
     */
    public TransferFunction copy();

    /**
     * Zoom into this function.<br>
     *
     * @param lower the new lower point
     * @param upper the new upper point
     */
    public void zoom(final double lower, final double upper);
}
