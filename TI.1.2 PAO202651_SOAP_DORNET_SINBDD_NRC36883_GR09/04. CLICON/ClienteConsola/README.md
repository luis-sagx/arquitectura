# Cliente de consola .NET para WSConUni

Cliente SOAP 1.1 para el servicio WCF de conversión de unidades. Conserva el flujo del cliente Java: inicio y cierre local de sesión, consulta de categorías y unidades, conversiones y recuperación de clave.

El código está organizado bajo `ec.edu.monster`: `consola`, `consola.controlador`, `consola.vista`, `cliente.ws`, `modelo` y `ws`.

## Ejecutar en Visual Studio

1. Abre la solución `04. SERVIDOR/WCF_CON_UNI_DOTNET_SOAP/WCF_CON_UNI_DOTNET_SOAP.slnx`.
2. Inicia el proyecto web `WCF_CON_UNI_DOTNET_SOAP` con F5 y confirma que el servicio esté disponible en `http://localhost:14028/WSConUni.svc`.
3. Ejecuta `ClienteConsola` desde Visual Studio. El usuario y clave iniciales son `monster` y `monster123`.

Si IIS Express muestra otro puerto, cambia `conversor.endpoint` en `App.config` o pasa la URL como primer argumento al ejecutar el cliente. También se acepta la URL terminada en `?wsdl`; el cliente la normaliza al endpoint `.svc`.

## Menú

- `1`: iniciar sesión.
- `2`: consultar categorías y unidades.
- `3`: convertir entre unidades de una misma categoría.
- `4`: cerrar la sesión local.
- `5`: recuperar la clave; el servidor invalida las sesiones activas.
- `0`: salir.

Las categorías, unidades, tokens y resultados vienen del servidor. La sesión y el endpoint se conservan localmente en memoria/configuración; el servidor no ofrece una operación SOAP para revocar un token al cerrar sesión localmente.

El contrato WCF de `ContratoWcf.cs` refleja la interfaz publicada por el servidor. Si cambian las operaciones o el esquema SOAP, actualiza ese contrato y `RespuestaWcf` junto con el servidor.
