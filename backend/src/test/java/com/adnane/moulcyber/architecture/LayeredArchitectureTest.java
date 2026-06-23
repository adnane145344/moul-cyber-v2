package com.adnane.moulcyber.architecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.adnane.moulcyber",
        importOptions = DoNotIncludeTests.class)
class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayers = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "..api..",
                    "..application..",
                    "..configuration..",
                    "..infra..");

    @ArchTest
    static final ArchRule apiDoesNotDependOnPersistence = noClasses()
            .that()
            .resideInAPackage("..api..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infra.persistence..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnApi = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule restControllersStayInApi = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule jpaRepositoriesStayInPersistence = classes()
            .that()
            .areAssignableTo(JpaRepository.class)
            .should()
            .resideInAPackage("..infra.persistence..");

    @ArchTest
    static final ArchRule springConfigurationStaysInConfiguration = classes()
            .that()
            .areAnnotatedWith(Configuration.class)
            .should()
            .resideInAPackage("..configuration..");
}
