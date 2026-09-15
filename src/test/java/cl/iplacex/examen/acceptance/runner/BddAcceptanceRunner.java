package cl.iplacex.examen.acceptance.runner;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Suite BDD de aceptacion (Actividad 3, §4.2 del informe). Se invoca aparte
 * de Surefire/Failsafe, solo desde cd.yml, contra el color recien
 * desplegado -- no contra un servidor efimero de CI.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = Constants.PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/acceptance.html, json:target/cucumber-reports/acceptance.json")
public class BddAcceptanceRunner {
}
