package bodexa.persistencia;

import bodexa.modelo.EstadoBodega;

public interface Repositorio extends AutoCloseable {
    EstadoBodega cargar() throws Exception;
    void guardar(EstadoBodega estado) throws Exception;
    @Override default void close() throws Exception { }
}
