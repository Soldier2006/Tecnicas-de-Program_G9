# Lo que necesita completar el equipo antes de entregar

## Datos que no pueden deducirse

- Institución, nombre oficial del curso, docente, integrantes y códigos, grupo y fecha.
- Confirmar Bodexa como nombre.
- Identificar una bodega real y validar la descripción del problema con su encargado.
- Confirmar procedencia/autoría del material original suministrado.
- Crear GitHub, compartir acceso al docente y pegar URL en el informe.
- Registrar aportes efectivos de cada integrante y capturas de commits o PR. No retrofechar ni simular participación.
- Ejecutar manualmente el flujo y ensayar la exposición entre todos.

## Estado técnico comprobado

- JDK 17 disponible; compilación y JAR ejecutable.
- 28 pruebas de servicio, modelo y archivo aprobadas.
- 8 capturas de componentes Swing reales con datos ficticios.
- Esquema SQL creado en BodexaT3 y cuatro verificaciones SQL aprobadas.
- JDBC sin prueba integral: SQL Server no escucha TCP y se decidió no reiniciarlo. Puede presentarse el modo local como versión funcional y declarar SQL parcial.
- Originales Bodega y scripts raíz conservados; entrega final organizada en Entrega3.

## GitHub, desde cero

1. Instalar Git o GitHub Desktop. Configurar la identidad de cada integrante en su equipo.
2. Crear un repositorio para Bodexa en la cuenta del equipo e invitar integrantes.
3. Subir la carpeta Entrega3 usando la aplicación o Git. Revisar .gitignore: no subir datos de uso ni contraseñas.
4. Hacer un primer commit identificando la base recibida y preparada con asistencia; no presentarlo como historial del semestre.
5. Cada integrante revisa, corrige o documenta una parte concreta y registra sus aportes reales.
6. Copiar URL y enlaces a commits en la sección 11. Guardar capturas legibles.

## Archivos de entrega

El informe inicial se llama Informe_borrador.pdf porque todavía no conocemos el grupo. El ZIP inicial se llama Entrega3_borrador.zip. No entregar esos nombres al docente.

Después de completar docs/INFORME.md, ejecutar desde Entrega3:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\empaquetar.ps1 -Grupo 04
```

Sustituir 04 por el grupo real. Genera `entregables/T3_grupo_04.pdf` y `entregables/T3_grupo_04.zip`. Abrir el PDF, revisar que no queden campos PENDIENTE y probar el ZIP extraído en otra carpeta.

El código y el informe son revisables; la sustentación y la participación del equipo deben realizarse personalmente.
