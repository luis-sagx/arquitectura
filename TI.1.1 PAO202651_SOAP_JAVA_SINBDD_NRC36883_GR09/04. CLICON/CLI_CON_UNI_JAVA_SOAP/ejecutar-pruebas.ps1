param([string]$Maven)
$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'construir.ps1') -Maven $Maven
Push-Location $PSScriptRoot
try {
    & java -cp 'target/classes;target/lib/*' ec.edu.monster.pruebas.PruebaClienteConsola
    if ($LASTEXITCODE -ne 0) { throw 'Fallo una prueba del cliente' }
} finally { Pop-Location }
