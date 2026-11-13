package bodexa.persistencia;

import bodexa.modelo.EstadoBodega;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;

/** Archivo local con reemplazo atómico y bloqueo para una sola instancia. */
public class RepositorioArchivo implements Repositorio {
    private final Path archivo;
    private final FileChannel canal;
    private final FileLock bloqueo;
    public RepositorioArchivo(Path archivo) throws IOException {
        this.archivo = archivo.toAbsolutePath();
        Files.createDirectories(this.archivo.getParent());
        canal = FileChannel.open(Path.of(this.archivo + ".lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock adquirido;
        try { adquirido = canal.tryLock(); }
        catch (RuntimeException e) { canal.close(); throw new IOException("La bodega ya está abierta.", e); }
        if (adquirido == null) { canal.close(); throw new IOException("La bodega ya está abierta."); }
        bloqueo = adquirido;
    }
    @Override public EstadoBodega cargar() throws Exception {
        if (!Files.exists(archivo)) return new EstadoBodega();
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(archivo))) {
            in.setObjectInputFilter(ObjectInputFilter.Config.createFilter("maxdepth=30;maxrefs=100000;bodexa.modelo.*;java.base/*;!*"));
            return (EstadoBodega) in.readObject();
        }
    }
    @Override public void guardar(EstadoBodega estado) throws Exception {
        Path temporal = Files.createTempFile(archivo.getParent(), "bodexa-", ".tmp");
        try {
            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temporal))) { out.writeObject(estado); }
            Files.move(temporal, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(temporal); }
    }
    @Override public void close() throws IOException { bloqueo.release(); canal.close(); }
}
