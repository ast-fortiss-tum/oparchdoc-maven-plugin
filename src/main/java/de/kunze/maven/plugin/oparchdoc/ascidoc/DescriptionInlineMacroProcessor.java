package de.kunze.maven.plugin.oparchdoc.ascidoc;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import de.kunze.maven.plugin.oparchdoc.annotations.ArchitectureDescription;
import org.asciidoctor.ast.ContentNode;
import org.asciidoctor.extension.InlineMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.HashMap;
import java.util.Map;

@Name("archclass")
public class DescriptionInlineMacroProcessor extends InlineMacroProcessor {

    private Map<String, String> classNameToDescription;

    public DescriptionInlineMacroProcessor(JavaClasses importerClasses) {
        classNameToDescription = new HashMap<>();

        importerClasses.that(CanBeAnnotated.Predicates.annotatedWith(ArchitectureDescription.class)).forEach(javaClass -> {
            classNameToDescription.put(javaClass.getSimpleName(), javaClass.getAnnotationOfType(ArchitectureDescription.class).description());
        });
    }

    @Override
    public Object process(
            ContentNode parent, String target, Map<String, Object> attributes) {

        String description = classNameToDescription.getOrDefault(target, "No description available");

        StringBuilder content = new StringBuilder()
                .append("<a ").append("title=").append("\"").append(description).append("\"").append(">")
                .append("<i>").append(target).append("</i>")
                .append("</a>");

        Map<String, Object> options = new HashMap<>();
        options.put("type", ":link");

        return createPhraseNode(parent, "anchor", content.toString(), attributes, options);
    }

}
