$base = if ($env:REST_API_BASE) { $env:REST_API_BASE } else { 'http://127.0.0.1:5123/api/ws-con-uni' }
$client = [System.Net.Http.HttpClient]::new()
function Send-EmptyPost($path, $token) {
    $request = [System.Net.Http.HttpRequestMessage]::new([System.Net.Http.HttpMethod]::Post, "$base/$path")
    $request.Content = [System.Net.Http.StringContent]::new('', [System.Text.Encoding]::UTF8, 'application/json')
    if ($token) { $request.Headers.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new('Bearer', $token) }
    $response = $client.Send($request)
    $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json -AsHashtable
    return @{ Response = $response; Body = $body }
}
try {
    $emptyLogin = Send-EmptyPost 'autenticar' $null
    if ([int]$emptyLogin.Response.StatusCode -ne 401 -or $emptyLogin.Body['codigo'] -ne 'AUTH-001') {
        throw "Empty login must return HTTP 401 with AUTH-001, got HTTP $([int]$emptyLogin.Response.StatusCode) and code '$($emptyLogin.Body['codigo'])'"
    }

    $emptyRecovery = Send-EmptyPost 'recuperar-clave' $null
    if ([int]$emptyRecovery.Response.StatusCode -ne 401 -or $emptyRecovery.Body['codigo'] -ne 'AUTH-001') {
        throw 'Empty password recovery must return HTTP 401 with AUTH-001'
    }

    $unauthorizedResponse = $client.GetAsync("$base/categorias").GetAwaiter().GetResult()
    $unauthorizedBody = $unauthorizedResponse.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json -AsHashtable
    if ([int]$unauthorizedResponse.StatusCode -ne 401 -or $unauthorizedBody['codigo'] -ne 'AUTH-002') {
        throw 'Missing token must return HTTP 401 with AUTH-002'
    }
    if (-not $unauthorizedResponse.Headers.WwwAuthenticate.ToString().Contains('Bearer')) {
        throw '401 responses must advertise the Bearer scheme'
    }

    $login = $client.PostAsync("$base/autenticar", [System.Net.Http.StringContent]::new('{"usuario":"monster","clave":"monster123"}', [System.Text.Encoding]::UTF8, 'application/json')).GetAwaiter().GetResult()
    $loginBody = $login.Content.ReadAsStringAsync().GetAwaiter().GetResult() | ConvertFrom-Json -AsHashtable
    $emptyConversion = Send-EmptyPost 'convertir' $loginBody['token']
    if ([int]$emptyConversion.Response.StatusCode -ne 400 -or $emptyConversion.Body['codigo'] -ne 'DATA-001') {
        throw "Empty conversion must return HTTP 400 with DATA-001, got HTTP $([int]$emptyConversion.Response.StatusCode) code [$($emptyConversion.Body["codigo"])] token-present=[$(-not [string]::IsNullOrWhiteSpace($loginBody["token"]))]"
    }

    'HTTP edge verification passed.'
}
finally {
    $client.Dispose()
}
