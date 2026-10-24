# CLI_MOV_CON_UNI_DOTNET_REST (.NET MAUI)

Aplicación móvil en C# con .NET MAUI 10 para consumir la API REST (ASP.NET Core) de conversión de unidades.
Sigue la misma arquitectura MVC y las mismas pantallas que los demás clientes móviles.
El proyecto compila para **Android**; **iOS** está configurado pero solo compila en una Mac.

## Requisitos

- Visual Studio 2022/2026 con la carga de trabajo **Desarrollo de .NET MAUI**, o el .NET SDK 10 con
  `dotnet workload install maui-android`
- Android SDK con la plataforma **API 36** (Visual Studio la instala; por consola:
  `dotnet build -t:InstallAndroidDependencies -f net10.0-android -p:AcceptAndroidSDKLicenses=true`)
- El servidor `WS_CON_UNI_DOTNET_REST` iniciado (Kestrel, puerto 5111)
- Usuario y clave iniciales del servidor: `Monster` / `Monster9`

## Ruta de compilación

La carpeta del repositorio tiene una ruta larga y la herramienta de recursos de Android (aapt2) falla con rutas
de más de 260 caracteres. Por eso `Directory.Build.props` manda los archivos intermedios (`obj`) a
`%LOCALAPPDATA%\obj-cli-mov-dotnet-rest`. El APK sigue quedando en `bin/` de este proyecto.

## Ejecutar en Visual Studio

1. Abrir `CLI_MOV_CON_UNI_DOTNET_REST.csproj`.
2. En la barra superior elegir **Android Emulator** (o un celular con depuración USB) y presionar **▶**.

## Generar el APK

```
dotnet publish -c Release -f net10.0-android -o publicar
```

El APK firmado queda en `publicar/ec.edu.monster.dotnetrest-Signed.apk`. Se instala con
`adb install -r publicar/ec.edu.monster.dotnetrest-Signed.apk` o copiándolo al celular. Pesa unos 28 MB
porque incluye el runtime de .NET.

## iOS (iPhone)

MAUI usa el mismo código para iOS, pero Apple solo permite compilar apps de iOS con Xcode en macOS:

- En una Mac con Xcode y `dotnet workload install maui-ios`: `dotnet build -f net10.0-ios`.
- En Windows, Visual Studio puede compilar para iOS enlazado a una Mac de la red
  (*Pair to Mac*), agregando `-p:IncluirIOS=true` o quitando la condición de `TargetFrameworks`.
- Para instalarla en un iPhone real se necesita una cuenta de Apple Developer.

`Platforms/iOS/Info.plist` ya permite HTTP (`NSAllowsArbitraryLoads`) para el laboratorio.

## Dirección de la API

La URL está en `ec/edu/monster/Configuracion.cs` (`EndpointRest`) y siempre termina en `/api/ws-con-uni`:

| Dónde corre la app | URL |
|---|---|
| Emulador de Android | `http://10.0.2.2:5111/api/ws-con-uni` (configurada) |
| Simulador de iOS en una Mac | `http://localhost:5111/api/ws-con-uni` |
| Celular u otra red (ngrok) | `https://xxxx.ngrok-free.app/api` |

Para un celular físico, publique el servidor con `ngrok http 5111` y ponga en `Configuracion.cs` la URL de
ngrok seguida de `/api/ws-con-uni`.

## Arquitectura (MVC)

- `ec.edu.monster.modelo`: `Sesion`, `OpcionCatalogo` y `RespuestaRest` (datos normalizados).
- `ec.edu.monster.vista`: `LoginPage` y `ConversionesPage` (XAML), las interfaces `IVistaLogin` e
  `IVistaConversiones` que usan los controladores, y `Navegacion`.
- `ec.edu.monster.controlador`: `ControladorLogin` (acceso y recuperación de clave) y
  `ControladorConversiones` (catálogo, conversión, expiración y cierre de sesión). No dependen de MAUI.
- `ec.edu.monster.servicio`: `ClienteRest`, el adaptador REST (JSON sobre HTTP).

`ClienteRest` llama a la API con `HttpClient` y lee el JSON con `System.Text.Json` (`JsonDocument`):

- `POST autenticar` y `POST recuperar-clave` con cuerpo JSON.
- `GET categorias` y `GET unidades?categoria=...` con `Authorization: Bearer <token>`.
- `POST convertir` con la categoría, las unidades y el valor.
- Las respuestas 400 y 401 también traen el JSON de negocio (`AUTH-001`, `DATA-001`, …), que se muestra al usuario.

`Platforms/` contiene el arranque de cada plataforma, y `Resources/` el ícono y la pantalla de inicio.

## Funciones

- Iniciar sesión y recuperar la contraseña (con confirmación en un panel con campos ocultos).
- Consultar categorías y unidades: longitud, masa y temperatura.
- Convertir con punto o coma decimal, valores negativos y botón para intercambiar origen y destino.
- AUTH-002 o AUTH-003 cierran la sesión local y regresan al acceso.
- **Salir** o el botón *Atrás* cierran la sesión local. El servidor no ofrece una operación para revocar el token.
- El token solo vive en memoria; no se guardan credenciales.

La app usa el identificador `ec.edu.monster.dotnetrest`, así que puede instalarse junto a los demás
clientes móviles.
