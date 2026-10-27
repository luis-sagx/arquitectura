$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & mvn -B -ntp -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Maven no pudo compilar las pruebas SOAP del cliente web.' }
    & java -cp 'target/classes;target/WEB-INF/lib/*' ec.edu.monster.pruebas.PruebaClienteSoap
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba SOAP del cliente web.' }
} finally {
    Pop-Location
}
