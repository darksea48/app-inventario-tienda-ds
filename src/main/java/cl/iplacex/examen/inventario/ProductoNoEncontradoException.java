package cl.iplacex.examen.inventario;

/** Se lanza cuando se opera sobre un SKU que no existe en el inventario. */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(String sku) {
        super("No existe un producto con SKU '%s'".formatted(sku));
    }
}
