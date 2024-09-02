package de.kunze.maven.plugin.oparchdoc.transformer;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Formatters;
import com.tngtech.archunit.core.domain.JavaClass;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;

import java.util.HashMap;
import java.util.List;

public class GenericArchitecturePredicateTransformer {

    public static HashMap<Component, DescribedPredicate<JavaClass>> getPredicatesForComponent(
            GenericArchitecture genericArchitecture) {
        HashMap<Component, DescribedPredicate<JavaClass>> predicatesForComponent = new HashMap<>();
        genericArchitecture.getComponents().forEach(component -> recursiveComponentPredicates(component, predicatesForComponent));
        return predicatesForComponent;
    }

    private static DescribedPredicate<JavaClass> recursiveComponentPredicates(Component component, HashMap<Component, DescribedPredicate<JavaClass>> predicatesForComponent) {
        DescribedPredicate<JavaClass> currentPredicate = getPredicateForClassesAndPackages(component.getClasses(), component.getPackages());
        if (component.getComponents() != null) {
            for (Component subComponent : component.getComponents()) {
                currentPredicate = currentPredicate.or(recursiveComponentPredicates(subComponent, predicatesForComponent));
            }
        }
        predicatesForComponent.put(component, currentPredicate);
        return currentPredicate;
    }

    public static HashMap<Component, DescribedPredicate<JavaClass>> getExclusivePredicateForComponent(GenericArchitecture genericArchitecture) {
        HashMap<Component, DescribedPredicate<JavaClass>> exclusivePredicateForComponent = new HashMap<>();
        HashMap<Component, DescribedPredicate<JavaClass>> predicateForComponent = getPredicatesForComponent(genericArchitecture);
        genericArchitecture.getComponents()
                .forEach(component -> {
                    addComponentPredicatesMinusSubpredicatesRecursive(component, exclusivePredicateForComponent, predicateForComponent);
                });
        return exclusivePredicateForComponent;
    }

    private static DescribedPredicate<JavaClass> addComponentPredicatesMinusSubpredicatesRecursive(Component component, HashMap<Component, DescribedPredicate<JavaClass>> exclusivePredicateForComponent, HashMap<Component, DescribedPredicate<JavaClass>> predicateForComponent) {
        DescribedPredicate<JavaClass> predicateFromRecursion = DescribedPredicate.alwaysFalse();

        if (component.getComponents() != null) {
            for (Component component1 : component.getComponents()) {
                predicateFromRecursion = predicateFromRecursion.or(
                        addComponentPredicatesMinusSubpredicatesRecursive(component1, exclusivePredicateForComponent, predicateForComponent));
            }
        }
        if (exclusivePredicateForComponent.containsKey(component)) {
            throw new RuntimeException();
        }
        exclusivePredicateForComponent.put(component,
                DescribedPredicate.doesNot(predicateFromRecursion).and(predicateForComponent.get(component)));

        return predicateForComponent.get(component).or(predicateFromRecursion);
    }

    private static DescribedPredicate<JavaClass> getPredicateForClassesAndPackages(List<String> classes, List<String> packages) {
        if (classes == null && packages == null) {
            return null;
        }
        if (classes == null) {
            return getPredicateForPackageNames(packages.toArray(new String[0]));
        }
        if (packages == null) {
            return getPredicateForClassNames(classes);
        }
        DescribedPredicate<JavaClass> predicateForClassNames = getPredicateForClassNames(classes);
        DescribedPredicate<JavaClass> predicateForPackageNames = getPredicateForPackageNames(packages.toArray(new String[0]));
        if (predicateForClassNames != null) {
            return predicateForClassNames.or(predicateForPackageNames);
        }
        return predicateForPackageNames;
    }

    private static DescribedPredicate<JavaClass> getPredicateForClassNames(List<String> classNames) {
        if (classNames.isEmpty() || classNames.size() == 0) {
            return null;
        }
        DescribedPredicate<JavaClass> classNamesPredicate = JavaClass.Predicates.simpleName(classNames.get(0));
        for (int i = 1; i < classNames.size(); i++) {
            classNamesPredicate = classNamesPredicate.or(JavaClass.Predicates.simpleName(classNames.get(i)));
        }
        return classNamesPredicate;
    }

    private static DescribedPredicate<JavaClass> getPredicateForPackageNames(String... packageNames) {
        return JavaClass.Predicates.resideInAnyPackage(packageNames).as(Formatters.joinSingleQuoted(packageNames), new Object[0]);
    }
}
