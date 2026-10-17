# CLI_MOV_CON_UNI_JAVA_SOAP

Aplicación Android en Java, con arquitectura MVC, para consumir el servicio SOAP `WSConUni`.

## Requisitos

- Android Studio (incluye el SDK de Android y Gradle)
- Un emulador Android 8.0 (API 26) o superior, o un celular con depuración USB
- Servidor Payara con `WS_CON_UNI_JAVA_SOAP` desplegado

## Abrir y ejecutar

1. En Android Studio: **File > Open** y seleccionar la carpeta `04. CLIMOV/CLI_MOV_CON_UNI_JAVA_SOAP`.
2. Esperar a que termine el **Gradle Sync**. La primera vez descarga Gradle 8.9 y las dependencias.
   Si Android Studio avisa que falta el *Gradle wrapper*, aceptar que lo genere.
3. Elegir un emulador o un celular y presionar **Run ▶**.
4. Usuario y clave iniciales del servidor: `monster` / `monster123`.

## Abrir y ejecutar en NetBeans

NetBeans no tiene soporte para Android, pero abre el proyecto como proyecto Gradle y compila el APK
con el Gradle wrapper incluido (`gradlew`, Gradle 8.9). Se necesita JDK 21 y el Android SDK
(`platforms;android-35`, `build-tools` y `platform-tools`), ya sea el que instala Android Studio o las
*command line tools* con `sdkmanager`.

1. Indicar dónde está el SDK: crear `local.properties` junto a `settings.gradle` con
   `sdk.dir=C\:\\Users\\USUARIO\\AppData\\Local\\Android\\Sdk`, o definir la variable `ANDROID_HOME`.
2. En NetBeans: **File > Open Project** y seleccionar `04. CLIMOV/CLI_MOV_CON_UNI_JAVA_SOAP` (aparece con el ícono de Gradle).
3. Clic derecho sobre el proyecto > **Run Gradle Task** > `assembleDebug`
   (o en una terminal: `gradlew.bat assembleDebug`).
4. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

Para probarlo:

- **Celular físico**: activar *Opciones de desarrollador > Depuración USB*, conectarlo y ejecutar
  `adb install -r app/build/outputs/apk/debug/app-debug.apk` (`adb` está en `SDK/platform-tools`).
  También se puede copiar el APK al celular e instalarlo permitiendo *orígenes desconocidos*.
  Antes de compilar, poner la IP del computador en `config.xml` (ver abajo).
- **Emulador**: listar los dispositivos con `SDK/emulator/emulator -list-avds`, iniciar uno con
  `emulator -avd NOMBRE` y luego usar el mismo `adb install`.
- Con Payara corriendo, abrir la app **Conversor Monster**, ingresar con `monster` / `monster123` y convertir.

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
