# ClienteEscritorio

Aplicación Java Swing con arquitectura MVC para consumir el servicio SOAP `WSConUni`.

## Requisitos

- JDK 21
- Maven (NetBeans puede usar el Maven que incluye)
- Servidor Payara desplegado y accesible desde el equipo cliente

## Abrir y ejecutar en NetBeans

1. Selecciona **File > Open Project** y abre la carpeta `04. CLIESC/ClienteEscritorio`.
2. Ejecuta **Clean and Build**. Maven genera los proxies JAX-WS en `target/generated-sources` desde el WSDL guardado localmente.
3. Ejecuta el proyecto; la clase principal es `ec.edu.monster.ClienteEscritorio`.

El WSDL local permite compilar aunque Payara no esté encendido. Para iniciar sesión y usar las operaciones, el servidor debe estar desplegado.

Si la generación `wsimport` falla en Windows con `Invoker` y un `NullPointerException`, configura antes de abrir NetBeans la variable de entorno `MAVEN_OPTS` con un directorio temporal escribible cuyo path no tenga espacios. En este entorno se resolvió con:

```text
-Djava.io.tmpdir=D:\dev\tmp
```

## Dirección del servicio

El endpoint predeterminado es:

```text
http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni
```

Puedes cambiarlo sin modificar código con la propiedad JVM `conversor.endpoint`, por ejemplo:

```text
-Dconversor.endpoint=http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni
```

También puedes pasar la URL como primer argumento del programa.

## Arquitectura

- `ec.edu.monster.vista`: pantallas Swing, estilos compartidos y `GestorPantallas` Singleton para la navegación.
- `ec.edu.monster.controlador`: autenticación, sesión, consulta del catálogo y conversión.
- `ec.edu.monster.modelo`: sesión, opciones del catálogo y respuestas normalizadas.
- `ec.edu.monster.servicio`: adaptador de las llamadas SOAP.
- `ec.edu.monster.clientesws`: proxies generados desde el WSDL; no editarlos manualmente.

Las ventanas de acceso y conversión comparten el tamaño fijo de 790 × 660 píxeles.

La aplicación consume `autenticar`, `recuperarClave`, `obtenerCategorias`, `obtenerUnidades` y `convertir`. La sesión se mantiene únicamente en memoria.

Para un servidor fuera del equipo local o de una red confiable, configura el endpoint con HTTPS para proteger las credenciales y el token durante el transporte.
