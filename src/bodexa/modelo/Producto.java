package bodexa.modelo;

import java.io.Serializable;
import java.math.BigDecimal;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String nombre;
    private final BigDecimal precio;
    private int stock;
    private final Categoria categoria;

    public Producto(int id, String nombre, BigDecimal precio, int stock, Categoria categoria) {
        if (id <= 0 || nombre == null || nombre.isBlank() || nombre.trim().length() > 80)
            throw new IllegalArgumentException("Código positivo y nombre de 1 a 80 caracteres requeridos.");
        if (precio == null || precio.signum() <= 0 || precio.stripTrailingZeros().scale() > 2 || precio.compareTo(new BigDecimal("999999999.99")) > 0)
            throw new IllegalArgumentException("Precio positivo, máximo 999999999.99 y hasta dos decimales.");
        if (stock < 0 || categoria == null) throw new IllegalArgumentException("Stock no negativo y categoría obligatoria.");
        this.id = id; this.nombre = nombre.trim(); this.precio = precio.setScale(2);
        this.stock = stock; this.categoria = categoria;
    }
    public void descontar(int cantidad) {
        if (cantidad <= 0 || cantidad > stock) throw new IllegalArgumentException("Stock insuficiente o cantidad inválida para " + nombre);
        stock -= cantidad;
    }
    public void reponer(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La reposición debe ser positiva.");
        stock = Math.addExact(stock, cantidad);
    }
    public boolean tieneStockBajo() { return stock < 5; }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public Categoria getCategoria() { return categoria; }
    @Override public String toString() { return id + " - " + nombre + " (" + stock + " und.)"; }
}
