package de.kunze.maven.plugin.oparchdoc.GenericArchitecture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenericArchitecture {

    private String name;
    private String description;
    private List<Component> components;
}
