# Servidor SOAP WCF de conversión de unidades

Servicio SOAP 1.1 alojado en WCF sobre .NET Framework 4.7.2. La lógica se separa en `ec.edu.monster.controladores.ws`, `ec.edu.monster.servicios` y `ec.edu.monster.modelos`; no usa base de datos.

## Endpoint y contrato

El servicio se aloja en `WSConUni.svc`. Después de iniciar la aplicación web, consulte su WSDL en:

`http://<servidor>/<aplicacion>/WSConUni.svc?wsdl`

Namespace SOAP: `http://ws.controladores.monster.edu.ec/`.

Operaciones:

- `autenticar(usuario, clave)`: crea un token y devuelve `expiraEn` en UTC ISO-8601.
- `recuperarClave(usuario, nuevaClave)`: reemplaza la clave en memoria e invalida las sesiones actuales.
- `obtenerCategorias(token)`: lista las categorías disponibles.
- `obtenerUnidades(token, categoria)`: lista las unidades de una categoría.
- `convertir(token, categoria, unidadOrigen, unidadDestino, valor)`: devuelve el valor convertido.

Las respuestas contienen `estado`, `codigo` y `mensaje`, además de los datos aplicables (`token`, `expiraEn`, `resultado`, `categoria`, `unidadOrigen`, `unidadDestino`, `categorias`, `unidades`). En caso de error, `resultado` queda nulo.

## Configuración y estado

Valores iniciales: usuario `monster`, clave `monster123`, sesión de 15 minutos. Se configuran en `Web.config` con `conversor.usuario`, `conversor.clave` y `conversor.sesion.minutos`.

Las sesiones y la clave recuperada viven en memoria del proceso. Reiniciar o redesplegar el servidor invalida los tokens y restaura la clave configurada. La recuperación comprueba el usuario, no requiere sesión y limpia todas las sesiones activas.

## Catálogo y validaciones

- `LONGITUD`: `KILOMETRO`, `METRO`, `CENTIMETRO`, `MILIMETRO`, `MILLA`, `PIE`.
- `MASA`: `KILOGRAMO`, `GRAMO`, `MILIGRAMO`, `LIBRA`.
- `TEMPERATURA`: `CELSIUS`, `FAHRENHEIT`, `KELVIN`.

Categorías y unidades aceptan minúsculas y espacios exteriores. Longitud y masa admiten valores desde cero; las temperaturas admiten valores hasta el cero absoluto. Valores no finitos, entradas incompletas, unidades incompatibles y resultados fuera de rango se rechazan.

Códigos: `OK`, `AUTH-001` (credenciales o usuario), `AUTH-002` (token ausente o inválido), `AUTH-003` (sesión expirada), `UNIT-001` (unidad no soportada), `DATA-001` (datos o conversión inválidos).

## Pruebas de lógica y contrato

Desde la carpeta del proyecto `04. SERVIDOR/WCF_CON_UNI_DOTNET_SOAP/WCF_CON_UNI_DOTNET_SOAP` ejecute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ec\edu\monster\pruebas\Ejecutar-Pruebas.ps1
```

El ejecutable de pruebas usa el compilador C# de .NET Framework y verifica conversiones, códigos, sesiones, recuperación y metadatos WSDL generados desde el contrato. Para compilar y alojar el proyecto web completo se requieren los componentes de desarrollo web de Visual Studio.

Los clientes generados desde el contrato anterior de ejemplo deben actualizar su referencia de servicio desde el nuevo WSDL.
