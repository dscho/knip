package org.knime.knip.core.data.img;

import net.imglib2.img.Img;
import net.imglib2.labeling.Labeling;
import net.imglib2.meta.CalibratedSpace;
import net.imglib2.meta.Named;
import net.imglib2.meta.Sourced;

/**
 * Any metadata {@link Labeling} and {@link Img} have as common Metadata
 * 
 * @author dietzc
 * 
 */
public interface GeneralMetadata extends CalibratedSpace, Named, Sourced {

}
