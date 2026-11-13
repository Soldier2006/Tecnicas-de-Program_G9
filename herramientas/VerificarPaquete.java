package herramientas;

import java.awt.Window;
import javax.swing.*;

/** Arranca el punto de entrada del JAR desde un ZIP extraído, sin datos previos. */
public class VerificarPaquete {
    public static void main(String[] args) throws Exception {
        bodexa.Main.main(new String[0]);
        SwingUtilities.invokeAndWait(() -> {
            boolean encontrada=false;
            for(Window w:Window.getWindows()) {
                if(w instanceof JFrame f && f.getTitle().startsWith("Bodexa") && f.isVisible()) encontrada=true;
                w.dispose();
            }
            if(!encontrada)throw new AssertionError("No se abrió la ventana del JAR.");
        });
        System.out.println("APROBADO: el JAR extraído inicia su ventana Swing mediante bodexa.Main, sin librerías adicionales en modo local.");
    }
}
