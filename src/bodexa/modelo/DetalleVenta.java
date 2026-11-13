package bodexa.modelo;

import java.io.Serializable;
import java.math.BigDecimal;

public class DetalleVenta implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int productoId;
    private final String nombreProducto;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    public DetalleVenta(Producto producto, int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad mayor que cero requerida.");
        this.productoId = producto.getId(); this.nombreProducto = producto.getNombre();
        this.cantidad = cantidad; this.precioUnitario = producto.getPrecio();
    }
    public int getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }
}
