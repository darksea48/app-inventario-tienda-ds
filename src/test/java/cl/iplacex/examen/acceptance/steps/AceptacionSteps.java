package cl.iplacex.examen.acceptance.steps;

import io.cucumber.java.After;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step definitions de los acceptance tests de la Actividad 3 (§4.2 del
 * informe). Se ejecutan contra la URL del color recien desplegado,
 * inyectada por cd.yml en la propiedad de sistema "acceptance.target.url".
 */
public class AceptacionSteps {

    private final String urlBase = System.getProperty("acceptance.target.url", "http://localhost:8080");
    private WebDriver driver;
    private Response respuestaHealth;

    @Dado("que la aplicación candidata está desplegada en el color inactivo")
    public void laAplicacionCandidataEstaDesplegada() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver(new ChromeOptions()
                .addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage"));
    }

    @Cuando("un usuario de prueba inicia sesión con credenciales válidas")
    public void unUsuarioInicioSesion() {
        driver.get(urlBase + "/login");
        driver.findElement(By.id("usuario")).sendKeys("qa.tester");
        driver.findElement(By.id("clave")).sendKeys("Test1234");
        driver.findElement(By.id("btn-ingresar")).click();

        // El login exitoso implica un redirect server-side a /inventario (ver
        // LoginInventarioSeleniumIT, §3.1 del informe, donde se detectó esta
        // misma carrera). Se espera el cambio de URL antes de continuar con el
        // siguiente paso, que asume que ya estamos en /inventario.
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/inventario"));
    }

    @Cuando("registra una salida de stock de un producto existente")
    public void registraUnaSalidaDeStock() {
        driver.findElement(By.id("sku")).sendKeys("SKU-001");
        driver.findElement(By.id("cantidad-salida")).sendKeys("2");
        driver.findElement(By.id("btn-registrar-salida")).click();
    }

    @Entonces("el stock del producto se actualiza correctamente")
    public void elStockSeActualiza() {
        // Igual que en LoginInventarioSeleniumIT: registrar la salida tambien
        // dispara un redirect a /inventario, asi que se espera a que el
        // elemento (relocalizado por id) contenga el valor esperado antes de
        // leerlo, para no toparse con una referencia "stale" a mitad de la
        // recarga de la pagina.
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.id("stock-SKU-001"), "8"));
        assertThat(driver.findElement(By.id("stock-SKU-001")).getText()).isEqualTo("8");
    }

    @Cuando("se consulta el endpoint {string}")
    public void seConsultaElEndpoint(String ruta) {
        respuestaHealth = given().baseUri(urlBase).when().get(ruta);
    }

    @Entonces("la respuesta indica el estado {string}")
    public void laRespuestaIndicaElEstado(String estadoEsperado) {
        respuestaHealth.then().statusCode(200).body("status", org.hamcrest.Matchers.equalTo(estadoEsperado));
    }

    @After
    public void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }
}
