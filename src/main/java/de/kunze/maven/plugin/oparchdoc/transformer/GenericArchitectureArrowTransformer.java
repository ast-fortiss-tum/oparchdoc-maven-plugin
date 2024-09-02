package de.kunze.maven.plugin.oparchdoc.transformer;

import com.tngtech.archunit.core.domain.JavaClass;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.ArrowTypes;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArrow;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GenericArchitectureArrowTransformer {

    public static Set<GenericArrow> getAllActualArrowsForClasses(Collection<JavaClass> classesToGetArrowsFor) {
        Set<GenericArrow> actualArrows = new HashSet<>();

        classesToGetArrowsFor.forEach(javaClass -> {
            javaClass.getAccessesToSelf().forEach(javaAccess -> {
                actualArrows.add(new GenericArrow(javaAccess.getOriginOwner(), javaAccess.getTargetOwner(), ArrowTypes.ONLY_ACCESS_TO));
            });

            javaClass.getAccessesFromSelf().forEach(javaAccess -> {
                actualArrows.add(new GenericArrow(javaAccess.getOriginOwner(), javaAccess.getTargetOwner(), ArrowTypes.ONLY_ACCESS_TO));
            });

            javaClass.getDirectDependenciesToSelf().forEach(dependency -> {
                actualArrows.add(new GenericArrow(dependency.getOriginClass(), dependency.getTargetClass(), ArrowTypes.ONLY_DEPENDENCIES_TO));
            });

            javaClass.getDirectDependenciesFromSelf().forEach(dependency -> {
                actualArrows.add(new GenericArrow(dependency.getOriginClass(), dependency.getTargetClass(), ArrowTypes.ONLY_DEPENDENCIES_TO));
            });

            javaClass.getConstructorCallsToSelf().forEach(javaConstructorCall -> {
                actualArrows.add(new GenericArrow(javaConstructorCall.getOriginOwner(), javaConstructorCall.getTargetOwner(), ArrowTypes.ONLY_CREATE));
            });
            javaClass.getConstructorCallsFromSelf().forEach(javaConstructorCall -> {
                actualArrows.add(new GenericArrow(javaConstructorCall.getOriginOwner(), javaConstructorCall.getTargetOwner(), ArrowTypes.ONLY_CREATE));
            });

            javaClass.getAllClassesSelfIsAssignableTo().forEach(target -> {
                actualArrows.add(new GenericArrow(javaClass, target, ArrowTypes.ASSIGNABLE_TO));
            });

            javaClass.getAllSubclasses().forEach(target -> {
                actualArrows.add(new GenericArrow(javaClass, target, ArrowTypes.ASSIGNABLE_FROM));
            });
        });

        return actualArrows;
    }
}
