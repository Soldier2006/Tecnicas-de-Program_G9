package bodexa;

import bodexa.persistencia.*;
import bodexa.servicio.BodegaService;
import bodexa.vista.VentanaBodega;
import java.nio.file.Path;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                boolean sql = args.length > 0 && args[0].equals("--sql");
                String url = System.getenv("BODEXA_DB_URL");
                if (url == null) url = "jdbc:sqlserver://localhost:1433;databaseName=BodexaT3;integratedSecurity=true;encrypt=true;trustServerCertificate=true;loginTimeout=5";
                Repositorio repo = sql ? new RepositorioSql(url) : new RepositorioArchivo(Path.of("datos/bodexa.dat"));
                Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { repo.close(); } catch (Exception ignored) { } }));
                new VentanaBodega(new BodegaService(repo), sql ? "SQL Server" : "Archivo local").setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "No se pudo abrir Bodexa: " + e.getMessage() + "\nConsulte README.md.", "Inicio", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
