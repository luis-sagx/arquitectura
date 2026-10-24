# CLI_MOV_CON_UNI_DOTNET_SOAP (.NET MAUI)

Aplicación móvil en C# con .NET MAUI 10 para consumir el servicio SOAP WCF `WSConUni.svc`.
Sigue la misma arquitectura MVC y las mismas pantallas que los clientes móviles de SOAP Java y REST.
El proyecto compila para **Android**; **iOS** está configurado pero solo compila en una Mac.

## Requisitos

- Visual Studio 2022/2026 con la carga de trabajo **Desarrollo de .NET MAUI**, o el .NET SDK 10 con
  `dotnet workload install maui-android`
- Android SDK con la plataforma **API 36** (Visual Studio la instala; por consola:
  `dotnet build -t:InstallAndroidDependencies -f net10.0-android -p:AcceptAndroidSDKLicenses=true`)
- El servicio `WCF_CON_UNI_DOTNET_SOAP` iniciado desde Visual Studio (IIS Express, puerto 14028)
- Usuario y clave iniciales del servidor: `Monster` / `Monster9`

## Ejecutar en Visual Studio

1. Abrir `CLI_MOV_CON_UNI_DOTNET_SOAP.csproj`.
2. En la barra superior elegir **Android Emulator** (o un celular con depuración USB) y presionar **▶**.

## Generar el APK

```
dotnet publish -c Release -f net10.0-android -o publicar
```

El APK firmado queda en `publicar/ec.edu.monster.wcf-Signed.apk`. Se instala con
`adb install -r publicar/ec.edu.monster.wcf-Signed.apk` o copiándolo al celular. Pesa unos 28 MB
porque incluye el runtime de .NET.

## iOS (iPhone)

MAUI usa el mismo código para iOS, pero Apple solo permite compilar apps de iOS con Xcode en macOS:

- En una Mac con Xcode y `dotnet workload install maui-ios`: `dotnet build -f net10.0-ios`.
- En Windows, Visual Studio puede compilar para iOS enlazado a una Mac de la red
  (*Pair to Mac*), agregando `-p:IncluirIOS=true` o quitando la condición de `TargetFrameworks`.
- Para instalarla en un iPhone real se necesita una cuenta de Apple Developer.

`Platforms/iOS/Info.plist` ya permite HTTP (`NSAllowsArbitraryLoads`) para el laboratorio.

## Dirección del servicio

La URL está en `ec/edu/monster/Configuracion.cs` (`EndpointSoap`):

| Dónde corre la app | URL |
|---|---|
| Emulador de Android | `http://10.0.2.2:14028/WSConUni.svc` (configurada) |
| Simulador de iOS en una Mac | `http://localhost:14028/WSConUni.svc` |
| Celular u otra red (ngrok) | `https://xxxx.ngrok-free.app/WSConUni.svc` |

IIS Express solo acepta peticiones con el nombre de host `localhost`. Desde el emulador o un celular
llegan con otro host y responde **HTTP 400 Bad Request - Invalid Hostname**. Para publicarlo:

```
ngrok http 14028 --host-header="localhost:14028"
```

y poner en `Configuracion.cs` la URL de ngrok seguida de `/WSConUni.svc`.

## Arquitectura (MVC)

- `ec.edu.monster.modelo`: `Sesion`, `OpcionCatalogo` y `RespuestaSoap` (datos normalizados).
- `ec.edu.monster.vista`: `LoginPage` y `ConversionesPage` (XAML), las interfaces `IVistaLogin` e
  `IVistaConversiones` que usan los controladores, y `Navegacion`.
- `ec.edu.monster.controlador`: `ControladorLogin` (acceso y recuperación de clave) y
  `ControladorConversiones` (catálogo, conversión, expiración y cierre de sesión). No dependen de MAUI.
- `ec.edu.monster.servicio`: `ClienteSoap`, el adaptador SOAP 1.1.

`ClienteSoap` arma el sobre con `HttpClient` y LINQ to XML en lugar de un proxy WCF
(`ChannelFactory`), porque los proxys dinámicos de WCF no funcionan en iOS. Sigue el contrato del WSDL:

- `SOAPAction: "http://ws.controladores.monster.edu.ec/WSConUni/<operacion>"` en cada llamada.
- Parámetros calificados con el namespace `http://ws.controladores.monster.edu.ec/`.
- La respuesta llega en `<operacion>Result`; un `resultado` vacío viene como `xsi:nil`.

`Platforms/` contiene el arranque de cada plataforma, y `Resources/` el ícono y la pantalla de inicio.

## Funciones

- Iniciar sesión y recuperar la contraseña (con confirmación en un panel con campos ocultos).
- Consultar categorías y unidades: longitud, masa y temperatura.
- Convertir con punto o coma decimal, valores negativos y botón para intercambiar origen y destino.
- AUTH-002 o AUTH-003 cierran la sesión local y regresan al acceso.
- **Salir** o el botón *Atrás* cierran la sesión local. El servidor no ofrece una operación para revocar el token.
- El token solo vive en memoria; no se guardan credenciales.

La app usa el identificador `ec.edu.monster.wcf`, así que puede instalarse junto a los clientes
móviles SOAP Java y REST.
