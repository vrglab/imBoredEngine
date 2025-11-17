package org.vrglab.imBoredEngine.core.initializer.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
@SuppressWarnings("unused")
public @interface CalledDuringLoop {
    int priority() default 0;
}
