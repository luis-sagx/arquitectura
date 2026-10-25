# Cliente de consola Java para el conversor REST

Aplicación Java 21 que consume el servidor `WS_CON_UNI_JAVA_REST` mediante HTTP y JSON. Conserva el flujo de consola del cliente SOAP de referencia y no requiere base de datos.

## Funciones

- Iniciar sesión y conservar el token en memoria.
- Consultar categorías y unidades.
- Convertir longitud, masa y temperatura.
- Cerrar la sesión local.
- Recuperar la clave.

Las operaciones protegidas envían `Authorization: Bearer <token>`. Los errores `AUTH-002` y `AUTH-003` limpian la sesión local. El servidor no publica cierre de sesión remoto.

## Requisitos y compilación

Se requiere JDK 21 y Maven. Desde PowerShell en esta carpeta ejecute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\construir.ps1
```

También puede abrir esta carpeta como proyecto Maven en NetBeans y seleccionar **Clean and Build**. El JAR se genera en `target/CLI_CON_UNI_JAVA_REST.jar`; las dependencias se copian a `target/lib`.

## Ejecución

```powershell
java -jar .\target\CLI_CON_UNI_JAVA_REST.jar
```

La URL predeterminada del API es:

```text
http://localhost:8080/WS_CON_UNI_JAVA_REST/api
```

Para otro contexto, puerto o servidor, indique como primer argumento una URL base que termine en `/api`:

```powershell
java -jar .\target\CLI_CON_UNI_JAVA_REST.jar http://localhost:8080/otro-contexto/api
```

También puede configurarla como propiedad JVM:

```powershell
java -Dconversor.endpoint=http://localhost:8080/otro-contexto/api -jar .\target\CLI_CON_UNI_JAVA_REST.jar
```

Si se indica tanto argumento como propiedad, se usa el argumento.

## Menú

1. Iniciar sesión.
2. Consultar catálogo: elegir una categoría y mostrar sus unidades.
3. Convertir: elegir categoría, unidad de origen, unidad de destino e ingresar un valor.
4. Cerrar sesión local.
5. Recuperar contraseña indicando usuario y nueva clave.
0. Salir. El fin de entrada también cierra la aplicación.

Cada sección se presenta con un encabezado y separadores. La conversión muestra
los cuatro pasos (categoría, origen, destino y cantidad) y conserva las
selecciones anteriores en pantalla. Los resultados y errores esperan Enter
antes de volver al menú cuando la consola es interactiva; elegir `0` en una
selección regresa directamente.

En una terminal se limpia la pantalla entre secciones. En la salida de un IDE
se desplaza el contenido para dejar visible la etapa actual. La interacción se
activa automáticamente cuando Java detecta una terminal; también puede
configurarse con `-Dconversor.interactivo=true` o desactivarse con
`-Dconversor.interactivo=false`. Al redirigir entrada o salida, se omiten las
pausas y la limpieza por defecto.

Las credenciales iniciales del servidor son `Monster` / `Monster9`, salvo que el servidor se haya configurado de otra manera. Las credenciales se conservan literalmente; la clave se oculta en terminales compatibles con `System.console()`. Si se usa desde un IDE, puede verse al escribirla. No se guardan credenciales ni tokens en archivos.

Se aceptan punto o coma decimal y valores negativos o cero; se rechazan `NaN` e infinitos. Las restricciones de conversión, como el cero absoluto, las aplica el servidor. Los errores HTTP/JSON, desconexiones y timeouts se muestran sin cerrar el menú.

La raíz `/WS_CON_UNI_JAVA_REST/api` no tiene un recurso propio y puede responder 404; el cliente usa las rutas concretas `/auth/login`, `/auth/recover`, `/catalog/categories`, `/catalog/categories/{categoria}/units` y `/conversions` debajo de esa raíz.
