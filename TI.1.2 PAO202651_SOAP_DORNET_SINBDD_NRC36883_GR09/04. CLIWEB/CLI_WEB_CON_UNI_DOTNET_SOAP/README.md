# Cliente web .NET MVC de conversión de unidades

Aplicación web ASP.NET Core MVC (.NET 10) que consume el servicio SOAP WCF del proyecto
`04. SERVIDOR/WCF_CON_UNI_DOTNET_SOAP`. Tiene las mismas páginas, estilos y reglas que el cliente web
Java (`CLI_WEB_CON_UNI_JAVA_SOAP`) y sigue la estructura MVC de los demás clientes:

- `ec/edu/monster/modelo`: `RespuestaSoap`, `OpcionCatalogo` y los modelos de cada página (`ModelosVista.cs`).
- `ec/edu/monster/ws`: contrato WCF (`IWSConUni` y `RespuestaWcf`), igual al del cliente de consola .NET.
- `ec/edu/monster/servicio`: `ClienteSoap`, adaptador con `ChannelFactory` y `BasicHttpBinding` (SOAP 1.1).
- `ec/edu/monster/controlador`: `AccesoController` (login, recuperar clave, salir), `ConversorController`,
  `InicioController` y el filtro de sesión (`SesionWeb.cs`).
- `ec/edu/monster/vista`: vistas Razor (`.cshtml`).
- `wwwroot/css/estilos.css`: la misma hoja de estilos del cliente web Java.

## Requisitos y ejecución

- .NET SDK 10 (o Visual Studio 2022/2026 con **Desarrollo de ASP.NET y web**).
- El servidor WCF iniciado desde Visual Studio (IIS Express, `http://localhost:14028/WSConUni.svc`).

En Visual Studio: abrir `CLI_WEB_CON_UNI_DOTNET_SOAP.csproj` y presionar **▶**. Por consola:

```
dotnet run
```

y abrir `http://localhost:5080`. Usuario y clave iniciales: `monster` / `monster123`.

## Dirección del servicio

Se configura en `appsettings.json`:

```json
"Conversor": { "EndpointSoap": "http://localhost:14028/WSConUni.svc" }
```

Como el cliente web corre en el mismo equipo que IIS Express, `localhost` funciona sin ngrok. Para usar
un servidor WCF de otro equipo, poner su URL pública (por ejemplo la de ngrok seguida de `/WSConUni.svc`)
o definir la variable de entorno `Conversor__EndpointSoap`.

## Rutas

| Ruta | Función |
|---|---|
| `/` | Redirige al acceso o al conversor según haya sesión. |
| `GET/POST /login` | Inicio de sesión. |
| `GET/POST /recuperar-clave` | Recuperación de clave con confirmación. |
| `GET /conversor?categoria=...` | Catálogo y unidades de la categoría. |
| `POST /conversor` | Conversión en el servidor. |
| `POST /salir` | Cierra la sesión local. |

## Seguridad y sesión

- El token del servicio se guarda en la sesión HTTP (cookie `HttpOnly`, `SameSite=Strict`, 15 minutos).
- Todos los formularios POST llevan token antifalsificación (CSRF); un POST sin él responde 400.
- Razor escapa los valores mostrados.
- Si el servidor responde `AUTH-002` o `AUTH-003`, se cierra la sesión y se vuelve al acceso con el aviso.
