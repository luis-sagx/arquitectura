# Cliente de escritorio Monster

Aplicación C# WinForms que consume el servicio SOAP WCF de conversión de unidades. El código usa la estructura `ec.edu.monster.vista`, `controlador`, `modelo` y `servicio`, y mantiene el token de sesión solo en memoria.

## Requisitos

- Windows y .NET Framework 4.7.2.
- Iniciar el servidor en `04. SERVIDOR/WCF_CON_UNI_DOTNET_SOAP` antes de usar operaciones SOAP.
- Visual Studio con el componente de desarrollo de escritorio de .NET Framework, o el SDK de .NET y las herramientas de referencia de .NET Framework.

## Dirección del servicio

El endpoint se define en `App.config` mediante `conversor.endpoint`. Su valor inicial es:

```text
http://localhost:14028/WSConUni.svc
```

Si IIS Express asigna otro puerto o se usa otro host, cambia ese valor. El cliente acepta HTTP o HTTPS. Para redes no confiables, configura HTTPS.

El proxy SOAP se generó desde el WSDL local y los esquemas guardados en `Connected Services/WSConUni`. Las importaciones usan rutas relativas para que las herramientas puedan resolver los archivos sin iniciar el servidor. No se debe editar `Reference.cs` manualmente.

Para regenerarlo con las herramientas de .NET Framework, abre una consola en `Connected Services/WSConUni` y ejecuta `SvcUtil.exe /namespace:*,ec.edu.monster.clientesws /noConfig /out:Reference.cs WSConUni.wsdl`; el WSDL importa el esquema `WSConUni_schema1.xsd`. Si se usa el WSDL alterno `WSConUni_2.wsdl`, primero resuelve su importación relativa a `WSConUni.wsdl`.

## Compilar y ejecutar

Desde esta carpeta se puede compilar con Visual Studio o con:

```powershell
dotnet build .\CLI_ESC_CON_UNI_DOTNET_SOAP.csproj --no-restore -p:GenerateResourceMSBuildRuntime=CurrentRuntime -p:GenerateResourceMSBuildArchitecture=CurrentArchitecture
```

Inicia el servidor WCF, confirma el endpoint y ejecuta el proyecto `CLI_ESC_CON_UNI_DOTNET_SOAP` desde Visual Studio. El usuario y clave locales predeterminados del servidor son `Monster` y `Monster9`; puedes cambiarlos en la configuración del servidor.

## Pruebas

Desde esta carpeta, ejecuta el harness de escritorio con:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ec\edu\monster\pruebas\Ejecutar-Pruebas.ps1
```

El script compila la aplicación y ejecuta las comprobaciones de dimensiones, paleta, bordes redondeados y ciclo de vida singleton de las ventanas.

## Funciones

- `autenticar`: inicia sesión y conserva en memoria el token y su vencimiento.
- `recuperarClave`: cambia la clave del usuario y el servidor invalida las sesiones activas.
- `obtenerCategorias` / `obtenerUnidades`: carga el catálogo desde el servidor.
- `convertir`: envía el valor, categoría y unidades seleccionadas al servicio.

El cliente no realiza conversiones localmente ni guarda contraseñas o tokens. Si el token caduca o queda invalidado, se cierra la sesión y se regresa al acceso.
