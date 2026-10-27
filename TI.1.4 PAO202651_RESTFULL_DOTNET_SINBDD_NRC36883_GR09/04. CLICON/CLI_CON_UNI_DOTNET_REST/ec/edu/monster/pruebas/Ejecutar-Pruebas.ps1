$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & dotnet run --project .\PruebasClienteRest.csproj
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba funcional del cliente REST.' }
} finally {
    Pop-Location
}

