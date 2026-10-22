# Cliente web .NET MVC de conversión de unidades

Aplicación web ASP.NET Core MVC (.NET 10) que consume la API REST del proyecto
`04. SERVIDOR/WS_CON_UNI_DOTNET_REST`. Tiene las mismas páginas, estilos y reglas que los demás clientes web
y sigue la misma estructura MVC:

- `ec/edu/monster/modelo`: `RespuestaRest`, `OpcionCatalogo` y los modelos de cada página (`ModelosVista.cs`).
- `ec/edu/monster/servicio`: `ClienteRest`, adaptador con `HttpClient` y `System.Text.Json` (token Bearer).
- `ec/edu/monster/controlador`: `AccesoController` (login, recuperar clave, salir), `ConversorController`,
  `InicioController` y el filtro de sesión (`SesionWeb.cs`).
- `ec/edu/monster/vista`: vistas Razor (`.cshtml`).
- `wwwroot/css/estilos.css`: la misma hoja de estilos de los demás clientes web.

## Requisitos y ejecución

- .NET SDK 10 (o Visual Studio 2022/2026 con **Desarrollo de ASP.NET y web**).
- El servidor REST iniciado (`http://localhost:5111/api/ws-con-uni`).

En Visual Studio: abrir `CLI_WEB_CON_UNI_DOTNET_REST.csproj` y presionar **▶**. Por consola:

```
dotnet run
```

y abrir `http://localhost:5080`. Usuario y clave iniciales: `monster` / `monster123`.

## Dirección de la API

Se configura en `appsettings.json`:

```json
"Conversor": { "EndpointRest": "http://localhost:5111/api/ws-con-uni" }
```

Como el cliente web corre en el mismo equipo que el servidor, `localhost` funciona sin ngrok. Para usar
un servidor de otro equipo, poner su URL pública (por ejemplo la de ngrok seguida de `/api/ws-con-uni`)
o definir la variable de entorno `Conversor__EndpointRest`.

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
