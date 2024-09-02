package de.kunze.maven.plugin.oparchdoc.GenericArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GenericArrow {

    private JavaClass origin;
    private JavaClass target;
    private ArrowTypes arrowType;
}
