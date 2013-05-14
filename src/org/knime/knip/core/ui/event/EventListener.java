package org.knime.knip.core.ui.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a listener for a certain event. This method must have one
 * parameter requesting a class derived from {@link KNIPEvent}. The respective
 * classed must be registered at the {@link EventService} (
 * {@link EventService#subscribe(Object)}).
 *
 * @author hornm, University of Konstanz
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
    // no attributes
}
