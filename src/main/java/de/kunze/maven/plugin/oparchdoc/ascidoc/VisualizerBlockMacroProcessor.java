package de.kunze.maven.plugin.oparchdoc.ascidoc;

import com.tngtech.archunit.core.domain.JavaClasses;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.executors.VisualizerExecutor;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;

import java.util.Map;

@Name("visualize")
public class VisualizerBlockMacroProcessor extends BlockMacroProcessor {

    private GenericArchitecture genericArchitecture;
    private JavaClasses importedClasses;

    public VisualizerBlockMacroProcessor(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        this.genericArchitecture = genericArchitecture;
        this.importedClasses = importedClasses;
    }

    @Override
    public Object process(StructuralNode parent, String target, Map<String, Object> attributes) {
        String fileName = "";

        if (!target.isEmpty()) {
            switch (target) {
                case "overview" -> fileName = VisualizerExecutor.generateGenericOverview(genericArchitecture, importedClasses);
                case "extended" -> fileName = VisualizerExecutor.generateGenericExtendedOverview(genericArchitecture, importedClasses);
                case "class" -> fileName = VisualizerExecutor.generateGenericClassOverview(genericArchitecture, importedClasses);
                case "full" -> fileName = VisualizerExecutor.generateGenericFullOverview(genericArchitecture, importedClasses);
            }
        } else {
            Object viewRaw = attributes.getOrDefault("view", "0");
            VisualizerExecutor.ViewType viewType = VisualizerExecutor.ViewType.OVERVIEW;
            if (viewRaw instanceof String) {
                switch ((String) viewRaw) {
                    //case 0 -> viewType = VisualizerExecutor.ViewType.OVERVIEW;
                    case "1" -> viewType = VisualizerExecutor.ViewType.COMPONENT_CLASS;
                    case "2" -> viewType = VisualizerExecutor.ViewType.FULL;
                }
            }

            Object idealRaw = attributes.getOrDefault("ideal", "1");
            boolean displayIdeal = true;
            if (idealRaw instanceof String) {
                switch ((String) idealRaw) {
                    case "0" -> displayIdeal = false;
                }
            }

            Object descriptionsRaw = attributes.getOrDefault("descriptions", "1");
            boolean displayDescriptions = true;
            if (descriptionsRaw instanceof String) {
                switch ((String) descriptionsRaw) {
                    case "0" -> displayDescriptions = false;
                }
            }

            Object violationsRaw = attributes.getOrDefault("violations", "0");
            VisualizerExecutor.VisibilityLevel visibilityLevel = VisualizerExecutor.VisibilityLevel.NONE;
            if (violationsRaw instanceof String) {
                switch ((String) violationsRaw) {
                    //case 0 -> viewType = VisualizerExecutor.ViewType.NONE;
                    case "1" -> visibilityLevel = VisualizerExecutor.VisibilityLevel.COMPONENT;
                    case "2" -> visibilityLevel = VisualizerExecutor.VisibilityLevel.CLASS_IF_IN_COMPONENTS;
                    case "3" -> visibilityLevel = VisualizerExecutor.VisibilityLevel.COMPONENT_OR_CLASS;
                    case "4" -> visibilityLevel = VisualizerExecutor.VisibilityLevel.CLASS;
                }
            }

            Object actualRaw = attributes.getOrDefault("actual", "0");
            VisualizerExecutor.VisibilityLevel displayActual = VisualizerExecutor.VisibilityLevel.NONE;
            if (actualRaw instanceof String) {
                switch ((String) actualRaw) {
                    //case 0 -> viewType = VisualizerExecutor.ViewType.NONE;
                    case "1" -> displayActual = VisualizerExecutor.VisibilityLevel.COMPONENT;
                    case "2" -> displayActual = VisualizerExecutor.VisibilityLevel.CLASS_IF_IN_COMPONENTS;
                    case "3" -> displayActual = VisualizerExecutor.VisibilityLevel.COMPONENT_OR_CLASS;
                    case "4" -> displayActual = VisualizerExecutor.VisibilityLevel.CLASS;
                }
            }

            Object legendRaw = attributes.getOrDefault("legend", "0");
            boolean displayLegend = false;
            if (legendRaw instanceof String) {
                switch ((String) legendRaw) {
                    case "1" -> displayLegend = true;
                }
            }

            Object leftrightRaw = attributes.getOrDefault("leftright", "1");
            boolean chosenLeftRight = true;
            if (leftrightRaw instanceof String) {
                switch ((String) leftrightRaw) {
                    case "0" -> chosenLeftRight = false;
                }
            }
            fileName = VisualizerExecutor.generateCustomView(genericArchitecture, importedClasses, viewType, displayIdeal, displayDescriptions, visibilityLevel, displayActual, displayLegend, chosenLeftRight, "");
        }

        String content = new StringBuilder()
                .append("<div class=\"imageblock\">")
                .append("<h2>" + genericArchitecture.getName() + "</h2>")
                .append("<div class=\"content\">")
                .append("<img src=\"" + fileName + ".svg\" alt=\"generatedFile\">")
                .append("<p>\"" + genericArchitecture.getDescription() + "\"</p>")
                .append("</div>")
                .append("</div>").toString();

        return createBlock(parent, "pass", content);
    }
}
