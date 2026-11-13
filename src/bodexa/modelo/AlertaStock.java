package bodexa.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AlertaStock implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final int productoId;
    private final LocalDateTime fecha;
    private boolean atendida;
    public AlertaStock(int id, int productoId, LocalDateTime fecha, boolean atendida) {
        this.id = id; this.productoId = productoId; this.fecha = fecha; this.atendida = atendida;
    }
    public void atender() { atendida = true; }
    public int getId() { return id; }
    public int getProductoId() { return productoId; }
    public LocalDateTime getFecha() { return fecha; }
    public boolean isAtendida() { return atendida; }
}
