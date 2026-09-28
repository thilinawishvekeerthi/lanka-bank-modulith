package com.example.bank;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Spring Modulith guards the boundaries between modules. ArchUnit guards what happens
 * inside one: layering, and keeping a vendor SDK out of the domain.
 */
@AnalyzeClasses(packages = "com.example.bank", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureRulesTest {

    @ArchTest
    static final ArchRule noCyclesBetweenModules =
            slices().matching("com.example.bank.(*)..").should().beFreeOfCycles();

    @ArchTest
    static final ArchRule layering = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Web").definedBy("..web..")
            .layer("Api").definedBy(
                    "com.example.bank.customer",
                    "com.example.bank.payments",
                    "com.example.bank.notifications")
            .layer("Internal").definedBy("..internal..")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            .whereLayer("Api").mayOnlyBeAccessedByLayers("Web", "Internal")
            .whereLayer("Internal").mayOnlyBeAccessedByLayers("Api");

    /**
     * Anti-corruption layer. When the core banking vendor changes, the blast radius
     * must be one package instead of the whole codebase.
     */
    @ArchTest
    static final ArchRule antiCorruptionLayer =
            noClasses().that().resideOutsideOfPackage("com.example.bank.payments.acl..")
                    .should().dependOnClassesThat().resideInAPackage("com.acme.corebanking..")
                    .because("vendor models must be translated in the anti-corruption layer");
}
