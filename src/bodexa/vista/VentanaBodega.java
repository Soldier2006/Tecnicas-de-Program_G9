package bodexa.vista;

import bodexa.modelo.*;
import bodexa.servicio.BodegaService;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VentanaBodega extends JFrame {
    private final BodegaService servicio;
    private final JTabbedPane pestanas = new JTabbedPane();
    private final DefaultTableModel productos = modelo("Código", "Producto", "Categoría", "Precio S/", "Stock", "Vendidos", "Estado");
    private final DefaultTableModel carrito = modelo("Código", "Producto", "Cantidad", "Precio S/", "Subtotal S/");
    private final DefaultTableModel alertas = modelo("Alerta", "Fecha", "Producto", "Stock actual", "Estado");
    private final DefaultTableModel ventas = modelo("Venta", "Fecha", "Productos", "Total S/");
    private final JTable tablaProductos = new JTable(productos);
    private final JTable tablaCarrito = new JTable(carrito);
    private final JTable tablaVentas = new JTable(ventas);
    private final JTextField codigo = new JTextField(6), nombre = new JTextField(14), precio = new JTextField(6), stock = new JTextField(5), buscar = new JTextField(20);
    private final JComboBox<Categoria> categoria = new JComboBox<>();
    private final JComboBox<Producto> productoVenta = new JComboBox<>();
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 1000000, 1));
    private final JLabel resumen = new JLabel(), total = new JLabel("Total S/ 0.00"), mensaje = new JLabel("Listo. Registre una categoría y sus productos para comenzar.");
    private final Map<Integer,Integer> cantidades = new LinkedHashMap<>();
    private boolean editando;
    private boolean avisos = true;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public VentanaBodega(BodegaService servicio, String modo) {
        super("Bodexa | Inventario y ventas"); this.servicio = servicio;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setSize(1100, 700); setLocationRelativeTo(null);
        JPanel cabecera = new JPanel(new BorderLayout(16, 8)); cabecera.setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        cabecera.setBackground(new Color(20, 48, 68));
        JLabel titulo = new JLabel("BODEXA  /  Inventario bajo control"); titulo.setForeground(Color.WHITE); titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        resumen.setForeground(Color.WHITE); cabecera.add(titulo, BorderLayout.NORTH); cabecera.add(resumen, BorderLayout.CENTER);
        JLabel modoLabel = new JLabel("Modo: " + modo + " · Uso local, una instancia"); modoLabel.setForeground(new Color(179,224,216)); cabecera.add(modoLabel, BorderLayout.SOUTH);
        add(cabecera,BorderLayout.NORTH);
        pestanas.addTab("1. Productos", panelProductos()); pestanas.addTab("2. Registrar venta", panelVenta());
        pestanas.addTab("3. Alertas", panelAlertas()); pestanas.addTab("4. Historial", panelHistorial()); add(pestanas);
        mensaje.setBorder(BorderFactory.createEmptyBorder(12,20,12,20)); add(mensaje,BorderLayout.SOUTH); refrescar();
    }
    private static DefaultTableModel modelo(String... titulos) {
        return new DefaultTableModel(titulos, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }
    private JPanel panel() { JPanel p = new JPanel(new BorderLayout(8,8)); p.setBorder(BorderFactory.createEmptyBorder(16,16,16,16)); return p; }
    private interface Accion { void ejecutar() throws Exception; }
    private JButton boton(String texto, Accion accion) {
        JButton b = new JButton(texto); b.addActionListener(e -> {
            try { accion.ejecutar(); refrescar(); }
            catch (Exception ex) { mostrarMensaje(ex.getMessage(), true); refrescar(); }
        }); return b;
    }
    private JPanel panelProductos() {
        JPanel p = panel(), superior = new JPanel(new GridLayout(3,1));
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT)); filtro.add(new JLabel("Buscar código o nombre:")); filtro.add(buscar);
        filtro.add(boton("Buscar", this::filtrar)); filtro.add(boton("Nueva categoría", () -> {
            String n = JOptionPane.showInputDialog(this,"Nombre de la categoría:"); if(n != null) { servicio.registrarCategoria(n); mostrarMensaje("Categoría registrada.",false); }
        })); superior.add(filtro);
        JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        campos.add(new JLabel("Código"));campos.add(codigo);campos.add(new JLabel("Nombre"));campos.add(nombre);campos.add(new JLabel("Precio"));campos.add(precio);campos.add(new JLabel("Stock inicial"));campos.add(stock);campos.add(categoria);superior.add(campos);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(boton("Guardar", () -> {
            Categoria c=(Categoria)categoria.getSelectedItem();
            if(c==null) throw new IllegalArgumentException("Primero registre una categoría.");
            servicio.guardarProducto(Integer.parseInt(codigo.getText().trim()),nombre.getText(),new BigDecimal(precio.getText().trim().replace(',','.')),Integer.parseInt(stock.getText().trim()),c.getId(),editando);
            limpiar(); mostrarMensaje("Producto guardado.",false);
        }));
        acciones.add(boton("Nuevo / limpiar", this::limpiar));
        acciones.add(boton("Reponer", () -> {
            int id = seleccionado(); String n = JOptionPane.showInputDialog(this,"Unidades que ingresan al inventario:");
            if(n!=null) { servicio.reponer(id,Integer.parseInt(n.trim())); limpiar(); mostrarMensaje("Reposición guardada. Las alertas se atienden al alcanzar 5 unidades.",false); }
        }));
        acciones.add(boton("Eliminar", () -> {
            int id=seleccionado(); if(JOptionPane.showConfirmDialog(this,"¿Eliminar producto " + id + "?","Eliminar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) { servicio.eliminarProducto(id); cantidades.remove(id); limpiar(); mostrarMensaje("Producto eliminado.",false); }
        })); superior.add(acciones);p.add(superior,BorderLayout.NORTH);
        tablaProductos.setRowHeight(28); tablaProductos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            if(e.getValueIsAdjusting() || tablaProductos.getSelectedRow()<0) return;
            Producto x=servicio.buscarProducto(seleccionado()); editando=true; codigo.setEditable(false); stock.setEditable(false);
            codigo.setText(""+x.getId()); nombre.setText(x.getNombre()); precio.setText(x.getPrecio().toString()); stock.setText(""+x.getStock());
            for(int i=0;i<categoria.getItemCount();i++) if(categoria.getItemAt(i).getId()==x.getCategoria().getId()) categoria.setSelectedIndex(i);
        });p.add(new JScrollPane(tablaProductos)); return p;
    }
    private int seleccionado() {
        int fila=tablaProductos.getSelectedRow(); if(fila<0) throw new IllegalArgumentException("Seleccione un producto de la tabla."); return (int)productos.getValueAt(fila,0);
    }
    private void limpiar() { editando=false;codigo.setEditable(true);stock.setEditable(true);codigo.setText("");nombre.setText("");precio.setText("");stock.setText("");tablaProductos.clearSelection(); }
    private JPanel panelVenta() {
        JPanel p=panel(), barra=new JPanel(new FlowLayout(FlowLayout.LEFT)); productoVenta.setPreferredSize(new Dimension(350,30));
        barra.add(productoVenta); barra.add(new JLabel("Cantidad")); barra.add(cantidad);
        barra.add(boton("Agregar al carrito", () -> {
            Producto x=(Producto)productoVenta.getSelectedItem(); if(x==null) throw new IllegalArgumentException("No hay productos registrados.");
            cantidad.commitEdit(); int n=Math.addExact(cantidades.getOrDefault(x.getId(),0),(int)cantidad.getValue());
            if(n>x.getStock()) throw new IllegalArgumentException("Stock insuficiente. Disponible: "+x.getStock());
            cantidades.put(x.getId(),n);
        }));
        barra.add(boton("Quitar seleccionado", () -> { int fila=tablaCarrito.getSelectedRow(); if(fila>=0) cantidades.remove((int)carrito.getValueAt(fila,0)); }));
        p.add(barra,BorderLayout.NORTH);tablaCarrito.setRowHeight(30);p.add(new JScrollPane(tablaCarrito));
        JPanel pie=new JPanel(new FlowLayout(FlowLayout.RIGHT));total.setFont(new Font("SansSerif",Font.BOLD,20));pie.add(total);
        pie.add(boton("Confirmar venta", () -> { Venta v=servicio.registrarVenta(cantidades);cantidades.clear();mostrarMensaje("Venta #"+v.getId()+" guardada. Total S/ "+v.getTotal()+". Stock actualizado.",false); }));p.add(pie,BorderLayout.SOUTH);return p;
    }
    private JPanel panelAlertas() {
        JPanel p=panel();p.add(new JLabel("Stock bajo: menos de 5 unidades. Reponga desde Productos; con 5 o más la alerta pasa a ATENDIDA."),BorderLayout.NORTH);
        JTable t=new JTable(alertas);t.setRowHeight(28);p.add(new JScrollPane(t));return p;
    }
    private JPanel panelHistorial() {
        JPanel p=panel();tablaVentas.setRowHeight(28);p.add(new JScrollPane(tablaVentas));
        p.add(boton("Ver detalle de venta", () -> {
            int fila=tablaVentas.getSelectedRow();if(fila<0)throw new IllegalArgumentException("Seleccione una venta.");
            int id=(int)ventas.getValueAt(fila,0);Venta v=servicio.ventas().stream().filter(x->x.getId()==id).findFirst().orElseThrow();
            StringBuilder texto=new StringBuilder("Venta #"+id+"\n");
            for(DetalleVenta d:v.getDetalles())texto.append(d.getNombreProducto()).append(" | ").append(d.getCantidad()).append(" x S/ ").append(d.getPrecioUnitario()).append(" = S/ ").append(d.getSubtotal()).append('\n');
            texto.append("Total S/ ").append(v.getTotal());JOptionPane.showMessageDialog(this,texto.toString(),"Detalle",JOptionPane.INFORMATION_MESSAGE);
        }),BorderLayout.SOUTH);return p;
    }
    private void filtrar() {
        productos.setRowCount(0);
        for(Producto p:servicio.buscarProducto(buscar.getText()))productos.addRow(new Object[]{p.getId(),p.getNombre(),p.getCategoria().getNombre(),p.getPrecio(),p.getStock(),servicio.unidadesVendidas(p.getId()),p.tieneStockBajo()?"STOCK BAJO":"DISPONIBLE"});
    }
    public void refrescar() {
        Categoria cat=(Categoria)categoria.getSelectedItem(); Producto elegido=(Producto)productoVenta.getSelectedItem();
        categoria.removeAllItems();for(Categoria c:servicio.categorias()){categoria.addItem(c);if(cat!=null&&cat.getId()==c.getId())categoria.setSelectedItem(c);}
        productoVenta.removeAllItems();for(Producto p:servicio.productos()){productoVenta.addItem(p);if(elegido!=null&&elegido.getId()==p.getId())productoVenta.setSelectedItem(p);}
        filtrar();alertas.setRowCount(0);ventas.setRowCount(0);carrito.setRowCount(0);
        for(AlertaStock a:servicio.alertas()){Producto p=servicio.buscarProducto(a.getProductoId());alertas.addRow(new Object[]{a.getId(),FECHA.format(a.getFecha()),p.getNombre(),p.getStock(),a.isAtendida()?"ATENDIDA":"PENDIENTE"});}
        for(Venta v:servicio.ventas())ventas.addRow(new Object[]{v.getId(),FECHA.format(v.getFecha()),v.getDetalles().size(),v.getTotal()});
        BigDecimal suma=BigDecimal.ZERO;
        for(var e:cantidades.entrySet()) { Producto p=servicio.buscarProducto(e.getKey());BigDecimal sub=p.getPrecio().multiply(BigDecimal.valueOf(e.getValue()));suma=suma.add(sub);carrito.addRow(new Object[]{p.getId(),p.getNombre(),e.getValue(),p.getPrecio(),sub}); }
        total.setText("Total S/ "+suma.setScale(2));
        resumen.setText(servicio.productos().size()+" productos  ·  "+servicio.ventas().size()+" ventas  ·  "+servicio.alertas().stream().filter(a->!a.isAtendida()).count()+" alertas pendientes");
    }
    public void mostrarMensaje(String texto, boolean error) {
        mensaje.setForeground(error?new Color(170,30,30):new Color(20,90,65));mensaje.setText((error?"ERROR: ":"")+texto);
        if(error && avisos) JOptionPane.showMessageDialog(this,texto,"Validación",JOptionPane.WARNING_MESSAGE);
    }
    /** Permite capturar los componentes reales en la herramienta de evidencias sin cuadros modales. */
    public void setAvisos(boolean avisos) { this.avisos=avisos; }
    public void seleccionarPestana(int indice) { pestanas.setSelectedIndex(indice); }
}
