package cl.iplacex.examen.inventario;

/**
 * Representa un producto del inventario. El stock es mutable a proposito:
 * es InventarioService quien controla, de forma centralizada, las reglas
 * de negocio sobre como puede cambiar (ver InventarioService).
 */
public class Producto {

    private final String sku;
    private final String nombre;
    private int stock;

    public Producto(String sku, String nombre, int stockInicial) {
        if (stockInicial < 0) {
            throw new IllegalArgumentException("El stock inicial no puede ser negativo");
        }
        this.sku = sku;
        this.nombre = nombre;
        this.stock = stockInicial;
    }

    public String getSku() {
        return sku;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStock() {
        return stock;
    }

    void sumar(int cantidad) {
        this.stock += cantidad;
    }

    void restar(int cantidad) {
        this.stock -= cantidad;
    }
}
