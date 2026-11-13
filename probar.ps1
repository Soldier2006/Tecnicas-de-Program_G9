$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
& "$PSScriptRoot/compilar.ps1"
& javac -encoding UTF-8 --release 17 -cp build -d build herramientas/Pruebas.java herramientas/Evidencias.java
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las pruebas.' }
& java -cp build herramientas.Pruebas
if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas.' }
& java -cp build herramientas.Evidencias
if ($LASTEXITCODE -ne 0) { throw 'Falló la captura de evidencias.' }
