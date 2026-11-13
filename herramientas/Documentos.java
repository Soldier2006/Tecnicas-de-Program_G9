package herramientas;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;

/** Generador reproducible del informe PDF a partir de los documentos editables. */
public class Documentos {
    private static Graphics2D g;
    private static void caja(int x,int y,int w,String titulo,String... lineas) {
        int h=75+lineas.length*31;
        g.setColor(new Color(239,246,249));g.fillRoundRect(x,y,w,h,16,16);
        g.setColor(new Color(20,48,68));g.setStroke(new BasicStroke(3));g.drawRoundRect(x,y,w,h,16,16);
        g.setFont(new java.awt.Font("SansSerif",java.awt.Font.BOLD,27));g.drawString(titulo,x+18,y+38);g.drawLine(x,y+52,x+w,y+52);
        g.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,23));
        for(int i=0;i<lineas.length;i++)g.drawString(lineas[i],x+18,y+84+i*31);
    }
    private static void flecha(int x1,int y1,int x2,int y2,String texto,boolean discontinua) {
        g.setColor(new Color(30,100,115));
        g.setStroke(discontinua?new BasicStroke(3,0,0,10,new float[]{10,8},0):new BasicStroke(3));g.drawLine(x1,y1,x2,y2);
        double a=Math.atan2(y2-y1,x2-x1);int z=18;
        g.drawLine(x2,y2,x2-(int)(z*Math.cos(a-0.5)),y2-(int)(z*Math.sin(a-0.5)));
        g.drawLine(x2,y2,x2-(int)(z*Math.cos(a+0.5)),y2-(int)(z*Math.sin(a+0.5)));
        if (texto.equals("implementa")) {
            int[] xs={x2,x2-(int)(28*Math.cos(a-0.45)),x2-(int)(28*Math.cos(a+0.45))};
            int[] ys={y2,y2-(int)(28*Math.sin(a-0.45)),y2-(int)(28*Math.sin(a+0.45))};
            g.setColor(Color.WHITE);g.fillPolygon(xs,ys,3);g.setColor(new Color(30,100,115));g.setStroke(new BasicStroke(3));g.drawPolygon(xs,ys,3);
        }
        g.setFont(new java.awt.Font("SansSerif",java.awt.Font.PLAIN,21));
        g.drawString(texto,Math.min(x1,x2)+12,(y1+y2)/2-10);
    }
    private static void diagramas() throws Exception {
        BufferedImage espina=new BufferedImage(1450,800,BufferedImage.TYPE_INT_RGB);g=espina.createGraphics();
        g.setColor(Color.WHITE);g.fillRect(0,0,1450,800);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        flecha(40,400,1090,400,"",false);
        caja(1090,315,325,"Efecto","Inventario","desactualizado");
        String[][] arriba={{"Método","Venta y stock se","anotan por separado"},{"Información","Datos dispersos","sin detalle por producto"},{"Personas","Omisión involuntaria","de actualizaciones"}};
        String[][] abajo={{"Herramientas","Sin validación","de disponibilidad"},{"Seguimiento","Sin aviso visible","de stock mínimo"},{"Control","Revisión tardía","de existencias"}};
        for(int i=0;i<3;i++){
            int x=50+i*345;
            flecha(x+90,255,x+240,400,"",false);flecha(x+90,560,x+240,400,"",false);
            g.setColor(new Color(20,48,68));g.setFont(new java.awt.Font("SansSerif",1,27));g.drawString(arriba[i][0],x,110);g.drawString(abajo[i][0],x,640);
            g.setFont(new java.awt.Font("SansSerif",0,24));for(int j=1;j<3;j++){g.drawString(arriba[i][j],x,125+j*35);g.drawString(abajo[i][j],x,655+j*35);}
        }
        g.dispose();ImageIO.write(espina,"png",Path.of("docs/evidencias/00_ishikawa.png").toFile());
        BufferedImage im=new BufferedImage(1450,1280,BufferedImage.TYPE_INT_RGB);g=im.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,1450,1280);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        caja(35,35,610,"Producto","- id: int; - nombre: String","- precio: BigDecimal; - stock: int","- categoria: Categoria","+ descontar(cantidad: int): void","+ reponer(cantidad: int): void","+ tieneStockBajo(): boolean");
        caja(805,35,610,"Categoria","- id: int","- nombre: String","+ Categoria(id: int, nombre: String)","+ getId(): int","+ getNombre(): String");
        flecha(645,150,805,150,"0..* → 1",false);
        caja(35,440,610,"Venta","- id: int; - fecha: LocalDateTime","- detalles: List<DetalleVenta>","+ getTotal(): BigDecimal","+ getDetalles(): List<DetalleVenta>");
        caja(805,440,610,"DetalleVenta","- productoId: int","- nombreProducto: String","- cantidad: int","- precioUnitario: BigDecimal","+ getSubtotal(): BigDecimal");
        // Diamante relleno: composición desde Venta.
        g.setColor(new Color(30,100,115));g.fillPolygon(new int[]{645,660,675,660},new int[]{550,540,550,560},4);
        flecha(675,550,805,550,"1 : 1..*",false);
        flecha(815,440,635,310,"refiere por ID",true);
        caja(35,875,610,"EstadoBodega","- categorias: Map<Integer,Categoria>","- productos: Map<Integer,Producto>","- ventas: List<Venta>","- alertas: List<AlertaStock>","+ getProductos(): Map<Integer,Producto>");
        caja(805,875,610,"AlertaStock","- id: int; - productoId: int","- fecha: LocalDateTime","- atendida: boolean","+ atender(): void","+ isAtendida(): boolean");
        flecha(340,875,340,639,"agrupa 0..* ventas",false);
        flecha(645,1000,805,1000,"1 → 0..*",false);
        g.setColor(Color.DARK_GRAY);g.setFont(new java.awt.Font("SansSerif",0,22));
        g.drawString("EstadoBodega agrupa 0..* categorías y productos. AlertaStock refiere a un producto por ID.",35,1215);
        g.drawString("Atributos privados (-), métodos públicos (+). Getters triviales omitidos. Fuente completa: DIAGRAMA_CLASES.puml.",35,1250);
        g.dispose();ImageIO.write(im,"png",Path.of("docs/evidencias/07_uml_dominio.png").toFile());

        im=new BufferedImage(1450,1220,BufferedImage.TYPE_INT_RGB);g=im.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,1450,1220);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        caja(35,35,610,"VentanaBodega extends JFrame","- servicio: BodegaService","- cantidades: Map<Integer,Integer>","+ refrescar(): void","+ mostrarMensaje(texto, error): void");
        caja(805,35,610,"BodegaService","- repositorio: Repositorio","- estado: EstadoBodega","+ buscarProducto(id: int): Producto","+ buscarProducto(texto: String): List<Producto>","+ registrarVenta(cantidades): Venta","+ reponer(id: int, cantidad: int): void");
        flecha(645,150,805,150,"usa 1",false);
        caja(805,455,610,"<<interface>> Repositorio","+ cargar(): EstadoBodega","+ guardar(estado: EstadoBodega): void","+ close(): void");
        flecha(1100,296,1100,455,"usa 1",false);
        caja(35,455,610,"Main","+ main(args: String[]): void","Crea el servicio y selecciona repositorio.");
        flecha(340,455,340,234,"inicia",true);
        caja(35,875,610,"RepositorioArchivo","- archivo: Path","- canal: FileChannel; - bloqueo: FileLock","+ cargar(): EstadoBodega","+ guardar(estado: EstadoBodega): void","+ close(): void");
        caja(805,875,610,"RepositorioSql","- conexion: Connection","+ cargar(): EstadoBodega","+ guardar(estado: EstadoBodega): void","+ close(): void");
        flecha(340,875,805,620,"implementa",true);flecha(1100,875,1100,623,"implementa",true);
        g.setColor(Color.DARK_GRAY);g.setFont(new java.awt.Font("SansSerif",0,22));g.drawString("Ambos adaptadores cumplen el mismo contrato. El servicio mantiene una instancia de EstadoBodega.",35,1180);
        g.dispose();ImageIO.write(im,"png",Path.of("docs/evidencias/08_uml_arquitectura.png").toFile());
    }
    private static String limpiar(String t) { return t.replace("**","").replace("`",""); }
    public static void main(String[] args) throws Exception {
        diagramas();
        Path salida=Path.of(args.length>0?args[0]:"entregables/Informe_borrador.pdf");Files.createDirectories(salida.toAbsolutePath().getParent());
        String texto=Files.readString(Path.of("docs/INFORME.md"));
        texto=texto.replace("{{PRUEBAS}}",Files.readString(Path.of("docs/PRUEBAS.md")));
        texto=texto.replace("{{SQL}}",Files.readString(Path.of("docs/evidencias/pruebas_sql.txt")));
        BaseFont base=BaseFont.createFont("C:/Windows/Fonts/arial.ttf",BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
        Font normal=new Font(base,10), tabla=new Font(base,8), titulo=new Font(base,17,Font.BOLD,new BaseColor(20,48,68)), subtitulo=new Font(base,13,Font.BOLD,new BaseColor(20,75,90));
        Document doc=new Document(PageSize.A4,42,42,42,45);PdfWriter writer=PdfWriter.getInstance(doc,Files.newOutputStream(salida));
        writer.setPageEvent(new PdfPageEventHelper(){@Override public void onEndPage(PdfWriter w,Document d){ColumnText.showTextAligned(w.getDirectContent(),Element.ALIGN_CENTER,new Phrase("Bodexa | "+w.getPageNumber(),new Font(base,8)),PageSize.A4.getWidth()/2,23,0);}});
        doc.addTitle("Bodexa - Entrega 3");doc.open();
        String[] lineas=texto.split("\\R");
        for(int i=0;i<lineas.length;i++) {
            String l=lineas[i].trim();if(l.isBlank())continue;
            if(l.startsWith("![")) {
                int fin=l.indexOf("](");String descripcion=l.substring(2,fin);String archivo=l.substring(fin+2,l.lastIndexOf(')'));
                Image image=Image.getInstance(Path.of("docs").resolve(archivo).toAbsolutePath().toString());
                doc.newPage();Paragraph cap=new Paragraph(descripcion,subtitulo);cap.setSpacingAfter(12);doc.add(cap);
                image.scaleToFit(510,650);image.setAlignment(Element.ALIGN_CENTER);doc.add(image);doc.newPage();continue;
            }
            if(l.startsWith("|")) {
                java.util.List<String[]> filas=new ArrayList<>();
                while(i<lineas.length&&lineas[i].trim().startsWith("|")) {
                    String fila=lineas[i].trim();if(!fila.matches("[| :\\-]+"))filas.add(fila.substring(1,fila.length()-1).split("\\|",-1));i++;
                }i--;
                int cols=filas.get(0).length;PdfPTable t=new PdfPTable(cols);t.setWidthPercentage(100);t.setHeaderRows(1);t.setSpacingBefore(6);t.setSpacingAfter(10);
                if(cols==6)t.setWidths(new float[]{0.6f,1.3f,1.5f,1.8f,1.4f,0.8f});
                if(cols==5)t.setWidths(new float[]{0.6f,1.1f,2.8f,0.7f,1.7f});
                for(int row=0;row<filas.size();row++)for(String cell:filas.get(row)) {
                    PdfPCell c=new PdfPCell(new Phrase(limpiar(cell.trim()),tabla));c.setPadding(5);c.setBorderColor(new BaseColor(190,205,210));if(row==0)c.setBackgroundColor(new BaseColor(220,235,240));t.addCell(c);
                }doc.add(t);continue;
            }
            if(l.startsWith("#")) {
                int n=0;while(n<l.length()&&l.charAt(n)=='#')n++;
                if(l.startsWith("## 1."))doc.newPage();
                Paragraph p=new Paragraph(limpiar(l.substring(n).trim()),n==1?titulo:subtitulo);p.setSpacingBefore(14);p.setSpacingAfter(8);p.setKeepTogether(true);doc.add(p);
            }else{Paragraph p=new Paragraph(limpiar(l),normal);p.setLeading(14);p.setSpacingAfter(7);doc.add(p);}
        }
        doc.close();System.out.println("PDF generado: "+salida);
    }
}
