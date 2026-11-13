package herramientas;

import bodexa.modelo.*;
import bodexa.persistencia.*;
import bodexa.servicio.BodegaService;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

public class Pruebas {
    private static final List<String> resultados = new ArrayList<>();
    private static int numero;
    private interface Accion { void run() throws Exception; }
    private static void verificar(boolean condicion) { if(!condicion)throw new AssertionError("Resultado distinto del esperado"); }
    private static void rechaza(Accion a) throws Exception {
        try { a.run(); } catch(IllegalArgumentException | java.io.IOException e) { return; }
        throw new AssertionError("Se esperaba rechazo");
    }
    private static void prueba(String caso,String entrada,String esperado,Accion a) throws Exception {
        numero++;
        try { a.run(); resultados.add("| P"+String.format("%02d",numero)+" | "+caso+" | "+entrada+" | "+esperado+" | Coincide con lo esperado | Aprobado |"); }
        catch(Throwable ex) { resultados.add("| P"+numero+" | "+caso+" | "+entrada+" | "+esperado+" | "+ex+" | No aprobado |"); guardar(); throw ex; }
    }
    private static void guardar() throws Exception {
        Files.createDirectories(Path.of("docs/evidencias"));
        String cabecera="# Registro real de pruebas\n\nEjecución: "+java.time.LocalDateTime.now()+"\n\nEntorno: Java "+System.getProperty("java.version")+", "+System.getProperty("os.name")+". Repositorio de archivo temporal, independiente de los datos del usuario.\n\n| Código | Funcionalidad | Datos o condición | Resultado esperado | Resultado obtenido | Estado |\n|---|---|---|---|---|---|\n";
        Files.writeString(Path.of("docs/PRUEBAS.md"),cabecera+String.join("\n",resultados)+"\n\nLa integración JDBC con SQL Server requiere TCP/IP: no evaluada en estas pruebas.\n");
    }
    public static void main(String[] args) throws Exception {
        Path ruta=Files.createTempDirectory("bodexa-pruebas-").resolve("estado.dat");
        try(RepositorioArchivo repo=new RepositorioArchivo(ruta)) {
            BodegaService s=new BodegaService(repo);
            prueba("Categoría","Abarrotes","Una categoría persistida",()->{s.registrarCategoria("Abarrotes");verificar(s.categorias().size()==1);});
            prueba("Categoría duplicada","abarrotes","Rechazo sin duplicar",()->{rechaza(()->s.registrarCategoria("abarrotes"));verificar(s.categorias().size()==1);});
            prueba("Producto válido","Arroz, 4.50, stock 6","Producto guardado",()->{s.guardarProducto(1,"Arroz",new BigDecimal("4.50"),6,1,false);verificar(s.buscarProducto(1).getStock()==6);});
            prueba("Código duplicado","Código 1","Rechazo, stock sigue en 6",()->{rechaza(()->s.guardarProducto(1,"Otro",BigDecimal.ONE,1,1,false));verificar(s.buscarProducto(1).getStock()==6);});
            prueba("Campo vacío","Nombre en blanco","Rechazo",()->rechaza(()->s.guardarProducto(2," ",BigDecimal.ONE,5,1,false)));
            prueba("Precio incorrecto","Precio -1","Rechazo",()->rechaza(()->s.guardarProducto(2,"Leche",new BigDecimal("-1"),5,1,false)));
            prueba("Stock negativo","Stock -1","Rechazo",()->rechaza(()->s.guardarProducto(2,"Leche",BigDecimal.ONE,-1,1,false)));
            prueba("Búsqueda sin resultados","Inexistente","Lista vacía",()->verificar(s.buscarProducto("inexistente").isEmpty()));
            prueba("Búsqueda por nombre","ARR","Encuentra Arroz",()->verificar(s.buscarProducto("ARR").size()==1));
            prueba("Modificar","Arroz extra","Nombre cambia y stock se conserva",()->{s.guardarProducto(1,"Arroz extra",new BigDecimal("4.50"),6,1,true);verificar(s.buscarProducto(1).getNombre().equals("Arroz extra"));});
            prueba("Venta normal","2 unidades a 4.50","Stock 4; total 9.00; detalle y alerta",()->{Venta v=s.registrarVenta(Map.of(1,2));verificar(v.getTotal().compareTo(new BigDecimal("9.00"))==0&&s.buscarProducto(1).getStock()==4&&s.alertas().size()==1&&s.unidadesVendidas(1)==2);});
            prueba("Venta inválida","5 unidades con stock 4","Rechazo; no aumenta historial ni baja stock",()->{rechaza(()->s.registrarVenta(Map.of(1,5)));verificar(s.ventas().size()==1&&s.buscarProducto(1).getStock()==4);});
            prueba("Cantidad cero","0 unidades","Rechazo",()->rechaza(()->s.registrarVenta(Map.of(1,0))));
            prueba("Carrito vacío","Sin detalles","Rechazo",()->rechaza(()->s.registrarVenta(Map.of())));
            prueba("Alerta no duplicada","Vender 1 con stock 4","Stock 3 y una alerta pendiente",()->{s.registrarVenta(Map.of(1,1));verificar(s.buscarProducto(1).getStock()==3&&s.alertas().size()==1);});
            prueba("Reposición parcial","Agregar 1 a stock 3","Stock 4; alerta sigue pendiente",()->{s.reponer(1,1);verificar(s.buscarProducto(1).getStock()==4&&!s.alertas().get(0).isAtendida());});
            prueba("Límite de alerta","Agregar 1 a stock 4","Stock 5; alerta atendida",()->{s.reponer(1,1);verificar(s.buscarProducto(1).getStock()==5&&s.alertas().get(0).isAtendida());});
            prueba("Nuevo agotamiento","Vender 1 desde stock 5","Nueva alerta pendiente",()->{s.registrarVenta(Map.of(1,1));verificar(s.alertas().size()==2&&!s.alertas().get(1).isAtendida());});
            prueba("Eliminar con historial","Producto 1 vendido","Rechazo",()->rechaza(()->s.eliminarProducto(1)));
            prueba("Eliminar sin ventas","Crear y eliminar producto 2","Producto eliminado",()->{s.guardarProducto(2,"Temporal",BigDecimal.ONE,10,1,false);s.eliminarProducto(2);verificar(s.productos().size()==1);});
            prueba("Persistencia","Recargar desde disco","Conserva ventas, stock y alertas",()->{BodegaService t=new BodegaService(repo);verificar(t.ventas().size()==3&&t.buscarProducto(1).getStock()==4&&t.alertas().size()==2);});
            prueba("Fallo de escritura","Repositorio que lanza IOException","Venta y stock se revierten en memoria y disco",()->{
                Repositorio falla=new Repositorio(){ public EstadoBodega cargar()throws Exception{return repo.cargar();}public void guardar(EstadoBodega e)throws Exception{throw new java.io.IOException("Fallo simulado");}};
                BodegaService t=new BodegaService(falla);rechaza(()->t.registrarVenta(Map.of(1,1)));verificar(t.buscarProducto(1).getStock()==4&&repo.cargar().getVentas().size()==3);
            });
            prueba("Varias líneas, una inválida","Producto 1 válido y producto 3 insuficiente","Ninguna línea se descuenta",()->{
                s.guardarProducto(3,"Leche",new BigDecimal("3.20"),1,1,false);Map<Integer,Integer> carrito=new LinkedHashMap<>();carrito.put(1,1);carrito.put(3,2);
                rechaza(()->s.registrarVenta(carrito));verificar(s.buscarProducto(1).getStock()==4&&s.buscarProducto(3).getStock()==1);
            });
            prueba("Decimal exacto","Precio 0.10 x 3","Total exacto 0.30",()->{s.guardarProducto(4,"Caramelo",new BigDecimal("0.10"),10,1,false);verificar(s.registrarVenta(Map.of(4,3)).getTotal().compareTo(new BigDecimal("0.30"))==0);});
            prueba("Bloqueo de instancia","Abrir archivo ya abierto","Rechazo de segunda instancia",()->rechaza(()->{try(RepositorioArchivo otro=new RepositorioArchivo(ruta)) { }}));
            prueba("Stock inicial bajo","Producto nuevo con stock 0","Alerta pendiente desde el registro",()->{s.guardarProducto(5,"Agotado",BigDecimal.ONE,0,1,false);verificar(s.alertas().stream().anyMatch(a->a.getProductoId()==5&&!a.isAtendida()));});
            prueba("Cambio de precio histórico","Cambiar 0.10 por 0.20","Venta anterior mantiene 0.30",()->{s.guardarProducto(4,"Caramelo",new BigDecimal("0.20"),7,1,true);verificar(s.ventas().get(3).getTotal().compareTo(new BigDecimal("0.30"))==0);});
            prueba("Reposición inválida","-2 unidades","Rechazo sin cambio de stock",()->{rechaza(()->s.reponer(1,-2));verificar(s.buscarProducto(1).getStock()==4);});
        }
        guardar();System.out.println(numero+" pruebas aprobadas. Registro: docs/PRUEBAS.md");
    }
}
