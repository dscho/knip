package org.knime.knip.core.awt.lookup;

/**
 *
 * @author muethingc
 *
 */
public interface LookupTable<T, U> {

    public U lookup(final T value);

}
