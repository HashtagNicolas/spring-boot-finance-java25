package com.hashtag.ngo.example.bank.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Conventions de nommage/rangement du projet :
 * - les controleurs REST s'appellent *Controller ;
 * - les interfaces de service vivent dans bean (jamais dans bean.impl) et
 *   s'appellent *Service ;
 * - leurs implementations vivent dans bean.impl et s'appellent *ServiceImpl ;
 * - plus generalement, toute classe *Impl (hors *MapperImpl, generees par
 *   MapStruct dans le meme package que leur interface) doit resider dans un
 *   package se terminant par .impl, et aucune interface ne doit y resider
 *   (un package .impl ne contient que des implementations concretes).
 * <p>
 * ImportOption.DoNotIncludeTests exclut le code de test (nos propres classes
 * de step definitions Cucumber/tests ArchUnit) de l'analyse : seul le code
 * de production est verifie.
 */
@AnalyzeClasses(packages = "com.hashtag.ngo.example.bank", importOptions = ImportOption.DoNotIncludeTests.class)
class NamingConventionsTest {

    @ArchTest
    static final ArchRule lesControleursSAppellentController = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .because("un @RestController doit etre immediatement identifiable par son nom");

    @ArchTest
    static final ArchRule lesInterfacesDeServiceSAppellentService = classes()
            .that().areInterfaces()
            .and().resideInAPackage("..bean")
            .should().haveSimpleNameEndingWith("Service")
            .because("le package bean (hors bean.impl) ne contient que des contrats de service");

    @ArchTest
    static final ArchRule lesImplementationsDeServiceSAppellentServiceImpl = classes()
            .that().resideInAPackage("..bean.impl")
            // Exclut les classes anonymes/synthetiques generees par le
            // compilateur (ex : TransactionServiceImpl$1 pour un lambda
            // passe a ScopedValue.Carrier#call, capture avec un type
            // generique qui empeche l'invokedynamic habituel) : elles
            // residents dans le meme package que leur classe englobante
            // mais n'ont evidemment pas a s'appeler *ServiceImpl.
            .and().haveNameNotMatching(".*\\$\\d+")
            .should().haveSimpleNameEndingWith("ServiceImpl")
            .because("le package bean.impl ne contient que des implementations de *Service");

    @ArchTest
    static final ArchRule lesClassesImplResidentDansUnPackageImpl = classes()
            .that().haveSimpleNameEndingWith("Impl")
            .and().haveSimpleNameNotEndingWith("MapperImpl")
            .should().resideInAPackage("..impl")
            .because("les *Impl (hors *MapperImpl generes par MapStruct, qui restent aux cotes de "
                    + "leur interface @Mapper) doivent etre rangees dans un package .impl");

    @ArchTest
    static final ArchRule aucuneInterfaceDansUnPackageImpl = noClasses()
            .that().areInterfaces()
            .should().resideInAPackage("..impl")
            .because("un package .impl ne doit contenir que des implementations concretes, jamais de contrats");
}
