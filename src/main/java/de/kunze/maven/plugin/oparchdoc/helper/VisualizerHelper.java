package de.kunze.maven.plugin.oparchdoc.helper;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.ArrowTypes;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArrow;
import de.kunze.maven.plugin.oparchdoc.executors.VisualizerExecutor;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class VisualizerHelper {

    private GenericArchitecture genericArchitecture;
    private JavaClasses importedClasses;

    private GenericArchitectureHelper genericArchitectureHelper;
    private StringBuilder umlStringBuilder;

    public VisualizerHelper(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        this.genericArchitecture = genericArchitecture;
        this.importedClasses = importedClasses;

        genericArchitectureHelper = new GenericArchitectureHelper(genericArchitecture, importedClasses);
        umlStringBuilder = new StringBuilder();
    }

    public String generateView(VisualizerExecutor.ViewType viewType, boolean displayIdeal, boolean displayDescriptions, VisualizerExecutor.VisibilityLevel visibilityLevel, VisualizerExecutor.VisibilityLevel displayActual, boolean displayLegend, boolean chosenLeftRight, String fileNamePrefix) {
        startUmlStringBuilder();

        if (chosenLeftRight) {
            setLeftToRightOrientation();
        }

        switch (viewType) {
            case OVERVIEW -> genericArchitecture.getComponents().forEach(this::addComponentsToUml);
            case COMPONENT_CLASS, FULL -> genericArchitecture.getComponents().forEach(this::addComponentClassesToUml);
        }

        if (displayIdeal) {
            addIdealComponentArrows(displayDescriptions);
        }

        switch (visibilityLevel) {
            case COMPONENT -> addViolationsComponentLevel();
            case CLASS_IF_IN_COMPONENTS -> addViolationsClassLevelOnlyIfPartOfComponents();
            case COMPONENT_OR_CLASS -> addViolationsComponentOrClassLevel();
            case CLASS -> addViolationsClassLevel();
        }

        switch (displayActual) {
            case COMPONENT -> addActualComponentLevel();
            case CLASS_IF_IN_COMPONENTS -> addActualClassLevelIfPartOfComponent();
            case COMPONENT_OR_CLASS -> addActualComponentOrClassLevel();
            case CLASS -> addActualClassLevel();
        }

        if (displayLegend) {
            addLegend();
        }


        umlStringBuilder.append("@enduml").append("\n");
        String filename = fileNamePrefix + "_visualization_" + viewType + "_ideal_" + displayIdeal + "_description_" + displayDescriptions + "_violationSeverity_" + visibilityLevel + "_actual_" + displayActual + "_legend_" + displayLegend;
        savePlantUmlToPng(umlStringBuilder.toString(), filename);
        return filename;
    }

    private void addIdealComponentArrows(boolean displayDescription) {
        HashSet<String> alreadyDrawn = new HashSet<>();
        genericArchitectureHelper.getRulesForComponent().forEach(((component, genericRules) -> {
            genericRules.forEach(genericRule -> {
                JavaClasses targets = importedClasses.that(genericRule.getPredicateTarget());

                targets.forEach(target -> {
                    if (!alreadyDrawn.contains(component.getName() + genericArchitectureHelper.getClassToComponent().get(target) + genericRule.getArrowType().arrowAsString)) {
                        if (displayDescription) {
                            drawUmlArrowFromOriginToTargetWithDescription(component.getName(), genericArchitectureHelper.getClassToComponent().get(target), genericRule.getArrowType().arrowAsString, genericRule.getDescription());
                        } else {
                            drawUmlArrowFromOriginToTarget(component.getName(), genericArchitectureHelper.getClassToComponent().get(target), genericRule.getArrowType().arrowAsString);
                        }
                        alreadyDrawn.add(component.getName() + genericArchitectureHelper.getClassToComponent().get(target) + genericRule.getArrowType().arrowAsString);
                    }
                });
            });
        }));
    }

//    private void addIdealArrows(Set<GenericRule> genericRuleSet) {
//        genericRuleSet.forEach(genericRule -> {
//            if (genericRule.getPredicateOrigin() == null || genericRule.getPredicateTarget() == null) {
//                // occurs with invalid architecture definition
//                return;
//            }
//            JavaClasses origins = importedClasses.that(genericRule.getPredicateOrigin());
//            JavaClasses targets = importedClasses.that(genericRule.getPredicateTarget());
//
//            origins.forEach(origin -> {
//                targets.forEach(target -> {
//                    if (!origin.getSimpleName().isEmpty() && !target.getSimpleName().isEmpty() && !origin.getSimpleName()
//                            .equals(target.getSimpleName())) {
//                        umlStringBuilder.append(origin.getSimpleName()).append(" ").append(genericRule.getArrowType())
//                                .append(" ")
//                                .append(target.getSimpleName()).append("\n");
//                    }
//                });
//            });
//        });
//    }

    private void addViolationsComponentLevel() {
        TestHelper testHelper = new TestHelper(genericArchitecture, importedClasses, genericArchitectureHelper);

        Set<String> alreadyDrawn = new HashSet<>();
        testHelper.doActionForViolation((javaClassOrigin, genericRule, javaClassTarget) -> {
            String originName = genericArchitectureHelper.getClassToComponent().get(javaClassOrigin);
            String targetName = genericArchitectureHelper.getClassToComponent().get(javaClassTarget);
            if (originName == null || targetName == null) {
                return;
            }

            String storeForAlreadyDrawn = originName + targetName + ArrowTypes.getViolationArrow(genericRule.getArrowType());
            // exclude arrows to self caused in certain cases by access to Java base classes
            if (!alreadyDrawn.contains(storeForAlreadyDrawn)) {
                drawUmlArrowFromOriginToTarget(originName, targetName, ArrowTypes.getViolationArrow(genericRule.getArrowType()));
                alreadyDrawn.add(storeForAlreadyDrawn);
            }
        });
    }

    private void addViolationsClassLevelOnlyIfPartOfComponents() {
        TestHelper testHelper = new TestHelper(genericArchitecture, importedClasses, genericArchitectureHelper);

        Set<String> alreadyDrawn = new HashSet<>();
        testHelper.doActionForViolation((javaClassOrigin, genericRule, javaClassTarget) -> {
            String originName = genericArchitectureHelper.getClassToComponent().get(javaClassOrigin);
            String targetName = genericArchitectureHelper.getClassToComponent().get(javaClassTarget);


            if (originName == null || targetName == null) {
                return;
            }

            originName = normalizedSimpleClassName(javaClassOrigin);
            targetName = normalizedSimpleClassName(javaClassTarget);

            String storeForAlreadyDrawn = originName + targetName + ArrowTypes.getViolationArrow(genericRule.getArrowType());
            // exclude arrows to self caused in certain cases by access to Java base classes
            if (!alreadyDrawn.contains(storeForAlreadyDrawn)) {
                drawUmlArrowFromOriginToTarget(originName, targetName, ArrowTypes.getViolationArrow(genericRule.getArrowType()));
                alreadyDrawn.add(storeForAlreadyDrawn);
            }
        });
    }

    private void addViolationsComponentOrClassLevel() {
        TestHelper testHelper = new TestHelper(genericArchitecture, importedClasses, genericArchitectureHelper);

        Set<String> alreadyDrawn = new HashSet<>();
        testHelper.doActionForViolation((javaClassOrigin, genericRule, javaClassTarget) -> {
            if (!genericArchitectureHelper.getAllClassesInImported().contains(javaClassOrigin)
                    || !genericArchitectureHelper.getAllClassesInImported().contains(javaClassTarget)) {
                return;
            }

            String originName = genericArchitectureHelper.getClassToComponent().get(javaClassOrigin);
            String targetName = genericArchitectureHelper.getClassToComponent().get(javaClassTarget);

            if (originName == null) {
                originName = normalizedSimpleClassName(javaClassOrigin);
            }

            if (targetName == null) {
                targetName = normalizedSimpleClassName(javaClassTarget);
            }

            String storeForAlreadyDrawn = originName + targetName + ArrowTypes.getViolationArrow(genericRule.getArrowType());
            // exclude arrows to self caused in certain cases by access to Java base classes
            if (!alreadyDrawn.contains(storeForAlreadyDrawn)) {
                drawUmlArrowFromOriginToTarget(originName, targetName, ArrowTypes.getViolationArrow(genericRule.getArrowType()));
                alreadyDrawn.add(storeForAlreadyDrawn);
            }
        });
    }

    private void addViolationsClassLevel() {
        TestHelper testHelper = new TestHelper(genericArchitecture, importedClasses, genericArchitectureHelper);

        Set<String> alreadyDrawn = new HashSet<>();
        testHelper.doActionForViolation((javaClassOrigin, genericRule, javaClassTarget) -> {
            if (!genericArchitectureHelper.getAllClassesInImported().contains(javaClassOrigin)
                    || !genericArchitectureHelper.getAllClassesInImported().contains(javaClassTarget)) {
                return;
            }
            String originName = normalizedSimpleClassName(javaClassOrigin);
            String targetName = normalizedSimpleClassName(javaClassTarget);
            String storeForAlreadyDrawn = originName + targetName + ArrowTypes.getViolationArrow(genericRule.getArrowType());
            // exclude arrows to self caused in certain cases by access to Java base classes
            if (!alreadyDrawn.contains(storeForAlreadyDrawn)) {
                drawUmlArrowFromOriginToTarget(originName, targetName, ArrowTypes.getViolationArrow(genericRule.getArrowType()));
                alreadyDrawn.add(storeForAlreadyDrawn);
            }
        });
    }

    private void addComponentsToUml(Component component) {
        umlStringBuilder.append("package \"").append(component.getName()).append("\" { \n");

        if (component.getComponents() != null) {
            component.getComponents().forEach(this::addComponentsToUml);
        }

        umlStringBuilder.append("}\n");
    }

    private void addComponentClassesToUml(Component component) {
        umlStringBuilder.append("package \"").append(component.getName()).append("\" { \n");

        genericArchitectureHelper.getJavaClassesMinusSubcomponentForComponent().get(component).forEach(javaClass -> {
            if (!javaClass.getSimpleName().isEmpty()) {
                umlStringBuilder.append("class ").append(javaClass.getSimpleName()).append("\n");
            }
        });

        if (component.getComponents() != null) {
            component.getComponents().forEach(this::addComponentClassesToUml);
        }

        umlStringBuilder.append("}\n");
    }

    private void addActualComponentLevel() {
        Set<GenericArrow> arrows = genericArchitectureHelper.getAllActualArrows();
        Set<String> alreadyDrawn = new HashSet<>();

        arrows.forEach(genericArrow -> {
            String origin = genericArchitectureHelper.getClassToComponent().get(genericArrow.getOrigin());
            String target = genericArchitectureHelper.getClassToComponent().get(genericArrow.getTarget());

            if (origin == null || target == null) {
                return;
            }

            if (!origin.equals(target)) {
                String arrow = ArrowTypes.getActualArrow(genericArrow.getArrowType());
                if (!alreadyDrawn.contains(origin + target + arrow)) {
                    drawUmlArrowFromOriginToTarget(origin, target, arrow);
                    alreadyDrawn.add(origin + target + arrow);
                }
            }
        });
    }

    private void addActualClassLevelIfPartOfComponent() {
        Set<GenericArrow> arrows = genericArchitectureHelper.getAllActualArrows();
        Set<String> alreadyDrawn = new HashSet<>();

        arrows.forEach(genericArrow -> {
            String originName = genericArchitectureHelper.getClassToComponent().get(genericArrow.getOrigin());
            String targetName = genericArchitectureHelper.getClassToComponent().get(genericArrow.getTarget());

            if (originName == null || targetName == null) {
                return;
            }

            originName = normalizedSimpleClassName(genericArrow.getOrigin());
            targetName = normalizedSimpleClassName(genericArrow.getTarget());

            if (!originName.equals(targetName)) {
                String arrow = ArrowTypes.getActualArrow(genericArrow.getArrowType());
                if (!alreadyDrawn.contains(originName + targetName + arrow)) {
                    drawUmlArrowFromOriginToTarget(originName, targetName, arrow);
                    alreadyDrawn.add(originName + targetName + arrow);
                }
            }
        });
    }

    private void addActualComponentOrClassLevel() {
        Set<GenericArrow> arrows = genericArchitectureHelper.getAllActualArrows();
        Set<String> alreadyDrawn = new HashSet<>();

        arrows.forEach(genericArrow -> {
            if (!genericArchitectureHelper.getAllClassesInImported().contains(genericArrow.getOrigin())
                    || !genericArchitectureHelper.getAllClassesInImported().contains(genericArrow.getTarget())) {
                return;
            }

            String originName = genericArchitectureHelper.getClassToComponent().get(genericArrow.getOrigin());
            String targetName = genericArchitectureHelper.getClassToComponent().get(genericArrow.getTarget());

            if (originName == null) {
                originName = normalizedSimpleClassName(genericArrow.getOrigin());
            }

            if (targetName == null) {
                targetName = normalizedSimpleClassName(genericArrow.getTarget());
            }

            if (!originName.equals(targetName)) {
                String arrow = ArrowTypes.getActualArrow(genericArrow.getArrowType());
                if (!alreadyDrawn.contains(originName + targetName + arrow)) {
                    drawUmlArrowFromOriginToTarget(originName, targetName, arrow);
                    alreadyDrawn.add(originName + targetName + arrow);
                }
            }
        });
    }

    private void addActualClassLevel() {
        Set<GenericArrow> arrows = genericArchitectureHelper.getAllActualArrows();
        Set<String> alreadyDrawn = new HashSet<>();

        arrows.forEach(genericArrow -> {
            if (!genericArchitectureHelper.getAllClassesInImported().contains(genericArrow.getOrigin())
                    || !genericArchitectureHelper.getAllClassesInImported().contains(genericArrow.getTarget())) {
                return;
            }

            String origin = normalizedSimpleClassName(genericArrow.getOrigin());
            String target = normalizedSimpleClassName(genericArrow.getTarget());


            if (!origin.equals(target)) {
                String arrow = ArrowTypes.getActualArrow(genericArrow.getArrowType());
                if (!alreadyDrawn.contains(origin + target + arrow)) {

                    if (!genericArchitectureHelper.getClassToComponent().containsKey(genericArrow.getOrigin())) {
                        drawUmlArrowFromOriginToTarget(origin, target, arrow.replaceAll("]", "]u"));
                        alreadyDrawn.add(origin + target + arrow);
                        return;
                    }

                    if (!genericArchitectureHelper.getClassToComponent().containsKey(genericArrow.getTarget())) {
                        drawUmlArrowFromOriginToTarget(origin, target, arrow.replaceAll("]", "]d"));
                        alreadyDrawn.add(origin + target + arrow);
                        return;
                    }


                    drawUmlArrowFromOriginToTarget(origin, target, arrow);
                    alreadyDrawn.add(origin + target + arrow);
                }
            }
        });
    }

    // simple class names have special cases for enums subclasses etc. and need to be normalized
    private String normalizedSimpleClassName(JavaClass classIn) {
        String simpleClassName = classIn.getSimpleName();
        if (simpleClassName.isEmpty()) {
            String[] className = classIn.getFullName().split("\\.");
            simpleClassName = className[className.length - 1];
            simpleClassName = simpleClassName.substring(0, simpleClassName.indexOf("$"));
        }
        return simpleClassName.replaceAll("\\[", "").replaceAll("]", "");
    }

    private void drawUmlArrowFromOriginToTarget(String origin, String target, String arrowType) {
        umlStringBuilder
                .append(origin)
                .append(" ")
                .append(arrowType)
                .append(" ")
                .append(target)
                .append("\n");
    }

    private void drawUmlArrowFromOriginToTargetWithDescription(String origin, String target, String arrowType, String description) {
        if (description == null || description.isEmpty()) {
            drawUmlArrowFromOriginToTarget(origin, target, arrowType);
        } else {
            umlStringBuilder
                    .append(origin)
                    .append(" ")
                    .append(arrowType)
                    .append(" ")
                    .append(target)
                    .append("\n")
                    .append("note on link #white\n")
                    .append(description).append("\n")
                    .append("end note\n");
        }
    }

    private void savePlantUmlToPng(String plantUmlAsString, String filename) {
        try {
            Files.writeString(Path.of(filename + ".txt"), plantUmlAsString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStream png = null;
        File newFile = new File(filename + ".svg");
        try {
            png = new FileOutputStream(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        SourceStringReader reader = new SourceStringReader(plantUmlAsString);
        try {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            // Write the first image to "os"
            String desc = reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
            os.writeTo(png);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startUmlStringBuilder() {
        umlStringBuilder.setLength(0);

        umlStringBuilder.append("@startuml").append("\n");
        umlStringBuilder.append("skinparam style strictuml\n");

        umlStringBuilder.append("skinparam linetype ortho\n");
        umlStringBuilder.append("skinparam packageStyle rectangle\n");
        umlStringBuilder.append("skinparam packageBorderThickness 5\n");
    }

    private void setLeftToRightOrientation() {
        umlStringBuilder.append("left to right direction\n");
    }

    private void addLegend() {
        umlStringBuilder.append("rectangle \"Relation types\" { \n");
        umlStringBuilder.append("""
                skinparam style strictuml
                scale 0.5
                hide empty members
                skinparam Object {
                    BorderColor transparent
                    BackgroundColor transparent
                    FontColor transparent
                }
                object " " as A
                object " " as B
                Object " " as A1
                Object " " as B1
                Object " " as A2
                Object " " as B2
                Object " " as A3
                Object " " as B3
                Object " " as A4
                Object " " as B4
                Object " " as A5
                Object " " as B5
                Object " " as A6
                Object " " as B6
                Object " " as A7
                Object " " as B7
                A %s B : DependenciesTo
                A1 %s B1 : DependenciesFrom
                A2 %s B2 : AccessTo
                A3 %s B3 : AccessFrom
                A4 %s B4 : CreateIn
                A5 %s B5 : CreateFrom
                A6 %s B6 : AssignableTo
                A7 %s B7 : AssignableFrom


                skinparam legendBackgroundColor #FFFFFF
                skinparam legendBorderColor #FFFFFF
                skinparam legendEntrySeparator #FFFFFF


                legend right
                '   the <#FFFFFF,#FFFFFF> sets the background color of the legend to white
                    <#FFFFFF,#FFFFFF>|<#black>| Defined relations|
                    ' the space between the | and <#blue> is important to make the color column wider
                    |<#blue>     | Actual relations|
                    |<#red>| Violating relations|
                endlegend


                """.formatted(
                ArrowTypes.ONLY_DEPENDENCIES_TO.arrowAsString,
                ArrowTypes.ONLY_DEPENDENCIES_FROM.arrowAsString,
                ArrowTypes.ONLY_ACCESS_TO.arrowAsString,
                ArrowTypes.ONLY_ACCESS_FROM.arrowAsString,
                ArrowTypes.ONLY_CREATE.arrowAsString,
                ArrowTypes.ONLY_CREATED_FROM.arrowAsString,
                ArrowTypes.ASSIGNABLE_TO.arrowAsString,
                ArrowTypes.ASSIGNABLE_FROM.arrowAsString));
        umlStringBuilder.append("\n}\n");
    }
}

