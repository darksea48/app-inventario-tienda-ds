package cl.iplacex.examen.inventario;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Reglas de negocio del inventario. Deliberadamente simple (un mapa en
 * memoria, sin persistencia) porque el objetivo del examen es demostrar el
 * flujo de Git, CI y CD alrededor de un dominio, no la complejidad del
 * dominio en si.
 *
 * Reglas:
 *  - El stock nunca puede quedar negativo (StockInsuficienteException).
 *  - Bajo un umbral, el producto queda con "alerta de stock bajo".
 */
@Service
public class InventarioService {

    /** Bajo esta cantidad de unidades, se considera stock bajo. */
    static final int UMBRAL_ALERTA_STOCK_BAJO = 3;

    private final Map<String, Producto> productos = new LinkedHashMap<>();

    public void registrarProducto(Producto producto) {
        productos.put(producto.getSku(), producto);
    }

    public void registrarIngreso(String sku, int cantidad) {
        validarCantidadPositiva(cantidad);
        obtenerProducto(sku).sumar(cantidad);
    }

    public void registrarSalida(String sku, int cantidad) {
        validarCantidadPositiva(cantidad);
        Producto producto = obtenerProducto(sku);
        if (producto.getStock() - cantidad < 0) {
            throw new StockInsuficienteException(sku, producto.getStock(), cantidad);
        }
        producto.restar(cantidad);
    }

    public int obtenerStock(String sku) {
        return obtenerProducto(sku).getStock();
    }

    public boolean tieneAlertaDeStockBajo(String sku) {
        return obtenerProducto(sku).getStock() < UMBRAL_ALERTA_STOCK_BAJO;
    }

    public Collection<Producto> listarProductos() {
        return productos.values();
    }

    private Producto obtenerProducto(String sku) {
        Producto producto = productos.get(sku);
        if (producto == null) {
            throw new ProductoNoEncontradoException(sku);
        }
        return producto;
    }

    private void validarCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }
}
