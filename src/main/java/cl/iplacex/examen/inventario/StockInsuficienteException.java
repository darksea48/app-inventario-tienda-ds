package cl.iplacex.examen.inventario;

/** Se lanza cuando una salida de stock dejaria el inventario en negativo. */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String sku, int stockActual, int cantidadSolicitada) {
        super("Stock insuficiente para %s: hay %d unidad(es), se solicitaron %d"
                .formatted(sku, stockActual, cantidadSolicitada));
    }
}
