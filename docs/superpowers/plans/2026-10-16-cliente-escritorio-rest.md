# Cliente de Escritorio REST Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Crear un cliente Swing MVC ejecutable que use el servidor REST de conversión y refleje la estructura y presentación del cliente de escritorio SOAP.

**Architecture:** Crear `TI.1.3 PAO202651_RESFULL_JAVA_SINBDD_NRC36883_GR09/04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST` como JAR Maven Java 21. El cliente tendrá modelos independientes, un adaptador Gson/HttpClient, controladores SwingWorker y vistas Swing bajo los mismos paquetes MVC del escritorio SOAP.

**Tech Stack:** Java 21, Swing, `java.net.http.HttpClient`, Gson 2.14.0, Maven JAR.

**Spec:** `docs/superpowers/specs/2026-10-16-cliente-escritorio-rest-design.md`

**Project root (`ROOT`):** `TI.1.3 PAO202651_RESFULL_JAVA_SINBDD_NRC36883_GR09/04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST`

## Global Constraints

- Crear un proyecto de escritorio Swing independiente dentro de `04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST` del árbol REST.
- Mantener `ec.edu.monster.controlador`, `ec.edu.monster.modelo`, `ec.edu.monster.servicio` y `ec.edu.monster.vista`.
- Consumir la raíz `http://localhost:8080/WS_CON_UNI_JAVA_REST/api`, configurable con `conversor.endpoint` o el primer argumento de la aplicación.
- Usar HTTP JSON, encabezado `Authorization: Bearer <token>` y campos del contrato REST indicados en la especificación.
- Guardar el token solo en memoria y borrar al cerrar o expirar la sesión.
- Mantener ventanas Swing de 790 × 660 píxeles y la paleta azul marino/gris/blanco del escritorio SOAP.
- No modificar los clientes SOAP, REST de consola/web ni el servidor; no agregar persistencia o JAX-WS.
- No agregar ni ejecutar pruebas automatizadas; compilar y empaquetar sin ejecutar pruebas.

## Review Focus

- URL base malformada o con protocolo distinto a HTTP/HTTPS: mostrar error de configuración comprensible al arrancar.
- Servidor detenido o sin respuesta: liberar el formulario y mostrar error de comunicación sin bloquear Swing.
- HTTP no exitoso con JSON de error: conservar `codigo`/`mensaje` para mostrar el error funcional.
- Token inválido o expirado (`AUTH-002`, `AUTH-003`): borrar el token y regresar a la ventana de acceso.
- Valor vacío, no numérico, no finito o con separadores ambiguos: no enviar conversión y explicar el formato válido.

---

### Task 1: Proyecto Maven, modelos y adaptador REST

**Files:**
- Create: `TI.1.3 PAO202651_RESFULL_JAVA_SINBDD_NRC36883_GR09/04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST/pom.xml`
- Create: `TI.1.3 PAO202651_RESFULL_JAVA_SINBDD_NRC36883_GR09/04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST/.gitignore`
- Create: `ROOT/src/main/java/ec/edu/monster/modelo/RespuestaRest.java`
- Create: `ROOT/src/main/java/ec/edu/monster/modelo/LoginRequest.java`
- Create: `ROOT/src/main/java/ec/edu/monster/modelo/RecoverRequest.java`
- Create: `ROOT/src/main/java/ec/edu/monster/modelo/ConversionRequest.java`
- Create: `ROOT/src/main/java/ec/edu/monster/modelo/Sesion.java`
- Create: `ROOT/src/main/java/ec/edu/monster/modelo/OpcionCatalogo.java`
- Create: `ROOT/src/main/java/ec/edu/monster/servicio/ClienteRest.java`

**Interfaces:**
- `ClienteRest(URI baseUri)`; methods `autenticar(String usuario, String clave)`, `recuperarClave(String usuario, String nuevaClave)`, `obtenerCategorias(String token)`, `obtenerUnidades(String token, String categoria)`, `convertir(String token, String categoria, String unidadOrigen, String unidadDestino, double valor)` all return `RespuestaRest` and throw `IOException`.
- `RespuestaRest` exposes getters for `estadoHttp`, `estado`, `codigo`, `mensaje`, `token`, `expiraEn`, `resultado`, `categoria`, `unidadOrigen`, `unidadDestino`, `categorias`, `unidades`, plus `boolean isExito()` and `boolean isSesionInvalida()`.
- Request records serialize exact JSON names `usuario`, `clave`, `nuevaClave`, `categoria`, `unidadOrigen`, `unidadDestino`, `valor`.
- `Sesion(String token, String expiraEn)`, `String token()`, `String expiraEn()`, `void limpiarToken()`.
- `OpcionCatalogo(String id, String etiqueta)` with `static OpcionCatalogo desde(String id)` and readable `toString()` label.

- [ ] Create a Java 21 Maven JAR POM with Gson 2.14.0, compiler plugin 3.12.1, JAR plugin 3.5.0, dependency plugin 3.8.1 to copy runtime dependencies to `target/lib`, and `exec.mainClass=ec.edu.monster.ClienteEscritorio`.
- [ ] Add `.gitignore` entries for `/target/`, `/nbproject/private/` and `*.class`.
- [ ] Implement request and response models; attach the HTTP status to every parsed response and define success/session-invalid helpers from business `estado`/`codigo`.
- [ ] Implement `ClienteRest` with JDK `HttpClient` (10-second connect timeout; 30-second request timeout), Gson JSON, base URI validation, UTF-8, and Bearer header for catalog/conversion calls. Parse JSON error bodies for non-2xx responses; turn transport failures and malformed JSON into safe `IOException` messages.
- [ ] Implement `Sesion` and `OpcionCatalogo` so token remains in memory and catalog ids remain separate from display labels.

