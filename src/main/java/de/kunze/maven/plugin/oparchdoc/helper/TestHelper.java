package de.kunze.maven.plugin.oparchdoc.helper;

import com.tngtech.archunit.base.HasDescription;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.domain.properties.HasSourceCodeLocation;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.ViolationHandler;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.Component;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericRule;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class TestHelper {

    private final GenericArchitecture genericArchitecture;
    private final JavaClasses importedClasses;

    private final GenericArchitectureHelper genericArchitectureHelper;
    private Map<GenericRule, EvaluationResult> evaluationResults;
    private HashMap<Component, List<EvaluationResult>> evaluationResultsPerComponent;

    public TestHelper(GenericArchitecture genericArchitecture, JavaClasses importedClasses, GenericArchitectureHelper genericArchitectureHelper) {
        this.genericArchitecture = genericArchitecture;
        this.importedClasses = importedClasses;
        this.genericArchitectureHelper = genericArchitectureHelper;
    }

    public TestHelper(GenericArchitecture genericArchitecture, JavaClasses importedClasses) {
        this(genericArchitecture, importedClasses, new GenericArchitectureHelper(genericArchitecture, importedClasses));
    }

    public void writeCustomReport(boolean considerOnlyInArchitecture) {
        StringBuilder reportBuilder = new StringBuilder();

        getEvaluationResultForRule().forEach((genericRule, evaluationResult) -> {
            if (evaluationResult.hasViolation()) {
                reportBuilder.append("Because of: ")
                        .append(genericRule.getDescription())
                        .append(" component ")
                        .append(genericRule.getComponentNameOrigin())
                        .append(" should ")
                        .append(genericRule.getArrowType())
                        .append(" ")
                        .append(genericRule.getComponentNameTarget())
                        .append("\n");

                reportBuilder.append("Expressed through ArchRule: ").append(genericRule.getArchRule()).append("\n");
                reportBuilder.append("Violations have occurred in: \n");

                var wrapper = new Object() {
                    String currentViolationSource = "";
                };

                Set<JavaClass> classesToIncludeInTest;
                if (considerOnlyInArchitecture) {
                    classesToIncludeInTest = genericArchitectureHelper.getAllClassesInArchitecture();
                } else {
                    classesToIncludeInTest = genericArchitectureHelper.getAllClassesInImported();
                }

                evaluationResult.handleViolations(new ViolationHandler<Object>() {
                    @Override
                    public void handle(Collection<Object> collection, String s) {
                        collection.forEach(violation -> {
                            Pair<JavaClass, JavaClass> originTargetPair = transformViolationObject(violation);
                            if (originTargetPair.getLeft() != null && originTargetPair.getRight() != null) {
                                if (classesToIncludeInTest.contains(originTargetPair.getLeft())
                                        && classesToIncludeInTest.contains(originTargetPair.getRight())) {
                                    // append origin class only once
                                    if (!wrapper.currentViolationSource.equals(((HasSourceCodeLocation) violation).getSourceCodeLocation().getSourceClass().getFullName())) {
                                        reportBuilder.append(((HasSourceCodeLocation) violation).getSourceCodeLocation().getSourceClass().getSimpleName()).append("\n");
                                        wrapper.currentViolationSource = ((HasSourceCodeLocation) violation).getSourceCodeLocation().getSourceClass().getFullName();
                                    }
                                    reportBuilder.append("\t")
                                            .append(((HasDescription) violation).getDescription())
                                            .append("\n");
                                }
                            } else {
                                // fallback if origin target transformation fails
                                reportBuilder.append(((HasDescription) violation).getDescription())
                                        .append("\n");
                            }
                        });
                    }
                });
                reportBuilder.append("-".repeat(100)).append("\n");
            }
        });

        PrintWriter out = null;
        File newFile = new File("CustomReport.txt");
        try {
            out = new PrintWriter(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        out.write(reportBuilder.toString());
        out.close();
    }

    public Map<GenericRule, EvaluationResult> getEvaluationResultForRule() {
        if (evaluationResults == null) {
            evaluationResults = new HashMap<>();
            genericArchitectureHelper.getRulesForComponent().forEach((compName, rules) -> {
                rules.forEach(genericRule -> {
                    EvaluationResult evaluationResult = genericRule.getArchRule().allowEmptyShould(true).evaluate(importedClasses);
                    evaluationResults.put(genericRule, evaluationResult);
                });
            });
        }
        return evaluationResults;
    }

    public void doActionForViolation(TriConsumer<JavaClass, GenericRule, JavaClass> action) {
        genericArchitectureHelper.getRulesForComponent().forEach(((component, genericRules) -> {
            genericRules.forEach(genericRule -> {
                genericRule.getArchRule().allowEmptyShould(true).evaluate(importedClasses).handleViolations(new ViolationHandler<Object>() {
                    @Override
                    public void handle(Collection<Object> collection, String s) {
                        collection.forEach(violation -> {
                            Pair<JavaClass, JavaClass> originTargetPair = transformViolationObject(violation);
                            action.accept(originTargetPair.getLeft(), genericRule, originTargetPair.getRight());
                        });
                    }
                });
            });
        }));
    }

    // returns the origin and target of the violation object or null not convertible
    private Pair<JavaClass, JavaClass> transformViolationObject(Object violationObject) {
        if (violationObject instanceof JavaAccess) {
            return Pair.of(((JavaAccess<?>) violationObject).getOriginOwner(),
                    ((JavaAccess<?>) violationObject).getTargetOwner());
        }
        if (violationObject instanceof Dependency) {
            return Pair.of(((Dependency) violationObject).getOriginClass(), ((Dependency) violationObject).getTargetClass());
        }
        if (violationObject instanceof JavaClass) {
            return Pair.of(((JavaClass) violationObject), ((JavaClass) violationObject));
        }
        if (violationObject instanceof JavaMember) {
            return Pair.of(((JavaMember) violationObject).getOwner(), ((JavaMember) violationObject).getOwner());
        }
        return Pair.of(null, null);
    }

    public HashMap<Component, List<EvaluationResult>> getEvaluationResultsPerComponent() {
        if (evaluationResultsPerComponent == null) {
            evaluationResultsPerComponent = new HashMap<>();

            genericArchitectureHelper.getRulesForComponent().forEach((compName, rules) -> {
                rules.forEach(genericRule -> {
                    List<EvaluationResult> resultsSoFar = evaluationResultsPerComponent.getOrDefault(compName,
                            new ArrayList<>());
                    resultsSoFar.add(genericRule.getArchRule().evaluate(importedClasses));
                    evaluationResultsPerComponent.put(compName, resultsSoFar);
                });
            });

        }
        return evaluationResultsPerComponent;
    }
}
