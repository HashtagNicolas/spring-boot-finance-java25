package com.hashtag.ngo.example.bank.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * Point d'entree JUnit Platform Suite : decouvre et execute tous les
 * scenarios Cucumber sous src/test/resources/features via le moteur
 * cucumber-junit-platform-engine. Le nom "RunCucumberTest" (suffixe "Test")
 * est deliberement choisi pour etre pris en compte par Surefire lors d'un
 * simple "mvn test", sans configuration additionnelle du plugin.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.hashtag.ngo.example.bank.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
public class RunCucumberTest {
}
