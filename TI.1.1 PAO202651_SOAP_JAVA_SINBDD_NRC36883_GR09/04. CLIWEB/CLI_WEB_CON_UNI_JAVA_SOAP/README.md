# Cliente web Java MVC de conversión de unidades

Aplicación web Java 21 que consume las operaciones SOAP del proyecto `04. SERVIDOR/WS_CON_UNI_JAVA_SOAP`. Sigue la misma estructura MVC de los clientes de escritorio y móvil:

- `ec.edu.monster.modelo`: respuestas de datos normalizadas.
- `ec.edu.monster.vista`: ayuda de presentación usada por JSP.
- `ec.edu.monster.controlador`: Servlets y filtros HTTP.
- `ec.edu.monster.servicio`: servicio de aplicación y adaptador SOAP.
- `ec.edu.monster.clientesws`: proxies SOAP generados por Maven.
- `src/main/webapp/WEB-INF/vista`: páginas JSP.

## Requisitos y ejecución

- JDK 21, Maven 3.8 o superior y Payara/Jakarta EE con soporte Metro JAX-WS.
- Inicia y despliega primero el servidor SOAP.
- El endpoint predeterminado es `http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni`. Cámbialo en `src/main/webapp/WEB-INF/web.xml` (`soap.endpoint`) o con `-Dconversor.soap.endpoint=https://host/ruta/WSConUni` en la JVM.
- Ejecuta `mvn clean package` y despliega `target/CLI_WEB_CON_UNI_JAVA_SOAP-1.0-SNAPSHOT.war` en Payara.
- Abre el contexto de la aplicación. Las credenciales iniciales están documentadas en el README del servidor.

El WSDL y su esquema se incluyen localmente; Maven genera las clases JAX-WS sin necesitar el servidor activo durante la compilación. Cada llamada usa su propio puerto SOAP. El token de autenticación se guarda en la sesión HTTP; los formularios POST incluyen protección CSRF y las vistas escapan los valores mostrados.
