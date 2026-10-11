# Servidor SOAP de conversion de unidades

Proyecto Java 17 WAR existente, con SOAP/JAX-WS Jakarta y despliegue en Payara compatible con Jakarta.
La logica de suma fue sustituida por conversion, autenticacion y catalogo en memoria.
No requiere una conexion de base de datos.

## Operaciones de WSConUni

- autenticar(usuario, clave): retorna token y expiraEn (fecha UTC ISO-8601).
- obtenerCategorias(token): retorna las categorias de longitud, masa y temperatura.
- obtenerUnidades(token, categoria): retorna identificadores de unidades.
- convertir(token, categoria, unidadOrigen, unidadDestino, valor): retorna resultado y unidades.

Namespace SOAP: http://ws.controladores.monster.edu.ec/
Despues de desplegar, consulte la URL del WSDL que muestra Payara/NetBeans para WSConUni.
Los clientes de la antigua operacion sumar deben regenerarse desde el nuevo WSDL.

Toda respuesta contiene estado (EXITO o ERROR), codigo y mensaje.
Conversion exitosa: resultado numerico. Error: resultado nulo.
Autenticacion exitosa: token y expiraEn. Consultas: categorias o unidades.

## Configuracion inicial

Usuario: monster. Clave: monster123. Sesion: 15 minutos.
Puede cambiar estas propiedades JVM en Payara antes de iniciar/desplegar:

- conversor.usuario
- conversor.clave
- conversor.sesion.minutos

Las sesiones son compartidas entre peticiones dentro de la misma aplicacion/JVM.
Reiniciar o redesplegar invalida los tokens. No se comparten sesiones entre servidores.
Los tokens vencidos se conservan hasta un dia despues del vencimiento y se depuran al autenticar;
tras su depuracion se consideran desconocidos.

## Alcance y reglas

LONGITUD: KILOMETRO, METRO, CENTIMETRO, MILIMETRO, MILLA, PIE.
MASA: KILOGRAMO, GRAMO, MILIGRAMO, LIBRA.
TEMPERATURA: CELSIUS, FAHRENHEIT, KELVIN.

Todas las categorias del catalogo permiten conversion.
Categoria y unidades aceptan minusculas y espacios exteriores; usar los identificadores del catalogo.
Cero y negativos se aceptan, salvo temperaturas inferiores al cero absoluto.
Se utiliza double sin redondeo de negocio; el cliente puede formatear la presentacion.
Valores no finitos y resultados fuera de rango se rechazan.
Catalogo y conversion requieren token, siguiendo los diagramas de actividad.

Codigos: OK, AUTH-001 (credenciales), AUTH-002 (token invalido),
AUTH-003 (sesion expirada), UNIT-001 (unidad no soportada),
DATA-001 (datos incompletos, incompatibles o fuera de rango).

## Pruebas

En NetBeans, ejecutar individualmente las clases de ec.edu.monster.pruebas con Run File:
PruebaAutenticacion, PruebaConsultaCatalogo, PruebaConversionLongitud,
PruebaConversionMasa, PruebaConversionTemperatura, PruebaValidacionConversion,
PruebaSesionExpirada, PruebaCeroAbsoluto y PruebaTemperaturaValoresGrandes.
PruebaSistemaUnidades ejecuta las nueve.

Desde PowerShell con JDK 17 o superior:

    powershell -NoProfile -ExecutionPolicy Bypass -File ./ejecutar-pruebas.ps1

Esta opcion aplica solamente al proceso de pruebas, sin cambiar la politica del equipo.

Las pruebas usan valores esperados y lanzan AssertionError si hay fallos.
La expiracion se verifica con un reloj controlado, sin esperar 15 minutos.
Para generar el WAR, usar Clean and Build en NetBeans o mvn package.
La prueba adicional src/test/java/ec/edu/monster/pruebas/PruebaIntegracionSOAP.java
publica un endpoint temporal en localhost, comprueba WSDL y mensajes XML reales y lo cierra.
Requiere las dependencias de ejecucion Metro del proyecto en el classpath.
