package bodexa.persistencia;

import bodexa.modelo.*;
import java.sql.*;
import java.util.*;

/** Persistencia transaccional para catálogo pequeño, con uso exclusivo de la aplicación. */
public class RepositorioSql implements Repositorio {
    private final Connection conexion;
    public RepositorioSql(String url) throws Exception {
        String user = System.getenv("BODEXA_DB_USER");
        conexion = user == null || user.isBlank() ? DriverManager.getConnection(url)
            : DriverManager.getConnection(url, user, System.getenv("BODEXA_DB_PASSWORD"));
        try {
            try (Statement s = conexion.createStatement()) {
                s.execute("SET QUOTED_IDENTIFIER ON; SET ANSI_NULLS ON; SET ANSI_PADDING ON; SET ANSI_WARNINGS ON; SET ARITHABORT ON; SET CONCAT_NULL_YIELDS_NULL ON; SET NUMERIC_ROUNDABORT OFF;");
            }
            String base = conexion.getCatalog();
            if (!"BodexaT3".equals(base) && !"BodexaT3_Pruebas".equals(base)) throw new SQLException("Utilice exclusivamente la base BodexaT3 o BodexaT3_Pruebas.");
            try (Statement s = conexion.createStatement(); ResultSet rs = s.executeQuery(
                "DECLARE @r int; EXEC @r=sys.sp_getapplock @Resource='BodexaUnicaInstancia', @LockMode='Exclusive', @LockOwner='Session', @LockTimeout=0; SELECT @r")) {
                if (!rs.next() || rs.getInt(1) < 0) throw new SQLException("Otra instancia está usando Bodexa.");
            }
        } catch (Exception e) { conexion.close(); throw e; }
    }
    @Override public EstadoBodega cargar() throws Exception {
        EstadoBodega e = new EstadoBodega();
        try (Statement s = conexion.createStatement()) {
            try (ResultSet r = s.executeQuery("SELECT * FROM Categoria ORDER BY id")) {
                while (r.next()) { Categoria c = new Categoria(r.getInt("id"), r.getString("nombre")); e.getCategorias().put(c.getId(), c); }
            }
            try (ResultSet r = s.executeQuery("SELECT * FROM Producto ORDER BY id")) {
                while (r.next()) { Producto p = new Producto(r.getInt("id"), r.getString("nombre"), r.getBigDecimal("precio"), r.getInt("stock"), e.getCategorias().get(r.getInt("categoria_id"))); e.getProductos().put(p.getId(), p); }
            }
            Map<Integer, List<DetalleVenta>> detalles = new LinkedHashMap<>();
            try (ResultSet r = s.executeQuery("SELECT * FROM DetalleVenta ORDER BY venta_id,producto_id")) {
                while (r.next()) {
                    Producto actual = e.getProductos().get(r.getInt("producto_id"));
                    Producto historico = new Producto(actual.getId(), r.getString("nombre_producto"), r.getBigDecimal("precio"), 0, actual.getCategoria());
                    detalles.computeIfAbsent(r.getInt("venta_id"), k -> new ArrayList<>()).add(new DetalleVenta(historico, r.getInt("cantidad")));
                }
            }
            try (ResultSet r = s.executeQuery("SELECT * FROM Venta ORDER BY id")) {
                while (r.next()) e.getVentas().add(new Venta(r.getInt("id"), r.getTimestamp("fecha").toLocalDateTime(), detalles.get(r.getInt("id"))));
            }
            try (ResultSet r = s.executeQuery("SELECT * FROM AlertaStock ORDER BY id")) {
                while (r.next()) e.getAlertas().add(new AlertaStock(r.getInt("id"), r.getInt("producto_id"), r.getTimestamp("fecha").toLocalDateTime(), r.getBoolean("atendida")));
            }
        }
        return e;
    }
    @Override public void guardar(EstadoBodega e) throws Exception {
        conexion.setAutoCommit(false);
        try {
            try (Statement s = conexion.createStatement()) {
                s.executeUpdate("DELETE FROM AlertaStock; DELETE FROM DetalleVenta; DELETE FROM Venta; DELETE FROM Producto; DELETE FROM Categoria;");
            }
            try (PreparedStatement p = conexion.prepareStatement("INSERT INTO Categoria VALUES(?,?)")) {
                for (Categoria c : e.getCategorias().values()) { p.setInt(1,c.getId()); p.setString(2,c.getNombre()); p.addBatch(); } p.executeBatch();
            }
            try (PreparedStatement p = conexion.prepareStatement("INSERT INTO Producto VALUES(?,?,?,?,?)")) {
                for (Producto v : e.getProductos().values()) { p.setInt(1,v.getId()); p.setString(2,v.getNombre()); p.setBigDecimal(3,v.getPrecio()); p.setInt(4,v.getStock()); p.setInt(5,v.getCategoria().getId()); p.addBatch(); } p.executeBatch();
            }
            try (PreparedStatement p = conexion.prepareStatement("INSERT INTO Venta VALUES(?,?)"); PreparedStatement d = conexion.prepareStatement("INSERT INTO DetalleVenta VALUES(?,?,?,?,?)")) {
                for (Venta v : e.getVentas()) {
                    p.setInt(1,v.getId()); p.setTimestamp(2,Timestamp.valueOf(v.getFecha())); p.addBatch();
                    for (DetalleVenta x : v.getDetalles()) { d.setInt(1,v.getId()); d.setInt(2,x.getProductoId()); d.setString(3,x.getNombreProducto()); d.setInt(4,x.getCantidad()); d.setBigDecimal(5,x.getPrecioUnitario()); d.addBatch(); }
                }
                p.executeBatch(); d.executeBatch();
            }
            try (PreparedStatement p = conexion.prepareStatement("INSERT INTO AlertaStock VALUES(?,?,?,?)")) {
                for (AlertaStock a : e.getAlertas()) { p.setInt(1,a.getId()); p.setInt(2,a.getProductoId()); p.setTimestamp(3,Timestamp.valueOf(a.getFecha())); p.setBoolean(4,a.isAtendida()); p.addBatch(); } p.executeBatch();
            }
            conexion.commit();
        } catch (Exception ex) { conexion.rollback(); throw ex; }
        finally { conexion.setAutoCommit(true); }
    }
    @Override public void close() throws SQLException { conexion.close(); }
}
