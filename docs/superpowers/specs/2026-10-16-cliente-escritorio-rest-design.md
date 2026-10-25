# Diseño: cliente de escritorio MVC para el servidor REST

## Objetivo

Agregar al proyecto `TI.1.3 PAO202651_RESTFULL_JAVA_SINBDD_NRC36883_GR09` una aplicación Swing de escritorio que consuma el servidor REST de conversiones. La estructura de paquetes, navegación y presentación seguirá el cliente `04. CLIESC/ClienteEscritorio` del proyecto SOAP, usando HTTP/JSON en lugar de JAX-WS/SOAP.

## Alcance funcional

- Inicio de sesión y recuperación de clave mediante el servidor REST.
- Consulta autenticada de categorías y unidades.
- Conversiones para longitud, masa y temperatura.
- Cierre local de sesión y manejo de tokens inválidos o expirados.
- Mensajes comprensibles para errores de validación, negocio y conexión.

## Estructura y arquitectura

Crear `04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST` dentro del proyecto REST como aplicación Maven Java 21 empaquetada en JAR ejecutable. Mantener la separación MVC y nombres de paquetes del escritorio SOAP:

- `ec.edu.monster.ClienteEscritorio`: punto de entrada Swing.
- `ec.edu.monster.controlador`: `ControladorLogin` y `ControladorConversiones`; operaciones de red ejecutadas con `SwingWorker` para no bloquear el hilo de interfaz.
- `ec.edu.monster.modelo`: `Sesion`, `OpcionCatalogo` y `RespuestaRest`.
- `ec.edu.monster.servicio`: `ClienteRest`, adaptador HTTP/JSON para los endpoints REST.
- `ec.edu.monster.vista`: `LoginFrame`, `VentanaConversiones`, `GestorPantallas` y `EstilosUI`.

El proyecto será independiente de los clientes REST de consola/web y del cliente SOAP. Usará `java.net.http.HttpClient` y Gson para JSON, Java 21 y un JAR con manifiesto y bibliotecas runtime en `target/lib`. No incluirá proxies JAX-WS, WSDL ni llamadas directas a la lógica del servidor.

## Contrato REST consumido

Raíz por defecto: `http://localhost:8080/WS_CON_UNI_JAVA_REST/api`. Se podrá sustituir mediante la propiedad JVM `conversor.endpoint` o el primer argumento de la aplicación.

- `POST /auth/login`: JSON `usuario`, `clave`; devuelve token y expiración.
- `POST /auth/recover`: JSON `usuario`, `nuevaClave`.
- `GET /catalog/categories`: requiere `Authorization: Bearer <token>`.
- `GET /catalog/categories/{categoria}/units`: requiere Bearer.
- `POST /conversions`: requiere Bearer y recibe `categoria`, `unidadOrigen`, `unidadDestino`, `valor`.

Las respuestas de negocio se mapean desde JSON a `RespuestaRest`, incluyendo `estado`, `codigo`, `mensaje`, `token`, `expiraEn`, `resultado`, `categoria`, unidades y listas del catálogo. Los códigos `AUTH-002` y `AUTH-003` limpian el token de memoria y regresan a la pantalla de acceso.

## Sesión y transporte

El token permanece solo en memoria dentro del modelo `Sesion`; se borra al cerrar sesión, salir de la aplicación o recibir una respuesta de sesión inválida. Las peticiones protegidas envían el token como Bearer. `ClienteRest` tendrá timeout de conexión de 10 segundos y timeout de respuesta de 30 segundos, validará la URL HTTP/HTTPS y presentará errores de red sin exponer trazas técnicas.

## Interfaz

Conservar las ventanas y navegación del cliente SOAP, incluidos el tamaño fijo de 790 × 660 píxeles y los tonos de fondo gris claro, panel blanco, texto gris oscuro, azul marino y controles secundarios gris/azul suave. Mantener formularios de acceso y conversión, recuperación de clave, estados de carga, avisos, resultado y controles no bloqueantes mientras se consulta la API.

## Compilación y ejecución

La aplicación se abre como proyecto Maven en NetBeans, compila con JDK 21 y se ejecuta desde `ec.edu.monster.ClienteEscritorio`. El servidor REST debe estar desplegado antes de iniciar sesión. Maven generará un JAR ejecutable y copiará Gson a `target/lib`; el endpoint local puede cambiarse con `-Dconversor.endpoint=...` o como primer argumento.

## Fuera de alcance

No modificar el cliente SOAP, el cliente de consola REST, el cliente web REST ni el servidor. No agregar persistencia, autenticación local ni dependencias de Jakarta EE/JAX-WS al cliente Swing.
