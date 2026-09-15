package cl.iplacex.examen.integration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de INTEGRACION (Actividad 2, §3.1 del informe): recorre la
 * aplicacion completa -- controlador, vista Thymeleaf y servicio -- a
 * traves de un navegador real, tal como lo haria una persona.
 *
 * Nombrada con sufijo "IT" (no "Test") para que Failsafe la reconozca y
 * Surefire la ignore (ver pom.xml).
 *
 * Nota: se usa @Value("${local.server.port}") en lugar de la anotacion
 * @LocalServerPort -- esta ultima cambio de paquete entre versiones de
 * Spring Boot (org.springframework.boot.web.server en unas,
 * org.springframework.boot.test.web.server en otras) y la propiedad
 * subyacente es identica y estable en todas las versiones 2.x/3.x.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginInventarioSeleniumIT {

    @Value("${local.server.port}")
    int puerto;

    private WebDriver driver;

    @BeforeEach
    void iniciarNavegador() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opciones = new ChromeOptions()
                .addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(opciones);
    }

    @Test
    void iniciaSesionYRegistraMovimientoDeStock() {
        driver.get("http://localhost:" + puerto + "/login");
        driver.findElement(By.id("usuario")).sendKeys("qa.tester");
        driver.findElement(By.id("clave")).sendKeys("Test1234");
        driver.findElement(By.id("btn-ingresar")).click();

        // El login exitoso implica dos saltos server-side (POST /login -> redirect
        // -> GET /inventario). Se espera explícitamente el cambio de URL en lugar
        // de asumir que el click() ya dejó al navegador en la página final --
        // esto evita falsos negativos por una carrera entre el redirect y la
        // siguiente aserción.
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.urlContains("/inventario"));

        // Nota: no se usa endsWith("/inventario"). En el primer redirect de una
        // sesion nueva, el contenedor (Tomcat) todavia no confirmo si el
        // navegador acepta cookies, asi que Spring codifica el id de sesion en
        // la propia URL via response.encodeRedirectURL() -- el resultado real
        // es ".../inventario;jsessionid=XXXX", no ".../inventario" a secas.
        // Esto es un comportamiento estandar de los contenedores de servlets,
        // no un error de la aplicacion, por lo que basta con confirmar que la
        // ruta esta presente en la URL.
        assertThat(driver.getCurrentUrl()).contains("/inventario");

        driver.findElement(By.id("sku")).sendKeys("SKU-001");
        driver.findElement(By.id("cantidad-salida")).sendKeys("2");
        driver.findElement(By.id("btn-registrar-salida")).click();

        // Mismo patron que el login: registrar la salida tambien implica un
        // POST seguido de un redirect a /inventario (misma URL de origen, asi
        // que no sirve esperar un cambio de URL aqui). Si se busca el elemento
        // y se lee su texto de inmediato, a veces la referencia queda "stale"
        // porque el navegador reemplaza el documento completo justo en medio
        // de esas dos llamadas. Se espera a que el elemento (re-localizado por
        // su id, no por la referencia vieja) efectivamente contenga el valor
        // esperado antes de leerlo.
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.id("stock-SKU-001"), "8"));

        String stockMostrado = driver.findElement(By.id("stock-SKU-001")).getText();
        assertThat(stockMostrado).isEqualTo("8");
    }

    @Test
    void noPermiteEntrarConCredencialesInvalidas() {
        driver.get("http://localhost:" + puerto + "/login");
        driver.findElement(By.id("usuario")).sendKeys("qa.tester");
        driver.findElement(By.id("clave")).sendKeys("clave-incorrecta");
        driver.findElement(By.id("btn-ingresar")).click();

        assertThat(driver.getCurrentUrl()).endsWith("/login");
        assertThat(driver.findElement(By.id("mensaje-error")).getText())
                .contains("incorrectos");
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }
}
