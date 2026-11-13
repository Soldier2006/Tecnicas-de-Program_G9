package bodexa.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Unidad de persistencia de esta versión local y monousuario. */
public class EstadoBodega implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<Integer, Categoria> categorias = new LinkedHashMap<>();
    private final Map<Integer, Producto> productos = new LinkedHashMap<>();
    private final List<Venta> ventas = new ArrayList<>();
    private final List<AlertaStock> alertas = new ArrayList<>();
    public Map<Integer, Categoria> getCategorias() { return categorias; }
    public Map<Integer, Producto> getProductos() { return productos; }
    public List<Venta> getVentas() { return ventas; }
    public List<AlertaStock> getAlertas() { return alertas; }
}
