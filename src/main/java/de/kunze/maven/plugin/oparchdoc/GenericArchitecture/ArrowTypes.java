package de.kunze.maven.plugin.oparchdoc.GenericArchitecture;

public enum ArrowTypes {

    ONLY_DEPENDENCIES_TO("-[thickness=2]down--|>"),
    ONLY_DEPENDENCIES_FROM(".[thickness=2]down..|>"),
    ONLY_ACCESS_TO("-[thickness=2]down-->"),
    ONLY_ACCESS_FROM(".[thickness=2]down..>"),
    ONLY_CREATE("-[thickness=2]down--*"),
    ONLY_CREATED_FROM(".[thickness=2]down..*"),
    ASSIGNABLE_TO("-[thickness=2]down--{"),
    ASSIGNABLE_FROM(".[thickness=2]down..{");

    public final String arrowAsString;

    ArrowTypes(String arrowAsString) {
        this.arrowAsString = arrowAsString;
    }

    public static String getViolationArrow(ArrowTypes arrowType) {
        return switch (arrowType) {
            case ONLY_DEPENDENCIES_TO -> "-[#red,thickness=2]-|>";
            case ONLY_DEPENDENCIES_FROM -> ".[#red,thickness=2].|>";
            case ONLY_ACCESS_TO -> "-[#red,thickness=2]->";
            case ONLY_ACCESS_FROM -> ".[#red,thickness=2].>";
            case ONLY_CREATE -> "-[#red,thickness=2]-*";
            case ONLY_CREATED_FROM -> ".[#red,thickness=2].*";
            case ASSIGNABLE_TO -> "-[#red,thickness=2]-{";
            case ASSIGNABLE_FROM -> ".[#red,thickness=2].{";
        };
    }

    public static String getActualArrow(ArrowTypes arrowType) {
        return switch (arrowType) {
            case ONLY_DEPENDENCIES_TO -> "-[#blue]-|>";
            case ONLY_DEPENDENCIES_FROM -> ".[#blue].|>";
            case ONLY_ACCESS_TO -> "-[#blue]->";
            case ONLY_ACCESS_FROM -> ".[#blue].>";
            case ONLY_CREATE -> "-[#blue]-*";
            case ONLY_CREATED_FROM -> ".[#blue].*";
            case ASSIGNABLE_TO -> "-[#blue]-{";
            case ASSIGNABLE_FROM -> ".[#blue].{";
        };
    }
}

