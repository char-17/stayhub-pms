package gr.ihu.pms;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTest {

    private static final String ROOT = "gr.ihu.pms";

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(ROOT);

    @Test
    void modulesHaveNoDependencyCycles() {
        slices().matching(ROOT + ".(*)..")
                .should().beFreeOfCycles()
                .check(CLASSES);
    }

    @Test
    void modulesDependOnlyInTheAllowedDirection() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("auth").definedBy(ROOT + ".auth..")
                .layer("rooms").definedBy(ROOT + ".rooms..")
                .layer("guests").definedBy(ROOT + ".guests..")
                .layer("folio").definedBy(ROOT + ".folio..")
                .layer("billing").definedBy(ROOT + ".billing..")
                .layer("housekeeping").definedBy(ROOT + ".housekeeping..")
                .layer("reservations").definedBy(ROOT + ".reservations..")
                .layer("reports").definedBy(ROOT + ".reports..")
                .layer("web").definedBy(ROOT + ".web..")
                .layer("config").definedBy(ROOT + ".config..")

                .whereLayer("auth").mayOnlyBeAccessedByLayers("config")
                .whereLayer("rooms").mayOnlyBeAccessedByLayers("housekeeping", "reservations", "reports", "web", "config")
                .whereLayer("guests").mayOnlyBeAccessedByLayers("reservations", "web", "config")
                .whereLayer("folio").mayOnlyBeAccessedByLayers("reservations", "config")
                .whereLayer("billing").mayOnlyBeAccessedByLayers("reservations", "reports", "web", "config")
                .whereLayer("housekeeping").mayOnlyBeAccessedByLayers("reservations")
                .whereLayer("reservations").mayOnlyBeAccessedByLayers("reports", "web", "config")
                .whereLayer("reports").mayNotBeAccessedByAnyLayer()
                .whereLayer("web").mayNotBeAccessedByAnyLayer()
                .whereLayer("config").mayNotBeAccessedByAnyLayer()
                .check(CLASSES);
    }

    @Test
    void repositoriesAreUsedOnlyInsideTheirOwnModule() {
        classes().that().haveSimpleNameEndingWith("Repository")
                .should(beAccessedOnlyFromTheirOwnModuleOrConfig())
                .check(CLASSES);
    }

    @Test
    void commonDependsOnNothingElse() {
        classes().that().resideInAPackage(ROOT + ".common..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(ROOT + ".common..", "java..")
                .check(CLASSES);
    }

    private static ArchCondition<JavaClass> beAccessedOnlyFromTheirOwnModuleOrConfig() {
        return new ArchCondition<>("be accessed only from their own module (or from config)") {
            @Override
            public void check(JavaClass repository, ConditionEvents events) {
                String owner = moduleOf(repository);
                for (Dependency dependency : repository.getDirectDependenciesToSelf()) {
                    String caller = moduleOf(dependency.getOriginClass());
                    if (!caller.equals(owner) && !caller.equals("config")) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    private static String moduleOf(JavaClass javaClass) {
        String rest = javaClass.getPackageName().substring(ROOT.length());
        if (rest.isEmpty()) {
            return "";
        }
        int next = rest.indexOf('.', 1);
        return next < 0 ? rest.substring(1) : rest.substring(1, next);
    }
}
