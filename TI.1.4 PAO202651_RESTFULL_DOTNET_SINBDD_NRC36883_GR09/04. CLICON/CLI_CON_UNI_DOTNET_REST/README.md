# Cliente de consola .NET para la API REST

Cliente REST (JSON sobre HTTP) para el servidor ASP.NET Core de conversión de unidades. Conserva el flujo de los demás clientes de consola: inicio y cierre local de sesión, consulta de categorías y unidades, conversiones y recuperación de clave.

El código está organizado bajo `ec.edu.monster`: `consola`, `consola.controlador`, `consola.vista`, `cliente.rest` y `modelo`. Usa .NET Framework 4.7.2 sin paquetes externos: `HttpWebRequest` para HTTP y `DataContractJsonSerializer` para el JSON.

## Ejecutar en Visual Studio

1. Abre la solución `04. SERVIDOR/WS_CON_UNI_DOTNET_REST/WS_CON_UNI_DOTNET_REST.slnx`.
2. Inicia el proyecto `WS_CON_UNI_DOTNET_REST` y confirma que la API responda en `http://localhost:5111/api/ws-con-uni`.
3. Ejecuta `CLI_CON_UNI_DOTNET_REST`. El usuario y clave iniciales son `monster` y `monster123`.

Si el servidor usa otro puerto o equipo, cambia `conversor.endpoint` en `App.config` o pasa la URL de la API (terminada en `/api/ws-con-uni`) como primer argumento al ejecutar el cliente.

## Menú

- `1`: iniciar sesión.
- `2`: consultar categorías y unidades.
- `3`: convertir entre unidades de una misma categoría.
- `4`: cerrar la sesión local.
- `5`: recuperar la clave; el servidor invalida las sesiones activas.
- `0`: salir.

La consola limpia la pantalla al cambiar de sección y entre los cuatro pasos de conversión: categoría, origen, destino y cantidad. Cada paso muestra las selecciones anteriores. Los resultados y errores permanecen visibles hasta presionar Enter; elegir `0` en una selección vuelve directamente al menú. Al redirigir la entrada o salida se conserva la salida completa y se omiten las pausas.

Las categorías, unidades, tokens y resultados vienen del servidor. El token viaja en `Authorization: Bearer <token>` y solo vive en memoria; el servidor no ofrece una operación para revocarlo al cerrar sesión localmente.

`ClienteRest` traduce cada operación a su ruta (`POST autenticar`, `GET categorias`, `GET unidades?categoria=`, `POST convertir`, etc.). Si el servidor responde 400 o 401, igual lee el JSON y muestra el código de negocio (`AUTH-001`, `DATA-001`, …).
