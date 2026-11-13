package bodexa.servicio;

import bodexa.modelo.*;
import bodexa.persistencia.Repositorio;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** Reglas de negocio compartidas por ambos repositorios. */
public class BodegaService {
    private final Repositorio repositorio;
    private EstadoBodega estado;
    public BodegaService(Repositorio repositorio) throws Exception {
        this.repositorio = repositorio; estado = repositorio.cargar();
    }
    private void modificar(Runnable operacion) throws Exception {
        try { operacion.run(); repositorio.guardar(estado); }
        catch (Exception e) { estado = repositorio.cargar(); throw e; }
    }
    public List<Categoria> categorias() { return List.copyOf(estado.getCategorias().values()); }
    public List<Producto> productos() { return List.copyOf(estado.getProductos().values()); }
    public List<Venta> ventas() { return List.copyOf(estado.getVentas()); }
    public List<AlertaStock> alertas() { return List.copyOf(estado.getAlertas()); }
    public Producto buscarProducto(int id) {
        Producto p = estado.getProductos().get(id);
        if (p == null) throw new IllegalArgumentException("No existe el producto " + id);
        return p;
    }
    public List<Producto> buscarProducto(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return productos().stream().filter(p -> p.getNombre().toLowerCase(Locale.ROOT).contains(filtro) || String.valueOf(p.getId()).equals(filtro)).toList();
    }
    public void registrarCategoria(String nombre) throws Exception {
        modificar(() -> {
            if (nombre == null || nombre.isBlank() || nombre.trim().length() > 50) throw new IllegalArgumentException("Nombre de categoría de 1 a 50 caracteres requerido.");
            if (categorias().stream().anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre.trim()))) throw new IllegalArgumentException("Categoría duplicada.");
            int id = estado.getCategorias().keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            estado.getCategorias().put(id, new Categoria(id, nombre));
        });
    }
    public void guardarProducto(int id, String nombre, BigDecimal precio, int stock, int categoriaId, boolean editar) throws Exception {
        modificar(() -> {
            Producto anterior = estado.getProductos().get(id);
            if (editar && anterior == null) throw new IllegalArgumentException("Seleccione un producto existente.");
            if (!editar && anterior != null) throw new IllegalArgumentException("Código de producto duplicado.");
            if (editar && stock != anterior.getStock()) throw new IllegalArgumentException("Para aumentar stock use Reponer; las salidas se registran mediante ventas.");
            Producto p = new Producto(id, nombre, precio, stock, estado.getCategorias().get(categoriaId));
            estado.getProductos().put(id, p); actualizarAlerta(p);
        });
    }
    public void eliminarProducto(int id) throws Exception {
        modificar(() -> {
            buscarProducto(id);
            if (ventas().stream().flatMap(v -> v.getDetalles().stream()).anyMatch(d -> d.getProductoId() == id))
                throw new IllegalArgumentException("El producto tiene ventas; conserve su historial.");
            estado.getAlertas().removeIf(a -> a.getProductoId() == id);
            estado.getProductos().remove(id);
        });
    }
    public void reponer(int id, int cantidad) throws Exception {
        modificar(() -> { Producto p = buscarProducto(id); p.reponer(cantidad); actualizarAlerta(p); });
    }
    public Venta registrarVenta(Map<Integer, Integer> cantidades) throws Exception {
        if (cantidades == null || cantidades.isEmpty()) throw new IllegalArgumentException("El carrito está vacío.");
        List<DetalleVenta> detalles = new ArrayList<>();
        // Validar TODO antes de descontar; los precios siempre salen del catálogo.
        for (var entrada : cantidades.entrySet()) {
            Producto p = buscarProducto(entrada.getKey());
            Integer cantidad = entrada.getValue();
            if (cantidad == null || cantidad <= 0 || cantidad > p.getStock()) throw new IllegalArgumentException("Stock insuficiente o cantidad inválida para " + p.getNombre());
            detalles.add(new DetalleVenta(p, cantidad));
        }
        int id = ventas().stream().mapToInt(Venta::getId).max().orElse(0) + 1;
        Venta venta = new Venta(id, LocalDateTime.now(), detalles);
        modificar(() -> {
            for (DetalleVenta d : detalles) { Producto p = buscarProducto(d.getProductoId()); p.descontar(d.getCantidad()); actualizarAlerta(p); }
            estado.getVentas().add(venta);
        });
        return venta;
    }
    private void actualizarAlerta(Producto p) {
        if (!p.tieneStockBajo()) {
            estado.getAlertas().stream().filter(a -> a.getProductoId() == p.getId()).forEach(AlertaStock::atender);
        } else if (estado.getAlertas().stream().noneMatch(a -> a.getProductoId() == p.getId() && !a.isAtendida())) {
            int id = estado.getAlertas().stream().mapToInt(AlertaStock::getId).max().orElse(0) + 1;
            estado.getAlertas().add(new AlertaStock(id, p.getId(), LocalDateTime.now(), false));
        }
    }
    public long unidadesVendidas(int id) {
        return ventas().stream().flatMap(v -> v.getDetalles().stream()).filter(d -> d.getProductoId() == id).mapToLong(DetalleVenta::getCantidad).sum();
    }
}
