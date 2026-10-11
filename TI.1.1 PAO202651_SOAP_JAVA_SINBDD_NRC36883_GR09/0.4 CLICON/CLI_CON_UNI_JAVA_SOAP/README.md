# Cliente consola Java con Web Service Client de NetBeans

La consola utiliza el cliente SOAP que creaste en NetBeans:
ec.edu.monster.ws.WSConUni_Service y el puerto ec.edu.monster.ws.WSConUni.
ClienteSoap adapta las respuestas generadas al menu; todas las peticiones se
realizan mediante los metodos del puerto JAX-WS.

## Abrir y ejecutar en NetBeans

Abrir esta carpeta como proyecto Maven. Ejecutar Clean and Build para generar
las clases ec.edu.monster.ws a partir del WSDL guardado en src/wsdl.
Despues ejecutar ec.edu.monster.consola.MainConsola.

El servidor debe estar desplegado en Payara. URL predeterminada del WSDL creado:

    http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni

Usuario y clave iniciales del servidor: monster / monster123.
Para otra direccion, indicar la URL del endpoint como argumento del programa
o propiedad JVM conversor.endpoint. Tambien se acepta una URL terminada en ?wsdl.

El WSDL y su esquema se incluyen en el JAR. Crear el puerto no requiere consultar
el WSDL remoto; BindingProvider configura la direccion para las operaciones.
Los archivos generados se encuentran en target/generated-sources/jaxws-wsimport
y deben regenerarse con Maven, no editarse a mano.

## Compilar y ejecutar desde PowerShell

Se requiere JDK 17 o superior y Maven, o Maven incluido en NetBeans.

    powershell -NoProfile -ExecutionPolicy Bypass -File ./construir.ps1
    java -jar target/CLI_CON_UNI_JAVA_SOAP.jar

El script busca Maven en PATH, MAVEN_HOME y NetBeans en Downloads.
Tambien se puede indicar -Maven "ruta/al/mvn.cmd".
Alternativa: mvn package.

Para otro contexto o puerto:

    java -jar target/CLI_CON_UNI_JAVA_SOAP.jar http://localhost:8080/otro-contexto/WSConUni

El JAR necesita la carpeta target/lib que Maven genera con las bibliotecas Metro.
Si se mueve el JAR, copiar tambien lib junto a el.
ExecutionPolicy afecta solamente al proceso del script.

## Menu

1. Iniciar sesion.
2. Consultar catalogo: elegir categoria para ver sus unidades.
3. Convertir: elegir longitud, masa o temperatura, origen, destino y valor.
4. Cerrar sesion local.
0. Salir. El fin de entrada tambien cierra la aplicacion.

El catalogo incluye longitud, masa y temperatura. Para temperatura se ofrecen Celsius, Fahrenheit y Kelvin.
La consola guarda el token en memoria y lo envia mediante el puerto generado.
AUTH-002 o AUTH-003 eliminan la sesion local y solicitan iniciar sesion nuevamente.
Cerrar sesion local no revoca tokens en el servidor, cuyo contrato no expone cierre.

Se aceptan punto o coma decimal, cero y negativos; se rechazan NaN e Infinity.
Los calculos y restricciones fisicas pertenecen al servidor.
Las credenciales se conservan literalmente, sin quitar espacios.
La clave se oculta en una terminal compatible con System.console(); en un IDE
puede verse al escribirla. No se guardan credenciales o tokens en archivos.

Las respuestas muestran codigo, mensaje y resultado con unidad si corresponde.
Los fallos de conexion y SOAP se muestran sin cerrar el menu.
Timeout de conexion: 10 segundos. Timeout de respuesta: 30 segundos.

## Pruebas

    powershell -NoProfile -ExecutionPolicy Bypass -File ./ejecutar-pruebas.ps1

PruebaTransporteSOAP comprueba el puerto JAX-WS con respuestas SOAP ajustadas al
contrato: parametros, caracteres especiales, catalogo, resultado, expiracion,
respuesta de otra operacion, errores SOAP/XML y servidor desconectado.
PruebaMenuConsola verifica el recorrido interactivo y entradas invalidas.
PruebaCredencialesConEspacios verifica credenciales literales.
PruebaClienteConsola ejecuta las tres.

PruebaClienteServidorReal publica temporalmente el WSConUni del servidor existente
y verifica la integracion real, sin cambiar el despliegue de Payara:

    java -cp "target/classes;target/test-classes;target/lib/*;../../0.4 SERVIDOR/WS_CON_UNI_JAVA_SOAP/target/classes" ec.edu.monster.pruebas.PruebaClienteServidorReal

Compilar antes el servidor para disponer de sus clases.
Las pruebas con main se ejecutan explicitamente; Maven package las compila.
