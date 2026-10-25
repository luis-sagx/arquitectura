# CLI_MOV_CON_UNI_JAVA_REST (REST)

Aplicación Android en Java, con arquitectura MVC, para consumir el API REST `WS_CON_UNI_JAVA_REST`.
Tiene la misma estructura y pantallas que el cliente móvil de `TI.1.1` (SOAP Java); cambia el
adaptador de servicio, que habla HTTP/JSON en lugar de SOAP.

## Requisitos

- Android Studio (incluye el SDK de Android y Gradle), o NetBeans con el Android SDK (ver abajo)
- Un emulador Android 8.0 (API 26) o superior, o un celular con depuración USB
- Servidor Payara con `WS_CON_UNI_JAVA_REST` desplegado

## Abrir y ejecutar

1. En Android Studio: **File > Open** y seleccionar la carpeta `04. CLIMOV/CLI_MOV_CON_UNI_JAVA_REST`.
2. Esperar a que termine el **Gradle Sync**.
3. Elegir un emulador o un celular y presionar **Run ▶**.
4. Usuario y clave iniciales del servidor: `Monster` / `Monster9`.

## Abrir y ejecutar en NetBeans

NetBeans abre el proyecto como proyecto Gradle y compila el APK con el Gradle wrapper incluido.
Se necesita JDK 21 y el Android SDK (`platforms;android-35`, `build-tools` y `platform-tools`).

1. Crear `local.properties` junto a `settings.gradle` con
   `sdk.dir=C\:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk`, o definir la variable `ANDROID_HOME`.
2. **File > Open Project** y seleccionar `04. CLIMOV/CLI_MOV_CON_UNI_JAVA_REST`.
3. Clic derecho > **Run Gradle Task** > `assembleDebug` (o `gradlew.bat assembleDebug`).
4. El APK queda en `app/build/outputs/apk/debug/ConversorMonsterREST-debug.apk`.
   Se instala con `adb install -r` o copiándolo al celular.

## Dirección del servicio

La URL base del API está en `app/src/main/res/values/config.xml` (`endpoint_rest`) y termina en `/api`:

| Dónde corre la app | URL |
|---|---|
| Emulador de Android Studio | `http://10.0.2.2:8080/WS_CON_UNI_JAVA_REST/api` (configurada) |
| Celular físico en la misma red Wi-Fi | `http://IP_DEL_COMPUTADOR:8080/WS_CON_UNI_JAVA_REST/api` |
| Celular u otra red (ngrok) | `https://xxxx.ngrok-free.app/WS_CON_UNI_JAVA_REST/api` |

Si Payara despliega el WAR con otro contexto (por ejemplo `WS_CON_UNI_JAVA_REST-1.0-SNAPSHOT`),
cambiar esa parte de la URL. Para un celular físico en la misma red, permitir el puerto 8080 en el
Firewall de Windows.

## Arquitectura (MVC)

- `ec.edu.monster.modelo`: `Sesion`, `OpcionCatalogo` y `RespuestaRest` (datos normalizados).
- `ec.edu.monster.vista`: `LoginActivity` y `ConversionesActivity`, más las interfaces `VistaLogin` y
  `VistaConversiones` que usan los controladores. Los layouts están en `res/layout`.
- `ec.edu.monster.controlador`: `ControladorLogin` (acceso y recuperación de clave) y
  `ControladorConversiones` (catálogo, conversión, expiración y cierre de sesión).
- `ec.edu.monster.servicio`: `ClienteRest` (adaptador HTTP/JSON) y `EjecutorAndroid` (hilo de fondo para la red).

`ClienteRest` usa `HttpURLConnection` y `org.json`, incluidos en Android, sin librerías extra:

| Operación | Petición |
|---|---|
| Iniciar sesión | `POST /auth/login` con `{"usuario","clave"}` |
| Recuperar clave | `POST /auth/recover` con `{"usuario","nuevaClave"}` |
| Categorías | `GET /catalog/categories` |
| Unidades | `GET /catalog/categories/{categoria}/units` |
| Convertir | `POST /conversions` con `{"categoria","unidadOrigen","unidadDestino","valor"}` |

Las operaciones protegidas envían `Authorization: Bearer <token>`. Los errores de negocio llegan con
HTTP 400 o 401 y el mismo JSON (`estado`, `codigo`, `mensaje`), así que se muestran igual que en los
otros clientes.

## Funciones

- Iniciar sesión y recuperar la contraseña (con confirmación).
- Consultar categorías y unidades: longitud, masa y temperatura.
- Convertir con punto o coma decimal, valores negativos y botón para intercambiar origen y destino.
- AUTH-002 o AUTH-003 cierran la sesión local y regresan al acceso.
- **Salir** o el botón *Atrás* cierran la sesión local. El servidor no ofrece una operación para revocar el token.
- El token solo vive en memoria; no se guardan credenciales.

La app usa el identificador `ec.edu.monster.rest`, así que puede instalarse junto a los clientes
móviles SOAP Java y WCF. `usesCleartextTraffic` está habilitado porque Payara se usa por HTTP en el
laboratorio.
