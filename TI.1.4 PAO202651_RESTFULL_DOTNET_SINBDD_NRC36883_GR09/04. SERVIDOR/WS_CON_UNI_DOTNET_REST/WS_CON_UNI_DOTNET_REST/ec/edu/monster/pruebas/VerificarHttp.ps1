# Verifica por HTTP los casos límite de la API REST en ejecución.
# Uso: powershell -NoProfile -ExecutionPolicy Bypass -File .\ec\edu\monster\pruebas\VerificarHttp.ps1 [-BaseUrl URL]
param(
    [string]$BaseUrl = $(if ($env:REST_API_BASE) { $env:REST_API_BASE } else { 'http://localhost:5111/api/ws-con-uni' })
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http
$cliente = New-Object System.Net.Http.HttpClient

function Enviar($metodo, $ruta, $cuerpo, $token) {
    $peticion = New-Object System.Net.Http.HttpRequestMessage ([System.Net.Http.HttpMethod]::new($metodo)), "$BaseUrl/$ruta"
    if ($null -ne $cuerpo) {
        $peticion.Content = New-Object System.Net.Http.StringContent $cuerpo, ([System.Text.Encoding]::UTF8), 'application/json'
    }
    if ($token) {
        $peticion.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue 'Bearer', $token
    }
    $respuesta = $cliente.SendAsync($peticion).GetAwaiter().GetResult()
    $texto = $respuesta.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    return @{ Estado = [int]$respuesta.StatusCode; Cuerpo = ($texto | ConvertFrom-Json); Respuesta = $respuesta }
}

function Comprobar($resultado, $estado, $codigo, $descripcion) {
    if ($resultado.Estado -ne $estado -or $resultado.Cuerpo.codigo -ne $codigo) {
        throw "$descripcion debe responder HTTP $estado con $codigo; se recibió HTTP $($resultado.Estado) con '$($resultado.Cuerpo.codigo)'"
    }
    Write-Output "OK  $descripcion -> HTTP $estado $codigo"
}

try {
    Comprobar (Enviar 'POST' 'autenticar' '' $null) 401 'AUTH-001' 'Inicio de sesión vacío'
    Comprobar (Enviar 'POST' 'recuperar-clave' '' $null) 401 'AUTH-001' 'Recuperación de clave vacía'

    $sinToken = Enviar 'GET' 'categorias' $null $null
    Comprobar $sinToken 401 'AUTH-002' 'Catálogo sin token'
    if (-not $sinToken.Respuesta.Headers.WwwAuthenticate.ToString().Contains('Bearer')) {
        throw 'Las respuestas 401 deben anunciar el esquema Bearer'
    }
    Write-Output 'OK  La respuesta 401 anuncia el esquema Bearer'

    $login = Enviar 'POST' 'autenticar' '{"usuario":"Monster","clave":"Monster9"}' $null
    Comprobar $login 200 'OK' 'Inicio de sesión con Monster'
    Comprobar (Enviar 'POST' 'convertir' '' $login.Cuerpo.token) 400 'DATA-001' 'Conversión vacía'

    Write-Output "Verificación HTTP correcta en $BaseUrl"
}
finally {
    $cliente.Dispose()
}
