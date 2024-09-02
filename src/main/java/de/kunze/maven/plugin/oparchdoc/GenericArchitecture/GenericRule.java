package de.kunze.maven.plugin.oparchdoc.GenericArchitecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class GenericRule {

    private DescribedPredicate<JavaClass> predicateOrigin;
    private DescribedPredicate<JavaClass> predicateTarget;
    private String componentNameOrigin;
    private List<String> componentNameTarget;
    private ArrowTypes arrowType;
    private ArchRule archRule;
    private String description;
}
