# Guion de sustentación (8 a 10 minutos, ajustar al tiempo del docente)

## 1. Problema y alcance — 1 minuto

Explicar cómo una venta registrada por separado del inventario puede dejar existencias incorrectas. Indicar qué bodega se consultó solo después de realizar esa validación. Presentar Bodexa como una aplicación local para encargado/propietario.

## 2. Demostración correcta — 3 minutos

1. Abrir ejecutar.bat. Registrar categoría Abarrotes.
2. Crear producto 101 Arroz, precio 4.50, stock 6. Si ya existe, usar otro código y explicar que los datos persisten.
3. Buscarlo por nombre. Agregar dos unidades al carrito y confirmar.
4. Mostrar total 9.00, stock 4, unidades vendidas 2.
5. Mostrar alerta pendiente e historial con sus detalles.
6. Reponer 10 unidades: stock 14 y alerta atendida.

## 3. Caso inválido — 1 minuto

Intentar vender más de lo disponible. Mostrar mensaje de rechazo y comprobar que no cambian el stock ni el número de ventas. Puede demostrarse también un código duplicado o precio negativo.

## 4. Diagrama y POO — 2 minutos

- Producto: atributos privados y métodos descontar/reponer.
- Venta contiene DetalleVenta; cada detalle conserva cantidad y precio histórico.
- BodegaService coordina validación y persistencia.
- Sobrecarga: buscarProducto(int) y buscarProducto(String).
- Polimorfismo: RepositorioArchivo y RepositorioSql implementan la misma interfaz.
- Colecciones: mapas para identificadores y carrito, listas para historial.
- Excepciones: errores de reglas o guardado se comunican y no dejan ventas a medias.

## 5. Pruebas, limitaciones y cierre — 1 a 2 minutos

Mostrar docs/PRUEBAS.md y mencionar el fallo de escritura simulado. Aclarar que las 28 pruebas corresponden al modo local. SQL cuenta con esquema y adaptador, pero la integración JDBC está pendiente de habilitar TCP/IP. Explicar que no hay pagos reales, facturas fiscales ni múltiples cajas.

## Preguntas que todos deben saber responder

- ¿Qué pasa cuando el stock es exactamente 5? No hay alerta pendiente por stock bajo.
- ¿Cómo evitan vender más de lo que hay? Se suman cantidades en el carrito y el servicio vuelve a validar todas las líneas.
- ¿Qué pasa si falla el guardado? El archivo anterior se conserva y el servicio recarga el estado; en SQL se usa rollback.
- ¿Por qué BigDecimal? Para representar importes decimales sin errores binarios típicos de double.
- ¿Qué diferencia hay entre interfaz y clase? Repositorio define el contrato; cada adaptador implementa cómo almacenar.
- ¿Por qué no hay un trigger de descuento? El servicio ya descuenta; duplicar esa lógica provocaría doble salida.
- ¿Por qué no se elimina un producto vendido? Para mantener la relación con su historial.
- ¿Qué ocurre al cambiar el precio? Las ventas anteriores conservan el precio unitario registrado.
- ¿Qué falta para entregar? Datos del equipo, contexto real, GitHub y evidencias de colaboración.

Repartir la exposición entre los integrantes reales. Todos deben ensayar el flujo completo y leer el código, no memorizar únicamente su sección.
