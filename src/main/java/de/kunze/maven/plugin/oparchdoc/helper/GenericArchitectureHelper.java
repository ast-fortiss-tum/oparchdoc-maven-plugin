package de.kunze.maven.plugin.oparchdoc.helper;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArrow;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericRule;
import de.kunze.maven.plugin.oparchdoc.transformer.GenericArchitectureArrowTransformer;
import de.kunze.maven.plugin.oparchdoc.transformer.GenericArchitecturePredicateTransformer;
import de.kunze.maven.plugin.oparchdoc.transformer.GenericArchitectureRuleTransformer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenericArchitectureHelper {

    private GenericArchitecture genericArchitecture;
    private JavaClasses importedClasses;

    HashMap<Component, DescribedPredicate<JavaClass>> predicatesForComponent;
    HashMap<String, DescribedPredicate<JavaClass>> predicatesForComponentName;
    HashMap<Component, List<GenericRule>> rulesForComponent;
    HashMap<JavaClass, String> classToComponent;
    Set<String> usedClassesSimpleNames;
    HashMap<Component, DescribedPredicate<JavaClass>> exclusivePredicatesForComponent;
    HashMap<String, DescribedPredicate<JavaClass>> exclusivePredicatesForComponentName;
    HashMap<Component, JavaClasses> javaClassesMinusSubcomponentForComponent;
    HashSet<JavaClass> allClassesInArchitecture;
    HashSet<JavaClass> allClassesInImported;
    Set<GenericArrow> allActualArrows;

    public GenericArchitectureHelper(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        this.genericArchitecture = genericArchitecture;
        this.importedClasses = importedClasses;
    }

    public HashMap<Component, DescribedPredicate<JavaClass>> getPredicatesForComponent() {
        if (predicatesForComponent == null) {
            predicatesForComponent = GenericArchitecturePredicateTransformer.getPredicatesForComponent(genericArchitecture);
        }
        return predicatesForComponent;
    }

    public HashMap<String, DescribedPredicate<JavaClass>> getPredicatesForComponentName() {
        if (predicatesForComponentName == null) {
            predicatesForComponentName = new HashMap<>();
            getPredicatesForComponent().forEach((component, predicate) -> predicatesForComponentName.put(component.getName(), predicate));
        }
        return predicatesForComponentName;
    }

    public HashMap<Component, List<GenericRule>> getRulesForComponent() {
        if (rulesForComponent == null) {
            rulesForComponent = new HashMap<>();
            rulesForComponent = GenericArchitectureRuleTransformer.rulesForComponents(getPredicatesForComponent(), getPredicatesForComponentName());
        }
        return rulesForComponent;
    }

    public HashMap<JavaClass, String> getClassToComponent() {
        if (classToComponent == null) {
            classToComponent = new HashMap<>();
            getExclusivePredicatesForComponent().forEach(((component, javaClassDescribedPredicate) -> {
                importedClasses.that(javaClassDescribedPredicate).forEach(javaClass -> {
                    classToComponent.put(javaClass, component.getName());
                });
            }));
        }
        return classToComponent;
    }

    public Set<String> getUsedClassesSimpleNames() {
        if (usedClassesSimpleNames == null) {
            usedClassesSimpleNames = new HashSet<>();
            importedClasses.forEach(javaClass -> {
                if (!javaClass.getSimpleName().isEmpty()) {
                    usedClassesSimpleNames.add(javaClass.getSimpleName());
                }
            });
        }
        return usedClassesSimpleNames;
    }

    // predicates excluding subcomponents
    public HashMap<Component, DescribedPredicate<JavaClass>> getExclusivePredicatesForComponent() {
        if (exclusivePredicatesForComponent == null) {
            exclusivePredicatesForComponent = GenericArchitecturePredicateTransformer.getExclusivePredicateForComponent(genericArchitecture);
        }
        return exclusivePredicatesForComponent;
    }

    public HashMap<Component, JavaClasses> getJavaClassesMinusSubcomponentForComponent() {
        if (javaClassesMinusSubcomponentForComponent == null) {
            javaClassesMinusSubcomponentForComponent = new HashMap<>();
            getExclusivePredicatesForComponent().forEach((component, predicate) -> {
                javaClassesMinusSubcomponentForComponent.put(component, importedClasses.that(predicate));
            });
        }
        return javaClassesMinusSubcomponentForComponent;
    }

    public HashMap<String, DescribedPredicate<JavaClass>> getExclusivePredicatesForComponentName() {
        if (exclusivePredicatesForComponentName == null) {
            exclusivePredicatesForComponentName = new HashMap<>();
            getExclusivePredicatesForComponent().forEach((component, predicate) -> exclusivePredicatesForComponentName.put(component.getName(), predicate));
        }
        return exclusivePredicatesForComponentName;
    }

    public Set<JavaClass> getAllClassesInArchitecture() {
        if (allClassesInArchitecture == null) {
            allClassesInArchitecture = new HashSet<>();
            getJavaClassesMinusSubcomponentForComponent().values().forEach(javaClasses -> {
                allClassesInArchitecture.addAll(javaClasses);
            });
        }
        return allClassesInArchitecture;
    }

    public Set<JavaClass> getAllClassesInImported() {
        if (allClassesInImported == null) {
            allClassesInImported = new HashSet<>();
            allClassesInImported.addAll(importedClasses);
        }
        return allClassesInImported;
    }

    public Set<GenericArrow> getAllActualArrows() {
        if (allActualArrows == null) {
            allActualArrows = GenericArchitectureArrowTransformer.getAllActualArrowsForClasses(getAllClassesInArchitecture());
        }
        return allActualArrows;
    }
}
