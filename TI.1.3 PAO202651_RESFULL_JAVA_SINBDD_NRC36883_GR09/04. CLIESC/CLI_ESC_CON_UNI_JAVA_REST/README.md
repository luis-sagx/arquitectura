# CLI_ESC_CON_UNI_JAVA_REST

Aplicación de escritorio Swing con arquitectura MVC. Consume el servidor REST de conversiones mediante HTTP y JSON.

## Requisitos

- JDK 21
- Maven (también puedes usar el Maven incluido con NetBeans)
- Servidor `WS_CON_UNI_JAVA_REST` desplegado y en ejecución

## Abrir y ejecutar en NetBeans

Abre **File > Open Project** y selecciona `04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST` dentro del proyecto REST. Ejecuta la aplicación Maven con `ec.edu.monster.ClienteEscritorio` como clase principal. El empaquetado genera `target/CLI_ESC_CON_UNI_JAVA_REST.jar` y copia Gson a `target/lib`.

También puedes compilar desde esta carpeta:

```powershell
mvn -DskipTests package
```

Para iniciar el JAR, conserva el directorio `target/lib` junto al archivo:

```powershell
java -jar target/CLI_ESC_CON_UNI_JAVA_REST.jar
```

## Servidor y endpoint

El servidor REST debe estar desplegado antes de iniciar sesión. El endpoint predeterminado es:

```text
http://localhost:8080/WS_CON_UNI_JAVA_REST/api
```

Puedes sustituirlo con la propiedad JVM `conversor.endpoint` o con el primer argumento de la aplicación:

```powershell
java -Dconversor.endpoint=http://localhost:8080/WS_CON_UNI_JAVA_REST/api -jar target/CLI_ESC_CON_UNI_JAVA_REST.jar
```

## Funciones

- Inicio de sesión y recuperación de contraseña
- Consulta de categorías y unidades disponibles
- Conversión de longitud, masa y temperatura
- Cierre local y vencimiento de sesión

Credenciales iniciales del servidor: usuario `Monster`, clave `Monster9`.

## Estructura MVC

- `ec.edu.monster.vista`: ventanas, estilos y navegación
- `ec.edu.monster.controlador`: acceso, catálogo, sesión y conversiones
- `ec.edu.monster.modelo`: solicitudes, sesión, catálogo y respuestas REST
- `ec.edu.monster.servicio`: cliente HTTP/JSON

El token se mantiene solo en memoria. Las llamadas HTTP se realizan en `SwingWorker` para mantener disponible la interfaz.
