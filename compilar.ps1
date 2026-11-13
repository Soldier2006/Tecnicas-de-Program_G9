$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force build, dist | Out-Null
$fuentes = @(Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& javac -encoding UTF-8 --release 17 -d build @fuentes
if ($LASTEXITCODE -ne 0) { throw 'La compilación falló.' }
& jar --create --file dist/Bodexa.jar --main-class bodexa.Main -C build bodexa
if ($LASTEXITCODE -ne 0) { throw 'No se pudo generar el JAR.' }
Write-Host 'Compilación correcta: dist/Bodexa.jar'
