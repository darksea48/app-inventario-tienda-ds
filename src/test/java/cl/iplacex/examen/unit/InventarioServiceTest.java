package cl.iplacex.examen.unit;

import cl.iplacex.examen.inventario.InventarioService;
import cl.iplacex.examen.inventario.Producto;
import cl.iplacex.examen.inventario.ProductoNoEncontradoException;
import cl.iplacex.examen.inventario.StockInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias atomicas de InventarioService (Actividad 2, §3.1 del
 * informe). No arrancan el servidor web ni usan el navegador: cada prueba
 * reconstruye el servicio en @BeforeEach para no compartir estado.
 */
class InventarioServiceTest {

    private InventarioService servicio;

    @BeforeEach
    void setUp() {
        servicio = new InventarioService();
        servicio.registrarProducto(new Producto("SKU-001", "Taladro", 10));
    }

    @Test
    void noPermiteQueElStockQuedeNegativo() {
        assertThrows(StockInsuficienteException.class,
                () -> servicio.registrarSalida("SKU-001", 15));
        // El intento fallido no debe alterar el stock existente.
        assertThat(servicio.obtenerStock("SKU-001")).isEqualTo(10);
    }

    @Test
    void disparaAlertaCuandoElStockQuedaBajoElUmbral() {
        servicio.registrarSalida("SKU-001", 8); // quedan 2, umbral = 3
        assertThat(servicio.tieneAlertaDeStockBajo("SKU-001")).isTrue();
    }

    @Test
    void noHayAlertaSiElStockEstaSobreElUmbral() {
        servicio.registrarSalida("SKU-001", 2); // quedan 8
        assertThat(servicio.tieneAlertaDeStockBajo("SKU-001")).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"5,15", "0,10", "10,20"})
    void sumaCorrectamenteUnIngreso(int ingreso, int esperado) {
        servicio.registrarIngreso("SKU-001", ingreso);
        assertThat(servicio.obtenerStock("SKU-001")).isEqualTo(esperado);
    }

    @Test
    void rechazaCantidadesNoPositivas() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarIngreso("SKU-001", 0));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarSalida("SKU-001", -1));
    }

    @Test
    void lanzaExcepcionSiElProductoNoExiste() {
        assertThrows(ProductoNoEncontradoException.class, () -> servicio.obtenerStock("SKU-999"));
    }
}
