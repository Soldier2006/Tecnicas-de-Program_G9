# Bodexa — Inventario, ventas y alertas

Aplicación Java 17 de escritorio para una bodega, con registro de productos, categorías, ventas con detalles, descuento de existencias, reposición, alertas e historial. Nombre provisional: Bodexa.

## Inicio rápido (sin instalar SQL Server)

1. Tener Java 17 o superior. Comprobar `java -version` en una terminal.
2. Extraer **todo** el ZIP en una carpeta con permiso de escritura.
3. Abrir `ejecutar.bat`, o ejecutar `java -jar dist/Bodexa.jar` desde esta carpeta.
4. En Productos, pulsar **Nueva categoría** y registrar `Abarrotes`.
5. Registrar código `101`, nombre `Arroz 1 kg`, precio `4.50`, stock inicial `6`, categoría `Abarrotes`.
6. En Registrar venta, seleccionar Arroz, cantidad `2`, Agregar al carrito y Confirmar venta.
7. Productos mostrará stock `4`, vendidos `2`. Alertas mostrará una alerta pendiente.
8. Intentar vender `5`: se rechaza. Para reabastecer, seleccionar el producto en Productos y pulsar Reponer; ingresar `10`. El stock será `14` y la alerta quedará atendida.
9. Cerrar y volver a abrir. Los datos siguen guardados en `datos/bodexa.dat`.

Este es un modo persistente completo, no una simulación. No necesita usuario ni contraseña. Los datos de este modo y los de SQL son independientes; no hay sincronización o migración automática. No borrar `datos` si se desea conservar el trabajo.

## Compilar desde código

Se necesita JDK 17 o superior, con `java`, `javac` y `jar` en PATH. No requiere NetBeans ni Maven.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\compilar.ps1
```

Salida: `dist/Bodexa.jar`. El modo local usa solamente la biblioteca estándar de Java.

## Modo SQL Server (opcional, configuración TCP pendiente en el equipo revisado)

El código del adaptador está incluido; **su conexión JDBC no se ha validado en este equipo**, porque SQL Server solo acepta memoria compartida y se decidió no reiniciar el servicio. Las tablas y restricciones sí se prueban con `sqlcmd`.

1. Instalar SQL Server y SQL Server Management Studio o `sqlcmd`.
2. Ejecutar `sql/01_crear_base.sql` en SQL Server. Crea exclusivamente `BodexaT3` si no existe.
3. En SQL Server Configuration Manager: Configuración de red > Protocolos de la instancia > TCP/IP > Habilitar. En IPAll establecer puerto TCP 1433 y limpiar puertos dinámicos si se elige puerto fijo. Reiniciar el servicio de la instancia cuando se pueda interrumpir su uso. Para otra instancia/puerto, ajustar la URL.
4. Verificar: `sqlcmd -S tcp:localhost,1433 -E -C -l 5 -Q "SELECT DB_NAME()"`.
5. El ZIP incluye el driver JDBC Microsoft 13.4 y su DLL x64, recuperados del material proporcionado. Autenticación integrada exige Windows y Java x64.
6. Ejecutar `ejecutar-sql.bat`. Para SQL Authentication, definir variables en la terminal y lanzar desde ella:

```powershell
$env:BODEXA_DB_URL='jdbc:sqlserver://localhost:1433;databaseName=BodexaT3;encrypt=true;trustServerCertificate=true;loginTimeout=5'
$env:BODEXA_DB_USER='usuario_configurado_en_sql'
$env:BODEXA_DB_PASSWORD='contraseña_del_usuario'
.\ejecutar-sql.bat
```

La URL predeterminada usa autenticación Windows. El usuario SQL necesita acceso de lectura/escritura en las tablas de BodexaT3. `trustServerCertificate=true` se utiliza para la instancia local de laboratorio.

No ejecutar `Bodega.sql` ni `Trigger.sql` del proyecto anterior sobre esta base. Esta versión aplica el descuento en Java y guarda el estado en una transacción: agregar un trigger de descuento duplicaría la salida de stock.

El adaptador SQL usa un bloqueo de aplicación para una sola instancia y persiste una instantánea completa del catálogo e historial. Es una implementación académica para volúmenes pequeños. No editar las tablas mientras la aplicación está abierta; para múltiples cajas debe sustituirse por actualizaciones incrementales con control de concurrencia.

## Uso de cada módulo

- **Productos:** registrar categoría; registrar, buscar, seleccionar y editar producto; eliminar solo productos sin ventas. El código y el stock inicial dejan de ser editables al seleccionar un producto. Para un registro nuevo usar Nuevo / limpiar.
- **Reponer:** aumenta stock con una cantidad positiva. No hay historial detallado de entradas en esta versión.
- **Registrar venta:** agrupa cantidades del mismo producto. Confirmar valida todas las líneas, toma precios del catálogo y guarda venta, detalles, stock y alertas juntos. No procesa pagos ni emite comprobantes fiscales.
- **Alertas:** stock bajo significa estrictamente **menos de 5**. No se duplican alertas pendientes. Una reposición que alcance 5 atiende automáticamente la alerta; volver a bajar genera otra.
- **Historial:** seleccionar una venta y Ver detalle. Conserva el nombre y el precio de la fecha de venta aunque el catálogo cambie.
- **Vendidos:** unidades acumuladas por producto; las reposiciones aumentan stock sin aumentar ventas.

## Pruebas y evidencias

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\probar.ps1
sqlcmd -S localhost -E -C -b -i sql/02_pruebas_sql.sql
```

