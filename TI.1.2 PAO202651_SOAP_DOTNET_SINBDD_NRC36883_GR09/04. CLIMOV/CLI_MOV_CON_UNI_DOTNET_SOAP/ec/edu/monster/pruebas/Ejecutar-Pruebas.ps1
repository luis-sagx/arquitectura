$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & dotnet run --project .\PruebasClienteSoap.csproj
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba SOAP funcional del cliente móvil.' }
} finally {
    Pop-Location
}
