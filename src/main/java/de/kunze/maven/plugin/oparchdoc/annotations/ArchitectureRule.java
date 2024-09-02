package de.kunze.maven.plugin.oparchdoc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface ArchitectureRule {
    String[] onlyDependenciesTo() default "";

    String[] onlyDependenciesFrom() default "";

    String[] onlyAccessTo() default "";

    String[] onlyAccessFrom() default "";

    String[] onlyCreatedIn() default "";

    String[] onlyCreatedFrom() default "";

    String[] assignableTo() default "";

    String[] assignableFrom() default "";

}
