package cl.iplacex.examen;

import cl.iplacex.examen.inventario.InventarioService;
import cl.iplacex.examen.inventario.Producto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Examen Final ADP1323 - Automatizacion de Pruebas.
 *
 * Aplicacion de ejemplo (gestion de inventario de una tienda) usada para
 * demostrar, sobre un mismo proyecto: control de versiones con Trunk-Based
 * Development, un pipeline de CI con pruebas unitarias y de integracion, y
 * un pipeline de CD con despliegue Blue-Green.
 */
@SpringBootApplication
public class AppInventarioTiendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppInventarioTiendaApplication.class, args);
    }

    /**
     * Carga datos de demostracion al arrancar, tanto para uso local como
     * para las pruebas de integracion/aceptacion que asumen que "SKU-001"
     * ya existe con 10 unidades de stock.
     */
    @Bean
    CommandLineRunner cargarDatosDeDemostracion(InventarioService inventarioService) {
        return args -> inventarioService.registrarProducto(new Producto("SKU-001", "Taladro", 10));
    }
}