La primera orden compila, ejecuta pruebas en archivos temporales y captura componentes Swing reales con datos ficticios. No modifica los datos de uso normal. Resultados en `docs/PRUEBAS.md`; capturas en `docs/evidencias`. La segunda comprueba tablas, restricciones y rollback SQL, no la conexión JDBC ni la interfaz en modo SQL. Requiere haber ejecutado el script 01.

## Organización

```text
src/bodexa/modelo       Entidades y estado
src/bodexa/servicio     Reglas de negocio
src/bodexa/persistencia Interfaz y adaptadores de archivo / SQL
src/bodexa/vista        Ventana Swing
sql                    Creación y pruebas de esquema
herramientas           Pruebas, capturas y generador de informe
docs                   Informe editable, UML, pruebas y sustentación
dist                   JAR ejecutable
lib                    Driver JDBC y herramientas PDF
```

## Informe y ZIP

Completar institución, curso, docente, integrantes, grupo, fecha y URL real del repositorio en `docs/INFORME.md`. Consultar `docs/PENDIENTES.md`. Para regenerar PDF y ZIP, ejecutar `empaquetar.ps1 -Grupo 04` (sustituir por el grupo real). Los borradores no sustituyen estos datos.

El repositorio GitHub debe crearlo el equipo en su cuenta; el historial debe contener sus aportes reales. No hay publicación automática ni evidencia inventada de participación.

## Origen y limitaciones

Esta versión reorganiza la idea y corrige el flujo detectado en los archivos de Bodega suministrados: separación entre cabecera y descuento, falta de detalles desde la pantalla de pago e incoherencias del SQL de alertas. Los archivos originales permanecen en su carpeta. No se importaron tickets históricos, credenciales de correo ni datos de otras bases.

No incluye correo a proveedores, vencimientos/lotes, clientes nominales, anulaciones, pagos, facturación fiscal, múltiples usuarios ni acceso web. No se afirma que se hayan presentado entregas previas. La problemática contextual debe confirmarse con una bodega real antes de presentarla como observación de campo.

Dependencias: Microsoft JDBC Driver for SQL Server (modo SQL) e iText 5.5.13.3 (solo herramienta de PDF). Consultar sus licencias antes de redistribución comercial. La aplicación local no depende de iText. Los JAR se tomaron de `Bodega/Bodega/dist/lib` proporcionado por el usuario.
