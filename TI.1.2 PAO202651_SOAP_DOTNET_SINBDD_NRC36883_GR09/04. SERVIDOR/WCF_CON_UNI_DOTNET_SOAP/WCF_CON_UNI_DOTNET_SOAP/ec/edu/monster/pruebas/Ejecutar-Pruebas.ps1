$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..\..'))
$compiler = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
if (-not (Test-Path -LiteralPath $compiler)) {
    $compiler = 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe'
}
if (-not (Test-Path -LiteralPath $compiler)) {
    throw 'No se encontro el compilador de .NET Framework (csc.exe).'
}

$output = Join-Path $env:TEMP ("PruebaServicioUnidades-{0}.exe" -f $PID)
$sources = @(Get-ChildItem -LiteralPath $PSScriptRoot -Filter '*.cs' | ForEach-Object FullName)
$sources += Join-Path $projectRoot 'ec\edu\monster\modelos\Respuesta.cs'
$sources += Join-Path $projectRoot 'ec\edu\monster\servicios\ConversorUnidadesServices.cs'
$sources += Join-Path $projectRoot 'ec\edu\monster\controladores\ws\WSConUni.cs'
$sources = $sources | Where-Object { Test-Path -LiteralPath $_ }

try {
    & $compiler "/out:$output" /target:exe /r:System.Core.dll /r:System.Configuration.dll /r:System.Runtime.Serialization.dll /r:System.ServiceModel.dll /r:System.Web.Services.dll /r:System.Xml.dll $sources
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    & $output
    exit $LASTEXITCODE
}
finally {
    Remove-Item -LiteralPath $output -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath ($output + '.config') -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath ($output -replace '\.exe$', '.pdb') -Force -ErrorAction SilentlyContinue
}
