# Registro real de pruebas

Ejecución: 2026-11-12T11:28:43.981283600

Entorno: Java 17.0.20.1, Windows 11. Repositorio de archivo temporal, independiente de los datos del usuario.

| Código | Funcionalidad | Datos o condición | Resultado esperado | Resultado obtenido | Estado |
|---|---|---|---|---|---|
| P01 | Categoría | Abarrotes | Una categoría persistida | Coincide con lo esperado | Aprobado |
| P02 | Categoría duplicada | abarrotes | Rechazo sin duplicar | Coincide con lo esperado | Aprobado |
| P03 | Producto válido | Arroz, 4.50, stock 6 | Producto guardado | Coincide con lo esperado | Aprobado |
| P04 | Código duplicado | Código 1 | Rechazo, stock sigue en 6 | Coincide con lo esperado | Aprobado |
| P05 | Campo vacío | Nombre en blanco | Rechazo | Coincide con lo esperado | Aprobado |
| P06 | Precio incorrecto | Precio -1 | Rechazo | Coincide con lo esperado | Aprobado |
| P07 | Stock negativo | Stock -1 | Rechazo | Coincide con lo esperado | Aprobado |
| P08 | Búsqueda sin resultados | Inexistente | Lista vacía | Coincide con lo esperado | Aprobado |
| P09 | Búsqueda por nombre | ARR | Encuentra Arroz | Coincide con lo esperado | Aprobado |
| P10 | Modificar | Arroz extra | Nombre cambia y stock se conserva | Coincide con lo esperado | Aprobado |
| P11 | Venta normal | 2 unidades a 4.50 | Stock 4; total 9.00; detalle y alerta | Coincide con lo esperado | Aprobado |
| P12 | Venta inválida | 5 unidades con stock 4 | Rechazo; no aumenta historial ni baja stock | Coincide con lo esperado | Aprobado |
| P13 | Cantidad cero | 0 unidades | Rechazo | Coincide con lo esperado | Aprobado |
| P14 | Carrito vacío | Sin detalles | Rechazo | Coincide con lo esperado | Aprobado |
| P15 | Alerta no duplicada | Vender 1 con stock 4 | Stock 3 y una alerta pendiente | Coincide con lo esperado | Aprobado |
| P16 | Reposición parcial | Agregar 1 a stock 3 | Stock 4; alerta sigue pendiente | Coincide con lo esperado | Aprobado |
| P17 | Límite de alerta | Agregar 1 a stock 4 | Stock 5; alerta atendida | Coincide con lo esperado | Aprobado |
| P18 | Nuevo agotamiento | Vender 1 desde stock 5 | Nueva alerta pendiente | Coincide con lo esperado | Aprobado |
| P19 | Eliminar con historial | Producto 1 vendido | Rechazo | Coincide con lo esperado | Aprobado |
| P20 | Eliminar sin ventas | Crear y eliminar producto 2 | Producto eliminado | Coincide con lo esperado | Aprobado |
| P21 | Persistencia | Recargar desde disco | Conserva ventas, stock y alertas | Coincide con lo esperado | Aprobado |
| P22 | Fallo de escritura | Repositorio que lanza IOException | Venta y stock se revierten en memoria y disco | Coincide con lo esperado | Aprobado |
| P23 | Varias líneas, una inválida | Producto 1 válido y producto 3 insuficiente | Ninguna línea se descuenta | Coincide con lo esperado | Aprobado |
| P24 | Decimal exacto | Precio 0.10 x 3 | Total exacto 0.30 | Coincide con lo esperado | Aprobado |
| P25 | Bloqueo de instancia | Abrir archivo ya abierto | Rechazo de segunda instancia | Coincide con lo esperado | Aprobado |
| P26 | Stock inicial bajo | Producto nuevo con stock 0 | Alerta pendiente desde el registro | Coincide con lo esperado | Aprobado |
| P27 | Cambio de precio histórico | Cambiar 0.10 por 0.20 | Venta anterior mantiene 0.30 | Coincide con lo esperado | Aprobado |
| P28 | Reposición inválida | -2 unidades | Rechazo sin cambio de stock | Coincide con lo esperado | Aprobado |

La integración JDBC con SQL Server requiere TCP/IP: no evaluada en estas pruebas.
