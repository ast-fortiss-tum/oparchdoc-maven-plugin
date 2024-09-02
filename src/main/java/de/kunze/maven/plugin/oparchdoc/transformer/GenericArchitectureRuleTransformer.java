package de.kunze.maven.plugin.oparchdoc.transformer;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.ArrowTypes;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;

public class GenericArchitectureRuleTransformer {

    public static HashMap<Component, List<GenericRule>> rulesForComponents(
            HashMap<Component, DescribedPredicate<JavaClass>> predicatesForComponent,
            HashMap<String, DescribedPredicate<JavaClass>> predicatesForComponentName) {
        HashMap<Component, List<GenericRule>> rulesForComponents = new HashMap<>();

        predicatesForComponent.keySet().forEach(component -> {
            rulesForComponents.put(component, rulesForComponent(component, predicatesForComponent, predicatesForComponentName));
        });

        return rulesForComponents;
    }

    private static List<GenericRule> rulesForComponent(Component component,
                                                       HashMap<Component, DescribedPredicate<JavaClass>> predicatesForComponent,
                                                       HashMap<String, DescribedPredicate<JavaClass>> predicatesForComponentName) {
        DescribedPredicate<JavaClass> predicateCurrentComponent = predicatesForComponent.get(component);

        // exclude own annotations
        DescribedPredicate<JavaClass> predicateExcludeClasses = JavaClass.Predicates.simpleName("ArchitectureRule")
                .or(JavaClass.Predicates.simpleName("PartOfComponent"))
                .or(JavaClass.Predicates.simpleName("ArchitectureDescription"));

        if (predicateCurrentComponent == null) {
            return new ArrayList<>();
        }
        List<GenericRule> rules = new ArrayList<>();

        // dependencies with other components
        if (component.getOnlyDependenciesTo() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getOnlyDependenciesTo(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getOnlyDependenciesTo(),
                    ArrowTypes.ONLY_DEPENDENCIES_TO,
                    classes().that(predicateCurrentComponent).should().onlyDependOnClassesThat(javaClassDescribedPredicate.or(predicateCurrentComponent).or(predicateExcludeClasses)),
                    component.getReasonOnlyDependenciesTo())));
        }
        if (component.getOnlyDependenciesFrom() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getOnlyDependenciesFrom(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getOnlyDependenciesFrom(),
                    ArrowTypes.ONLY_DEPENDENCIES_FROM,
                    classes().that(predicateCurrentComponent).should().onlyHaveDependentClassesThat(javaClassDescribedPredicate.or(predicateCurrentComponent).or(predicateExcludeClasses)),
                    component.getReasonOnlyDependenciesFrom())));
        }

        // access with other components
        if (component.getOnlyAccessTo() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getOnlyAccessTo(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getOnlyAccessTo(),
                    ArrowTypes.ONLY_ACCESS_TO,
                    classes().that(predicateCurrentComponent).should().onlyAccessClassesThat(javaClassDescribedPredicate.or(predicateCurrentComponent).or(JavaClass.Predicates.simpleName("Object").or(predicateExcludeClasses))),
                    component.getReasonOnlyAccessTo())));
        }
        if (component.getOnlyAccessFrom() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getOnlyAccessFrom(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getOnlyAccessFrom(),
                    ArrowTypes.ONLY_ACCESS_FROM,
                    classes().that(predicateCurrentComponent).should().onlyBeAccessed().byClassesThat(javaClassDescribedPredicate.or(predicateCurrentComponent).or(predicateExcludeClasses)),
                    component.getReasonOnlyAccessFrom())));
        }

        // constructor calls
        if (component.getOnlyCreatedIn() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getOnlyCreatedIn(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getOnlyCreatedIn(),
                    ArrowTypes.ONLY_CREATE,
                    classes().that(predicateCurrentComponent).should().onlyCallConstructorsThat(JavaMember.Predicates.declaredIn(javaClassDescribedPredicate.or(predicateCurrentComponent).or(predicateExcludeClasses))),
                    component.getReasonOnlyCreatedIn())));
        }
        if (component.getOnlyCreatedFrom() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getOnlyCreatedFrom(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getOnlyCreatedFrom(),
                    ArrowTypes.ONLY_CREATED_FROM,
                    constructors().that(JavaMember.Predicates.declaredIn(predicateCurrentComponent)).should().onlyBeCalled().byClassesThat(javaClassDescribedPredicate.or(predicateCurrentComponent).or(predicateExcludeClasses)),
                    component.getReasonOnlyCreatedFrom())));
        }
        if (component.getAssignableTo() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getAssignableTo(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getAssignableTo(),
                    ArrowTypes.ASSIGNABLE_TO,
                    classes().that(predicateCurrentComponent).should().beAssignableTo(javaClassDescribedPredicate/*.or(predicateCurrentComponent)*/),
                    component.getReasonAssignableTo())));
        }
        if (component.getAssignableFrom() != null) {
            Optional<DescribedPredicate<JavaClass>> predicateTarget = getPredicateForComponentList(
                    component.getAssignableFrom(), predicatesForComponentName);
            predicateTarget.ifPresent(javaClassDescribedPredicate -> rules.add(new GenericRule(predicateCurrentComponent,
                    javaClassDescribedPredicate,
                    component.getName(),
                    component.getAssignableFrom(),
                    ArrowTypes.ASSIGNABLE_FROM,
                    classes().that(predicateCurrentComponent).should().beAssignableFrom(javaClassDescribedPredicate/*.or(predicateCurrentComponent)*/),
                    component.getReasonAssignableFrom())));
        }
        return rules;
    }

    private static Optional<DescribedPredicate<JavaClass>> getPredicateForComponentList(
            List<String> componentsToGetPredicatesFrom, HashMap<String, DescribedPredicate<JavaClass>> predicatesForComponent) {
        if (componentsToGetPredicatesFrom.isEmpty()) {
            return Optional.empty();
        }
        DescribedPredicate<JavaClass> predicate = predicatesForComponent.get(componentsToGetPredicatesFrom.get(0));
        for (int i = 1; i < componentsToGetPredicatesFrom.size(); i++) {
            predicate = predicate.or(predicatesForComponent.get(componentsToGetPredicatesFrom.get(i)));
        }
        if (predicate == null) {
            return Optional.empty();
        }
        return Optional.of(predicate);
    }
}
