package bodexa.modelo;

import java.io.Serializable;

public class Categoria implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String nombre;
    public Categoria(int id, String nombre) {
        if (id <= 0 || nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Categoría inválida.");
        this.id = id;
        this.nombre = nombre.trim();
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    @Override public String toString() { return nombre; }
}
