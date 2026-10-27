$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & dotnet run --project .\\PruebasClienteWcf.csproj
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba SOAP funcional del cliente web.' }
} finally {
    Pop-Location
}

