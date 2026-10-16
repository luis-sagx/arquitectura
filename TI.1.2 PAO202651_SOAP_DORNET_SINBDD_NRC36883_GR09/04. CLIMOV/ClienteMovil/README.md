# ClienteMovil (WCF)

Aplicación Android en Java, con arquitectura MVC, para consumir el servicio SOAP WCF `WSConUni.svc`.
Tiene la misma estructura y pantallas que el cliente móvil de `TI.1.1` (SOAP Java); solo cambia el
adaptador SOAP y la dirección del servicio.

## Requisitos

- Android Studio (incluye el SDK de Android y Gradle), o NetBeans con el Android SDK (ver abajo)
- Un emulador Android 8.0 (API 26) o superior, o un celular con depuración USB
- El servicio `WCF_CON_UNI_DOTNET_SOAP` iniciado desde Visual Studio (IIS Express, puerto 14028)

## Abrir y ejecutar

1. En Android Studio: **File > Open** y seleccionar la carpeta `04. CLIMOV/ClienteMovil`.
2. Esperar a que termine el **Gradle Sync**.
3. Elegir un emulador o un celular y presionar **Run ▶**.
4. Usuario y clave iniciales del servidor: `monster` / `monster123`.

## Abrir y ejecutar en NetBeans

NetBeans abre el proyecto como proyecto Gradle y compila el APK con el Gradle wrapper incluido.
Se necesita JDK 21 y el Android SDK (`platforms;android-35`, `build-tools` y `platform-tools`).

1. Crear `local.properties` junto a `settings.gradle` con
   `sdk.dir=C\:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk`, o definir la variable `ANDROID_HOME`.
2. **File > Open Project** y seleccionar `04. CLIMOV/ClienteMovil`.
3. Clic derecho > **Run Gradle Task** > `assembleDebug` (o `gradlew.bat assembleDebug`).
4. El APK queda en `app/build/outputs/apk/debug/ConversorMonsterWCF-debug.apk`.
   Se instala con `adb install -r` o copiándolo al celular.

## Dirección del servicio

La URL está en `app/src/main/res/values/config.xml` (`endpoint_soap`):

| Dónde corre la app | URL |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:14028/WSConUni.svc` (configurada) |
| Celular u otra red (ngrok) | `https://xxxx.ngrok-free.app/WSConUni.svc` |

IIS Express solo acepta peticiones con el nombre de host `localhost`. Desde el emulador o el celular
llegan con otro host (`10.0.2.2`, la IP del equipo) y responde **HTTP 400 Bad Request - Invalid Hostname**.
La forma más simple de publicarlo es ngrok reescribiendo el host:

```
ngrok http 14028 --host-header="localhost:14028"
```

y poner en `config.xml` la URL que entrega ngrok seguida de `/WSConUni.svc`.

## Arquitectura (MVC)

- `ec.edu.monster.modelo`: `Sesion`, `OpcionCatalogo` y `RespuestaSoap` (datos normalizados).
- `ec.edu.monster.vista`: `LoginActivity` y `ConversionesActivity`, más las interfaces `VistaLogin` y
  `VistaConversiones` que usan los controladores. Los layouts están en `res/layout`.
- `ec.edu.monster.controlador`: `ControladorLogin` (acceso y recuperación de clave) y
  `ControladorConversiones` (catálogo, conversión, expiración y cierre de sesión).
- `ec.edu.monster.servicio`: `ClienteSoap` (adaptador SOAP) y `EjecutorAndroid` (hilo de fondo para la red).

Android no incluye WCF, por eso `ClienteSoap` arma el sobre SOAP 1.1 y lee la respuesta con
`HttpURLConnection` y DOM, según el WSDL del servidor (namespace `http://ws.controladores.monster.edu.ec/`).
Diferencias con el servicio JAX-WS:

- Cada operación se envía con `SOAPAction: "http://ws.controladores.monster.edu.ec/WSConUni/<operacion>"`.
- Los parámetros van calificados con el namespace del contrato (`elementFormDefault="qualified"`).
- La respuesta llega en `<operacion>Result` en lugar de `<return>`, y un `resultado` vacío viene como `xsi:nil`.

## Funciones

- Iniciar sesión y recuperar la contraseña (con confirmación).
- Consultar categorías y unidades: longitud, masa y temperatura.
- Convertir con punto o coma decimal, valores negativos y botón para intercambiar origen y destino.
- AUTH-002 o AUTH-003 cierran la sesión local y regresan al acceso.
- **Salir** o el botón *Atrás* cierran la sesión local. El servidor no ofrece una operación para revocar el token.
- El token solo vive en memoria; no se guardan credenciales.

La app usa el identificador `ec.edu.monster.wcf`, así que puede instalarse junto a los clientes
móviles SOAP Java y REST. `usesCleartextTraffic` está habilitado porque IIS Express se usa por HTTP
en el laboratorio.
