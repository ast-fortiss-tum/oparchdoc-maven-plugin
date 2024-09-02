package de.kunze.maven.plugin.oparchdoc.executors;

import com.tngtech.archunit.core.domain.JavaClasses;
import de.kunze.maven.plugin.oparchdoc.GenericArchitecture.GenericArchitecture;
import de.kunze.maven.plugin.oparchdoc.helper.TestHelper;

public class TestExecutor {

    public static void writeCustomReport(GenericArchitecture genericArchitecture, JavaClasses importedClasses, boolean considerOnlyInArchitecture) {
        new TestHelper(genericArchitecture, importedClasses).writeCustomReport(considerOnlyInArchitecture);
    }
}