### Task 2: Swing views, navigation and shared styling

**Files:**
- Create: `ROOT/src/main/java/ec/edu/monster/vista/EstilosUI.java`
- Create: `ROOT/src/main/java/ec/edu/monster/vista/GestorPantallas.java`
- Create: `ROOT/src/main/java/ec/edu/monster/vista/LoginFrame.java`
- Create: `ROOT/src/main/java/ec/edu/monster/vista/VentanaConversiones.java`

**Interfaces:**
- `LoginFrame`: `alIngresar(ActionListener)`, `alRecuperar(ActionListener)`, `usuarioIngresado()`, `claveIngresada()`, `pedirNuevaClave()`, `limpiarClave()`, `setOcupado(boolean)`, `mostrarMensaje(String, boolean)`, `mostrarDialogo(String, String, int)`.
- `VentanaConversiones`: `alCambiarCategoria(ActionListener)`, `alConvertir(ActionListener)`, `alCerrarSesion(ActionListener)`, `categoriaSeleccionada()`, `origenSeleccionado()`, `destinoSeleccionado()`, `valorIngresado()`, `setCategorias(List<OpcionCatalogo>)`, `seleccionarPrimeraCategoria()`, `setUnidades(List<OpcionCatalogo>)`, `limpiarUnidades()`, `mostrarResultado(String)`, `mostrarEstado(String)`, `setOcupado(boolean)`, `mostrarAviso(String,String,int)`, `limpiarResultado()`.
- `GestorPantallas.INSTANCIA`: `registrarAcceso(LoginFrame)`, `mostrarAcceso()`, `mostrarConversiones(VentanaConversiones)`, `volverAlAcceso(VentanaConversiones, String, boolean)`.

- [ ] Port `EstilosUI` color values and 790 × 660 sizing from the SOAP desktop client; keep component spacing and button/field treatments consistent.
- [ ] Implement `LoginFrame` with login and password-recovery actions, password fields, loading controls and status messages.
- [ ] Implement `VentanaConversiones` with category/origin/destination selectors, numeric input, result, status, progress state and logout.
- [ ] Implement `GestorPantallas` navigation between login and conversion windows, disposing old conversion windows when returning to login.

### Task 3: Application entry point and login/recovery controller

**Files:**
- Create: `ROOT/src/main/java/ec/edu/monster/ClienteEscritorio.java`
- Create: `ROOT/src/main/java/ec/edu/monster/controlador/ControladorLogin.java`

**Interfaces:** `ControladorLogin(ClienteRest cliente, LoginFrame vista)` and `void mostrar()`; uses Task 1 client/models and Task 2 view/navigation interfaces.

- [ ] Start Swing on the EDT, apply system look-and-feel when available, resolve endpoint in precedence order (first argument, then `-Dconversor.endpoint`, then default URI), validate it, and display a setup dialog for invalid URL.
- [ ] Implement asynchronous login using `SwingWorker`; on business success create `Sesion`, clear the password field and open conversions; on failure show the server message and re-enable inputs.
- [ ] Implement asynchronous password recovery; clear temporary `char[]` values after use and show success/business/network messages without freezing the UI.
- [ ] Map `IOException` to a readable connection dialog while preserving thread interruption.

### Task 4: Catalog, conversion and session controller

**Files:**
- Create: `ROOT/src/main/java/ec/edu/monster/controlador/ControladorConversiones.java`

**Interfaces:** `ControladorConversiones(ClienteRest cliente, Sesion sesion, VentanaConversiones vista)` and `void iniciar()`; consumes Task 1 client/session and Task 2 view/navigation interfaces.

- [ ] Load categories asynchronously after opening the conversion window; map ids to `OpcionCatalogo`, select the first category and request its units.
- [ ] Load category units asynchronously when selection changes; clear stale result and unit selections before the request.
- [ ] Parse values accepting one decimal point or comma, reject blank, ambiguous separators and non-finite values before sending.
- [ ] Call REST conversion asynchronously, format result with `es-EC` locale and four maximum fractional digits, and display the destination unit.
- [ ] On `AUTH-002`/`AUTH-003`, clear the token and return to login; on other HTTP/business errors show the returned message; on network failure re-enable controls and show a communication dialog.
- [ ] Close local session when logout is clicked or the conversion window is closed.

### Task 5: NetBeans launch configuration and user documentation

**Files:**
- Create: `ROOT/nbactions.xml`
- Create: `ROOT/README.md`

- [ ] Configure NetBeans to run `ec.edu.monster.ClienteEscritorio` with Maven and allow endpoint override through JVM property or first program argument.
- [ ] Document Java 21, Maven build/open steps, executable JAR plus `target/lib`, REST server prerequisite, default endpoint, supported functions and demo credentials `monster` / `monster123`.

### Task 6: Compile and package the executable JAR

**Files:**
- Review: all files created in Tasks 1–5.

- [ ] Build with JDK 21 and Maven using `mvn -DskipTests package`; confirm the JAR and Gson runtime dependency are generated under `target/` without running automated tests.
- [ ] Review API path/payload names and UI package layout against the REST server README and SOAP desktop source; preserve all changes locally and do not push.
