package bodexa.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Venta implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final LocalDateTime fecha;
    private final List<DetalleVenta> detalles;
    public Venta(int id, LocalDateTime fecha, List<DetalleVenta> detalles) {
        if (id <= 0 || fecha == null || detalles == null || detalles.isEmpty()) throw new IllegalArgumentException("La venta debe contener productos.");
        this.id = id; this.fecha = fecha; this.detalles = List.copyOf(detalles);
    }
    public int getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public BigDecimal getTotal() { return detalles.stream().map(DetalleVenta::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
}
