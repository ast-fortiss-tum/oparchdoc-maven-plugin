package de.kunze.maven.plugin.oparchdoc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface PartOfComponent {
    String component() default "";
}
