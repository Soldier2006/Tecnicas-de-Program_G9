param([ValidatePattern('^[0-9]{1,3}$')][string]$Grupo)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
& "$PSScriptRoot/compilar.ps1"
& javac -encoding UTF-8 --release 17 -cp 'lib/itextpdf-5.5.13.3.jar' -d build herramientas/Documentos.java
if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar la herramienta PDF.' }
New-Item -ItemType Directory -Force entregables | Out-Null
if ($Grupo) {
    $nombre = 'T3_grupo_' + $Grupo.PadLeft(2,'0')
    $pdf = "entregables/$nombre.pdf"
    $zip = "entregables/$nombre.zip"
} else {
    $pdf = 'entregables/Informe_borrador.pdf'
    $zip = 'entregables/Entrega3_borrador.zip'
}
& java -cp 'build;lib/itextpdf-5.5.13.3.jar' herramientas.Documentos $pdf
if ($LASTEXITCODE -ne 0) { throw 'Falló el generador PDF.' }
$staging = Join-Path (Join-Path $env:LOCALAPPDATA 'Temp/opencode') ('bodexa-entrega-' + [guid]::NewGuid())
New-Item -ItemType Directory -Path $staging | Out-Null
try {
    foreach ($item in @('src','sql','herramientas','docs','dist','lib','README.md','compilar.ps1','probar.ps1','empaquetar.ps1','ejecutar.bat','ejecutar-sql.bat','.gitignore')) {
        Copy-Item -Path $item -Destination $staging -Recurse
    }
    Copy-Item $pdf $staging
    Compress-Archive -Path "$staging/*" -DestinationPath $zip -Force
} finally {
    Remove-Item -LiteralPath $staging -Recurse -Force
}
Write-Host "Archivos generados: $pdf y $zip"
Write-Host 'Revisar datos pendientes del informe y enlace GitHub antes de entregar.'
