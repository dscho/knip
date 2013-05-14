package org.knime.knip.core.awt.parametersupport;

import org.knime.knip.core.awt.lookup.LookupTable;

/**
 * Renderer of this instance use a LookupTable for Renderering.<br>
 *
 * @author muethingc
 */
public interface RendererWithLookupTable<T, U> {

    /**
     * Set a new lookup table for the next rendering pass.<br>
     *
     * @param table
     *                the table to use
     */
    public void setLookupTable(final LookupTable<T, U> table);

}
