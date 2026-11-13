package herramientas;

import bodexa.persistencia.RepositorioArchivo;
import bodexa.servicio.BodegaService;
import bodexa.vista.VentanaBodega;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Captura los componentes Swing reales con datos ficticios generados por el servicio. */
public class Evidencias {
    private static void captura(VentanaBodega v,String nombre)throws Exception {
        v.validate();BufferedImage imagen=new BufferedImage(v.getWidth(),v.getHeight(),BufferedImage.TYPE_INT_RGB);
        var g=imagen.createGraphics();v.paintAll(g);g.dispose();ImageIO.write(imagen,"png",Path.of("docs/evidencias/"+nombre+".png").toFile());
    }
    public static void main(String[] args)throws Exception {
        Files.createDirectories(Path.of("docs/evidencias"));
        try(RepositorioArchivo r=new RepositorioArchivo(Files.createTempDirectory("bodexa-evidencia-").resolve("datos.dat"))) {
            BodegaService s=new BodegaService(r);s.registrarCategoria("Abarrotes");s.registrarCategoria("Bebidas");
            s.guardarProducto(101,"Arroz 1 kg",new BigDecimal("4.50"),6,1,false);
            s.guardarProducto(102,"Leche evaporada",new BigDecimal("3.80"),12,1,false);
            s.guardarProducto(103,"Agua 625 ml",new BigDecimal("1.50"),20,2,false);
            final Exception[] error={null};
            SwingUtilities.invokeAndWait(()->{
                VentanaBodega v=null;
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    v=new VentanaBodega(s,"Archivo local · Datos ficticios de prueba");v.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);v.setAvisos(false);v.setVisible(true);
                    captura(v,"01_productos_iniciales");
                    s.registrarVenta(Map.of(101,2));v.refrescar();v.mostrarMensaje("Venta #1 registrada: 2 arroces x S/ 4.50 = S/ 9.00. Stock: 6 → 4.",false);captura(v,"02_venta_stock");
                    v.seleccionarPestana(2);captura(v,"03_alerta_pendiente");
                    try{s.registrarVenta(Map.of(101,5));throw new AssertionError("Debió rechazar venta");}catch(IllegalArgumentException ex){v.seleccionarPestana(1);v.mostrarMensaje(ex.getMessage(),true);captura(v,"04_stock_insuficiente");}
                    s.reponer(101,10);v.refrescar();v.seleccionarPestana(2);v.mostrarMensaje("Reposición de 10 unidades: stock 14. Alerta atendida.",false);captura(v,"05_reposicion");
                    v.seleccionarPestana(3);captura(v,"06_historial");
                    v.seleccionarPestana(0);
                    var campo=VentanaBodega.class.getDeclaredField("buscar");campo.setAccessible(true);((JTextField)campo.get(v)).setText("arroz");
                    v.refrescar();v.mostrarMensaje("Consulta por nombre: arroz. Solo se muestra la coincidencia.",false);captura(v,"09_busqueda");
                    s.guardarProducto(101,"Arroz extra 1 kg",new BigDecimal("4.80"),14,1,true);v.refrescar();
                    v.mostrarMensaje("Producto modificado: nuevo nombre y precio 4.80. Stock conservado: 14; venta histórica: 9.00.",false);captura(v,"10_modificacion");
                }catch(Exception ex){error[0]=ex;}finally{if(v!=null)v.dispose();}
            });
            if(error[0]!=null)throw error[0];
        }
        System.out.println("8 capturas de componentes reales generadas en docs/evidencias.");
    }
}
