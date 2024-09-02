package de.kunze.maven.plugin.oparchdoc.mojos;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.executors.TestExecutor;
import de.kunze.maven.plugin.oparchdoc.transformer.GenericArchitectureTransformer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "test", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TesterMojo extends AbstractMojo {

    @Parameter(property = "onlyArchitecture", defaultValue = "0")
    private String onlyArchitecture;

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

        boolean considerOnlyInArchitecture =
                switch (onlyArchitecture) {
                    case "0" -> false;
                    default -> true;
                };

        TestExecutor.writeCustomReport(genericArchitecture, importedClasses, considerOnlyInArchitecture);
    }
}
