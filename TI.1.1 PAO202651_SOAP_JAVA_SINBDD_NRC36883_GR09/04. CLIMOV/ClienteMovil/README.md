# ClienteMovil

Aplicación Android en Java, con arquitectura MVC, para consumir el servicio SOAP `WSConUni`.

## Requisitos

- Android Studio (incluye el SDK de Android y Gradle)
- Un emulador Android 8.0 (API 26) o superior, o un celular con depuración USB
- Servidor Payara con `WS_CON_UNI_JAVA_SOAP` desplegado

## Abrir y ejecutar

1. En Android Studio: **File > Open** y seleccionar la carpeta `04. CLIMOV/ClienteMovil`.
2. Esperar a que termine el **Gradle Sync**. La primera vez descarga Gradle 8.9 y las dependencias.
   Si Android Studio avisa que falta el *Gradle wrapper*, aceptar que lo genere.
3. Elegir un emulador o un celular y presionar **Run ▶**.
4. Usuario y clave iniciales del servidor: `monster` / `monster123`.

## Dirección del servicio

La URL está en `app/src/main/res/values/config.xml` (`endpoint_soap`):

| Dónde corre la app | URL |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:8080/WS_CON_UNI_JAVA_SOAP/WSConUni` (configurada) |
| Celular físico en la misma red Wi-Fi | `http://IP_DEL_COMPUTADOR:8080/WS_CON_UNI_JAVA_SOAP/WSConUni` |

Dentro del emulador, `localhost` es el propio emulador; `10.0.2.2` apunta al computador.
Para un celular físico, cambiar la URL en `config.xml`, obtener la IP con `ipconfig` y permitir
el puerto 8080 en el Firewall de Windows.

## Arquitectura (MVC)

- `ec.edu.monster.modelo`: `Sesion`, `OpcionCatalogo` y `RespuestaSoap` (datos normalizados).
- `ec.edu.monster.vista`: `LoginActivity` y `ConversionesActivity`, más las interfaces `VistaLogin` y
  `VistaConversiones` que usan los controladores. Los layouts están en `res/layout`.
- `ec.edu.monster.controlador`: `ControladorLogin` (acceso y recuperación de clave) y
  `ControladorConversiones` (catálogo, conversión, expiración y cierre de sesión).
- `ec.edu.monster.servicio`: `ClienteSoap` (adaptador SOAP) y `EjecutorAndroid` (hilo de fondo para la red).

Android no incluye JAX-WS, por eso `ClienteSoap` arma el sobre SOAP 1.1 y lee la respuesta con
`HttpURLConnection` y DOM, según el WSDL del servidor (namespace `http://ws.controladores.monster.edu.ec/`).
Los controladores no usan clases de Android; se comunican con la pantalla mediante las interfaces de vista.

## Funciones

- Iniciar sesión y recuperar la contraseña (con confirmación).
- Consultar categorías y unidades: longitud, masa y temperatura.
- Convertir con punto o coma decimal, valores negativos y botón para intercambiar origen y destino.
- AUTH-002 o AUTH-003 cierran la sesión local y regresan al acceso.
- **Salir** o el botón *Atrás* cierran la sesión local. El servidor no ofrece una operación para revocar el token.
- El token solo vive en memoria; no se guardan credenciales.

`usesCleartextTraffic` está habilitado porque Payara se usa por HTTP en el laboratorio.
Fuera de una red de confianza conviene publicar el servicio con HTTPS.
