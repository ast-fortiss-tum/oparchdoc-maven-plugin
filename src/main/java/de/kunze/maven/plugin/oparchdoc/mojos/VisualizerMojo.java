package de.kunze.maven.plugin.oparchdoc.mojos;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.executors.VisualizerExecutor;
import de.kunze.maven.plugin.oparchdoc.transformer.GenericArchitectureTransformer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;


@Mojo(name = "visualizer", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class VisualizerMojo extends AbstractMojo {

    @Parameter(property = "view", defaultValue = "0")
    private String view;

    @Parameter(property = "ideal", defaultValue = "1")
    private String ideal;

    @Parameter(property = "descriptions", defaultValue = "1")
    private String descriptions;

    @Parameter(property = "violations", defaultValue = "0")
    private String violations;

    @Parameter(property = "actual", defaultValue = "0")
    private String actual;

    @Parameter(property = "legend", defaultValue = "0")
    private String legend;

    @Parameter(property = "leftright", defaultValue = "1")
    private String leftright;

    @Parameter(property = "innerComponent", defaultValue = "")
    private String innerComponent;

    @Parameter(property = "jsonpath", defaultValue = "generic.json")
    private String pathToGeneric;

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    private String importPath = "";

    public void setImportPath(String importPath) {
        this.importPath = importPath;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (importPath.isEmpty()) {
            importPath = project.getBasedir().getPath();
        }
        JavaClasses importedClasses = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES).importPath(importPath);
        GenericArchitecture genericArchitecture = GenericArchitectureTransformer.jsonToGeneric(pathToGeneric, importedClasses);

        // special case generate inner component view
        if(innerComponent != null && !innerComponent.isEmpty()){
            VisualizerExecutor.generateInnerComponentView(innerComponent, genericArchitecture, importedClasses);
            return;
        }

        VisualizerExecutor.ViewType viewType =
                switch (view) {
                    //case "0" -> VisualizerExecutor.ViewType.OVERVIEW;
                    case "1" -> VisualizerExecutor.ViewType.COMPONENT_CLASS;
                    case "2" -> VisualizerExecutor.ViewType.FULL;
                    default -> VisualizerExecutor.ViewType.OVERVIEW;
                };
        boolean displayIdeal =
                switch (ideal) {
                    case "0" -> false;
                    default -> true;
                };
        boolean displayDescriptions =
                switch (descriptions) {
                    case "0" -> false;
                    default -> true;
                };
        boolean displayLegend =
                switch (legend) {
                    case "0" -> false;
                    default -> true;
                };
        boolean chosenLeftRight =
                switch (leftright) {
                    case "0" -> false;
                    default -> true;
                };

        VisualizerExecutor.VisibilityLevel visibilityLevel = visibilityLevelForString(violations);
        VisualizerExecutor.VisibilityLevel displayActual = visibilityLevelForString(actual);

        VisualizerExecutor.generateCustomView(genericArchitecture, importedClasses, viewType, displayIdeal, displayDescriptions, visibilityLevel, displayActual, displayLegend, chosenLeftRight, "");
    }

    private VisualizerExecutor.VisibilityLevel visibilityLevelForString(String input) {
        return switch (input) {
            //case "0" -> VisualizerExecutor.ViolationDisplaySeverity.NONE;
            case "1" -> VisualizerExecutor.VisibilityLevel.COMPONENT;
            case "2" -> VisualizerExecutor.VisibilityLevel.CLASS_IF_IN_COMPONENTS;
            case "3" -> VisualizerExecutor.VisibilityLevel.COMPONENT_OR_CLASS;
            case "4" -> VisualizerExecutor.VisibilityLevel.CLASS;
            default -> VisualizerExecutor.VisibilityLevel.NONE;
        };
    }
}




