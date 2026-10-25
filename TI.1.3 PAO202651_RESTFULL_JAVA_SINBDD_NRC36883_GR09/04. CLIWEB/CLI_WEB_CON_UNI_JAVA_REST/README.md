# Cliente Web REST de conversión de unidades

Aplicación web Java 21 empaquetada como WAR. Sigue MVC con servlets controladores, vistas JSP y un servicio HTTP que consume el servidor REST del proyecto `04. SERVIDOR/WS_CON_UNI_JAVA_REST`.

## Requisitos

- JDK 21 y Maven 3.8 o superior.
- Payara o un servidor Jakarta EE 11 compatible.
- Desplegar primero el WAR del servidor REST.

## Compilar y desplegar

Desde esta carpeta, ejecutar `mvn package`. Maven genera `target/CLI_WEB_CON_UNI_JAVA_REST-1.0-SNAPSHOT.war`; despliega ese archivo en Payara y abre el contexto asignado al WAR.

El endpoint REST predeterminado se configura en `src/main/webapp/WEB-INF/web.xml` como `rest.endpoint`:

```text
http://localhost:8080/WS_CON_UNI_JAVA_REST/api
```

Para otro contexto o servidor, cambia ese parámetro o define la propiedad JVM `-Dconversor.rest.endpoint=https://host/contexto/api`. La propiedad de sistema se usa si `rest.endpoint` no está configurado.

## Funciones

- Inicio y cierre de sesión; el token Bearer se conserva solo en la sesión HTTP del servidor web.
- Recuperación de clave a través de `POST /auth/recover`.
- Consulta autenticada del catálogo con `GET /catalog/categories` y `GET /catalog/categories/{categoria}/units`.
- Conversión autenticada con `POST /conversions`.

Credenciales iniciales del servidor, salvo que se hayan cambiado: usuario `Monster`, clave `Monster9`. La sesión del servicio vence después de 15 minutos por defecto; al vencer, el cliente redirige al inicio de sesión.
