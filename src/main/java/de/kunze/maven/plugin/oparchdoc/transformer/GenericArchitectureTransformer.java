package de.kunze.maven.plugin.oparchdoc.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.annotations.ArchitectureRule;
import de.kunze.maven.plugin.oparchdoc.annotations.PartOfComponent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GenericArchitectureTransformer {

    public static GenericArchitecture jsonToGeneric(String pathToJson, JavaClasses importedClasses) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GenericArchitecture genericArchitecture = Optional.of(objectMapper.readValue(new File(pathToJson), GenericArchitecture.class)).orElseThrow();
            return addAnnotatedClassesToArchitecture(genericArchitecture, importedClasses);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static GenericArchitecture addAnnotatedClassesToArchitecture(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        // include according to annotations
        importedClasses.that(CanBeAnnotated.Predicates.annotatedWith(PartOfComponent.class)).forEach(javaClass -> {
            genericArchitecture.getComponents().forEach(component -> addClassToComponent(component, javaClass, javaClass.getAnnotationOfType(PartOfComponent.class).component()));
        });

        HashMap<Component, DescribedPredicate<JavaClass>> exclusivePredicatesForComponent = GenericArchitecturePredicateTransformer.getExclusivePredicateForComponent(genericArchitecture);

        HashMap<JavaClass, Component> classToComponent = new HashMap<>();
        exclusivePredicatesForComponent.forEach(((component, javaClassDescribedPredicate) -> {
            importedClasses.that(javaClassDescribedPredicate).forEach(javaClass -> {
                classToComponent.put(javaClass, component);
            });
        }));

        importedClasses.that(CanBeAnnotated.Predicates.annotatedWith(ArchitectureRule.class)).forEach(javaClass -> {
            ArchitectureRule architectureRule = javaClass.getAnnotationOfType(ArchitectureRule.class);

            Component component = new Component();
            component.setName(javaClass.getSimpleName());
            List<String> classes = new ArrayList<>();
            classes.add(javaClass.getSimpleName());
            component.setClasses(classes);

            if (classToComponent.containsKey(javaClass)) {
                Component temp = classToComponent.get(javaClass);
                // exact match
                if (temp.getClasses() != null
                        && temp.getClasses().contains(javaClass.getSimpleName())
                        && temp.getClasses().size() == 1
                        && temp.getComponents() == null
                        && temp.getPackages() == null) {
                    component = temp;
                } else {
                    if (temp.getComponents() == null) {
                        temp.setComponents(new ArrayList<>());
                    }
                    temp.getComponents().add(component);
                }
            } else {
                genericArchitecture.getComponents().add(component);
            }

            if (!architectureRule.onlyDependenciesTo()[0].isEmpty()) {
                component.setOnlyDependenciesTo(Arrays.asList(architectureRule.onlyDependenciesTo()));
            }
            if (!architectureRule.onlyDependenciesFrom()[0].isEmpty()) {
                component.setOnlyDependenciesFrom(Arrays.asList(architectureRule.onlyDependenciesFrom()));
            }

            if (!architectureRule.onlyAccessTo()[0].isEmpty()) {
                component.setOnlyAccessTo(Arrays.asList(architectureRule.onlyAccessTo()));
            }
            if (!architectureRule.onlyAccessFrom()[0].isEmpty()) {
                component.setOnlyAccessFrom(Arrays.asList(architectureRule.onlyAccessFrom()));
            }

            if (!architectureRule.onlyCreatedIn()[0].isEmpty()) {
                component.setOnlyCreatedIn(Arrays.asList(architectureRule.onlyCreatedIn()));
            }
            if (!architectureRule.onlyCreatedFrom()[0].isEmpty()) {
                component.setOnlyCreatedFrom(Arrays.asList(architectureRule.onlyCreatedFrom()));
            }

            if (!architectureRule.assignableTo()[0].isEmpty()) {
                component.setAssignableTo(Arrays.asList(architectureRule.assignableTo()));
            }
            if (!architectureRule.assignableFrom()[0].isEmpty()) {
                component.setAssignableFrom(Arrays.asList(architectureRule.assignableFrom()));
            }
        });

        return genericArchitecture;
    }

    private static void addClassToComponent(Component currentComponent, JavaClass toBeAdded, String component) {
        if (currentComponent.getName().equals(component)) {
            currentComponent.getClasses().add(toBeAdded.getSimpleName());
        } else {
            if (currentComponent.getComponents() != null) {
                currentComponent.getComponents().forEach(component1 -> addClassToComponent(component1, toBeAdded, component));
            }
        }
    }
}
