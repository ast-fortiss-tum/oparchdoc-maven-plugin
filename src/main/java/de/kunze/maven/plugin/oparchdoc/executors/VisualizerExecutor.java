package de.kunze.maven.plugin.oparchdoc.executors;

import com.tngtech.archunit.core.domain.JavaClasses;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.helper.VisualizerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VisualizerExecutor {

    public enum ViewType {
        OVERVIEW,
        COMPONENT_CLASS,
        FULL,
    }

    public enum VisibilityLevel {
        NONE, // display no violations
        COMPONENT, // display violations component level
        CLASS_IF_IN_COMPONENTS, // display class level if in component
        COMPONENT_OR_CLASS, // display class level violations only if not component level
        CLASS // display class level violations
    }

    private static Optional<Component> findComponentByName(String componentName, Component component) {
        if (component.getName().equals(componentName)) {
            return Optional.of(component);
        }

        if (component.getComponents() == null || component.getComponents().isEmpty()) {
            return Optional.empty();
        }
        for (Component subComp : component.getComponents()) {
            Optional<Component> optionalComponent = findComponentByName(componentName, subComp);
            if (optionalComponent.isPresent()) {
                return optionalComponent;
            }
        }

        return Optional.empty();
    }

    public static String generateInnerComponentView(String componentName, GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        List<Component> filteredComponents = new ArrayList<>();
        genericArchitecture.getComponents().forEach(component -> {
            Optional<Component> optionalComponent = findComponentByName(componentName, component);
            optionalComponent.ifPresent(filteredComponents::add);
        });

        return new VisualizerHelper(new GenericArchitecture(genericArchitecture.getName(), genericArchitecture.getDescription(), filteredComponents), importedClasses)
                .generateView(ViewType.FULL, true, true, VisibilityLevel.CLASS_IF_IN_COMPONENTS, VisibilityLevel.CLASS, false, true, "partial_" + componentName);
    }

    public static String generateGenericOverview(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        return new VisualizerHelper(genericArchitecture, importedClasses)
                .generateView(ViewType.OVERVIEW, true, true, VisibilityLevel.COMPONENT, VisualizerExecutor.VisibilityLevel.COMPONENT, false, true, "");
    }

    public static String generateGenericExtendedOverview(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        return new VisualizerHelper(genericArchitecture, importedClasses)
                .generateView(ViewType.OVERVIEW, true, true, VisibilityLevel.COMPONENT_OR_CLASS, VisualizerExecutor.VisibilityLevel.COMPONENT_OR_CLASS, false, true, "");
    }

    public static String generateGenericClassOverview(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        return new VisualizerHelper(genericArchitecture, importedClasses)
                .generateView(ViewType.COMPONENT_CLASS, true, true, VisibilityLevel.CLASS_IF_IN_COMPONENTS, VisualizerExecutor.VisibilityLevel.CLASS_IF_IN_COMPONENTS, false, true, "");
    }

    public static String generateGenericFullOverview(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        return new VisualizerHelper(genericArchitecture, importedClasses)
                .generateView(ViewType.FULL, true, true, VisibilityLevel.CLASS, VisualizerExecutor.VisibilityLevel.CLASS, false, true, "");
    }

    public static String generateCustomView(GenericArchitecture genericArchitecture, JavaClasses importedClasses, VisualizerExecutor.ViewType viewType, boolean displayIdeal, boolean displayDescription, VisibilityLevel visibilityLevel, VisibilityLevel displayActual, boolean displayLegend, boolean chosenLeftRight, String fileNamePrefix) {
        return new VisualizerHelper(genericArchitecture, importedClasses)
                .generateView(viewType, displayIdeal, displayDescription, visibilityLevel, displayActual, displayLegend, chosenLeftRight, fileNamePrefix);
    }
}
