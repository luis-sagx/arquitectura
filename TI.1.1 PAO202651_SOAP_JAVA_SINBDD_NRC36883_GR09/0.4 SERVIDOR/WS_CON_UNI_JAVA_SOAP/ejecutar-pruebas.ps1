$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $fuentesConversion = @(Get-ChildItem -LiteralPath 'src/main/java/ec/edu/monster/pruebas', 'src/main/java/ec/edu/monster/modelos', 'src/main/java/ec/edu/monster/servicios' -Filter *.java | ForEach-Object FullName)
    & javac -encoding UTF-8 --release 17 -d target/pruebas @fuentesConversion
    if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las pruebas' }
    & java -cp target/pruebas ec.edu.monster.pruebas.PruebaSistemaUnidades
    if ($LASTEXITCODE -ne 0) { throw 'Fallo una prueba del sistema de unidades' }
} finally {
    Pop-Location
}
