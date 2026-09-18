package com.hashtag.ngo.example.bank.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Verifie le sens de dependance entre les trois couches du projet :
 * api -> bean -> entity, sans remontee (une couche "basse" ne doit jamais
 * dependre d'une couche "haute").
 * <p>
 * ImportOption.DoNotIncludeTests exclut le code de test (y compris nos
 * propres classes de step definitions Cucumber, qui vivent elles aussi sous
 * com.hashtag.ngo.example.bank et referencent librement la couche api) :
 * seul le code de production est analyse. Sans ce filtre, "mayOnlyBeAccessedByLayers"
 * considere TOUT le code importe, y compris hors des couches definies, donc
 * le code de test serait a tort compte comme une violation de la couche Api.
 */
@AnalyzeClasses(packages = "com.hashtag.ngo.example.bank", importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule lesCouchesRespectentLeSensDeDependance = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Api").definedBy("..api..")
            .layer("Bean").definedBy("..bean..")
            .layer("Entity").definedBy("..entity..")
            // La couche api est le point d'entree de l'application (les
            // controleurs REST) : rien dans le code de production ne doit
            // dependre d'elle.
            .whereLayer("Api").mayNotBeAccessedByAnyLayer()
            // La couche bean (logique metier) ne doit etre utilisee que par
            // la couche api, jamais par la couche entity (qui lui est
            // inferieure) ni par elle-meme de maniere circulaire via une
            // autre couche.
            .whereLayer("Bean").mayOnlyBeAccessedByLayers("Api")
            // La couche entity (JPA) est la plus basse : elle peut etre
            // utilisee directement par bean ET par api (ex : les DTO/mappers
            // de la couche api referencent les enums/entites), mais ne doit
            // jamais remonter vers bean ou api.
            .whereLayer("Entity").mayOnlyBeAccessedByLayers("Bean", "Api");
}
