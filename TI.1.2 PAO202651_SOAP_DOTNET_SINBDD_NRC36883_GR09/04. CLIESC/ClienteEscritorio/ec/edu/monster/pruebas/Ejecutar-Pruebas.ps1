$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..\..'))
$compiler = 'C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe'
if (-not (Test-Path -LiteralPath $compiler)) {
    $compiler = 'C:\Windows\Microsoft.NET\Framework\v4.0.30319\csc.exe'
}
if (-not (Test-Path -LiteralPath $compiler)) {
    throw 'No se encontro el compilador de .NET Framework (csc.exe).'
}

& dotnet build (Join-Path $projectRoot 'ClienteEscritorio.csproj') --no-restore `
    -p:GenerateResourceMSBuildRuntime=CurrentRuntime `
    -p:GenerateResourceMSBuildArchitecture=CurrentArchitecture
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$clientAssembly = Join-Path $projectRoot 'bin\Debug\ClienteEscritorio.exe'
$temporaryRoot = [IO.Path]::GetFullPath($env:TEMP).TrimEnd('\')
$temporaryDirectory = Join-Path $temporaryRoot ("PruebaClienteEscritorio-{0}" -f [Guid]::NewGuid().ToString('N'))
$testOutput = Join-Path $temporaryDirectory 'PruebaClienteEscritorio.exe'
$testAssembly = Join-Path $temporaryDirectory 'ClienteEscritorio.exe'
$sources = @(
    (Join-Path $PSScriptRoot 'PruebaClienteEscritorio.cs')
)

try {
    New-Item -ItemType Directory -Path $temporaryDirectory -ErrorAction Stop | Out-Null
    Copy-Item -LiteralPath $clientAssembly -Destination $testAssembly -Force
    & $compiler "/out:$testOutput" /target:exe /r:System.dll /r:System.Core.dll /r:System.Drawing.dll /r:System.Windows.Forms.dll "/r:$testAssembly" $sources
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    & $testOutput
    exit $LASTEXITCODE
}
finally {
    $resolvedTemporaryDirectory = [IO.Path]::GetFullPath($temporaryDirectory)
    $expectedParent = [IO.Path]::GetFullPath($temporaryRoot).TrimEnd('\') + '\'
    if ($resolvedTemporaryDirectory.StartsWith($expectedParent, [StringComparison]::OrdinalIgnoreCase) -and
        (Split-Path -Leaf $resolvedTemporaryDirectory).StartsWith('PruebaClienteEscritorio-', [StringComparison]::OrdinalIgnoreCase) -and
        (Test-Path -LiteralPath $resolvedTemporaryDirectory)) {
        Remove-Item -LiteralPath $resolvedTemporaryDirectory -Recurse -Force -ErrorAction SilentlyContinue
    }
}
