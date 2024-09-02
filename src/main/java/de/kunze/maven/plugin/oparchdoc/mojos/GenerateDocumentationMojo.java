package de.kunze.maven.plugin.oparchdoc.mojos;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.ascidoc.DescriptionInlineMacroProcessor;
import de.kunze.maven.plugin.oparchdoc.ascidoc.VisualizerBlockMacroProcessor;
import de.kunze.maven.plugin.oparchdoc.transformer.GenericArchitectureTransformer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;

import java.io.File;

@Mojo(name = "generatedoc", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateDocumentationMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true)
    private MavenProject project;

    @Parameter(property = "adocpath", defaultValue = "doc.adoc")
    private String pathToAdoc;

    @Parameter(property = "jsonpath", defaultValue = "generic.json")
    private String pathToGeneric;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        JavaClasses importedClasses = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES).importPath(project.getBasedir().getPath());
        GenericArchitecture genericArchitecture = GenericArchitectureTransformer.jsonToGeneric(pathToGeneric, importedClasses);

        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.javaExtensionRegistry()
                .inlineMacro(new DescriptionInlineMacroProcessor(importedClasses))
                .blockMacro(new VisualizerBlockMacroProcessor(genericArchitecture, importedClasses));

        Options emptyOptions = Options.builder().build();
        asciidoctor.convertFile(new File(pathToAdoc), emptyOptions);
        asciidoctor.shutdown();
    }
}

