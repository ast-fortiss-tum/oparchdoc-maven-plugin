package de.kunze.maven.plugin.oparchdoc.GenericArchitecture;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Component {

    // unique name
    private String name;

    // define what is part of component
    private List<String> packages;
    private List<String> classes;
    // all subcomponents are itself part of the component
    private List<Component> components;

    // dependencies with other components
    private List<String> onlyDependenciesTo;
    private List<String> onlyDependenciesFrom;

    private String reasonOnlyDependenciesTo;
    private String reasonOnlyDependenciesFrom;

    // access with other components
    private List<String> onlyAccessTo;
    private List<String> onlyAccessFrom;

    private String reasonOnlyAccessTo;
    private String reasonOnlyAccessFrom;

    // constructor calls
    private List<String> onlyCreatedIn;
    private List<String> onlyCreatedFrom;

    private String reasonOnlyCreatedIn;
    private String reasonOnlyCreatedFrom;

    // inheritance
    private List<String> assignableTo;
    private List<String> assignableFrom;

    private String reasonAssignableTo;
    private String reasonAssignableFrom;
}
