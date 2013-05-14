package org.knime.knip.core.awt.parametersupport;

import net.imglib2.display.ColorTable;

/*
 * Similar to the lookup renderer. To make sure that transfer functions and
 * color tables do not interfere a 2nd interface is defined.
 */
/**
 * 
 * @author zinsmaie
 * 
 */
public interface RendererWithColorTable {

    public void setColorTables(ColorTable[] tables);

}
