# Servidor REST de conversión de unidades

Aplicación Java 21 empaquetada como WAR. Expone Jakarta RESTful Web Services (JAX-RS) con arquitectura MVC: recursos REST como controladores, `ConversorUnidadesServices` para la lógica de negocio y modelos Java serializados como JSON. No requiere base de datos.

## API

La raíz JAX-RS es `/api`. Sustituye `{contexto}` por el nombre de contexto asignado al desplegar el WAR, por ejemplo `WS_CON_UNI_JAVA_REST`.

| Método y ruta | Requiere sesión | Cuerpo / respuesta |
|---|---|---|
| `POST /api/auth/login` | No | Recibe `usuario` y `clave`; devuelve `token` y `expiraEn`. |
| `POST /api/auth/recover` | No | Recibe `usuario` y `nuevaClave`; actualiza la clave e invalida sesiones. |
| `GET /api/catalog/categories` | Sí | Devuelve la lista `categorias`. |
| `GET /api/catalog/categories/{categoria}/units` | Sí | Devuelve la categoría y sus `unidades`. |
| `POST /api/conversions` | Sí | Recibe categoría, unidades y valor; devuelve `resultado`. |

Envíe y reciba `application/json`. Las operaciones protegidas usan `Authorization: Bearer <token>`.

### Iniciar sesión

```http
POST /{contexto}/api/auth/login
Content-Type: application/json

{"usuario":"Monster","clave":"Monster9"}
```

Respuesta exitosa (HTTP 200):

```json
{
  "estado": "EXITO",
  "codigo": "OK",
  "mensaje": "Sesion iniciada",
  "token": "<token>",
  "expiraEn": "<fecha UTC ISO-8601>"
}
```

### Consultar categorías y unidades

```http
GET /{contexto}/api/catalog/categories
Authorization: Bearer <token>
```

```http
GET /{contexto}/api/catalog/categories/LONGITUD/units
Authorization: Bearer <token>
```

### Convertir unidades

```http
POST /{contexto}/api/conversions
Authorization: Bearer <token>
Content-Type: application/json

{"categoria":"LONGITUD","unidadOrigen":"KILOMETRO","unidadDestino":"METRO","valor":2.5}
```

La respuesta incluye `resultado`, `categoria`, `unidadOrigen` y `unidadDestino`.

### Recuperar clave

```http
POST /{contexto}/api/auth/recover
Content-Type: application/json

{"usuario":"Monster","nuevaClave":"nueva-clave"}
```

La recuperación solo verifica el nombre de usuario. El cambio vive en memoria, borra todas las sesiones y se pierde al reiniciar o redesplegar.

## Credenciales, sesiones y configuración

Credenciales iniciales: usuario `Monster`, clave `Monster9`. La sesión dura 15 minutos por defecto. Configure estas propiedades JVM del servidor antes de iniciar o desplegar:

- `conversor.usuario`
- `conversor.clave`
- `conversor.sesion.minutos`

Los tokens son UUID compartidos por las peticiones de la aplicación en la misma JVM. Un reinicio o redespliegue invalida los tokens. Los tokens expirados se conservan hasta un día después de su expiración para distinguirlos de tokens desconocidos; se depuran al autenticar.

## Catálogo y reglas

- **LONGITUD:** `KILOMETRO`, `METRO`, `CENTIMETRO`, `MILIMETRO`, `MILLA`, `PIE`.
- **MASA:** `KILOGRAMO`, `GRAMO`, `MILIGRAMO`, `LIBRA`.
- **TEMPERATURA:** `CELSIUS`, `FAHRENHEIT`, `KELVIN`.

Categorías y unidades aceptan minúsculas y espacios exteriores. Se permiten cero y números negativos, excepto temperaturas bajo el cero absoluto. Se usa `double` sin redondeo de negocio; se rechazan valores no finitos, resultados fuera de rango y unidades de otra magnitud.

## Respuestas y errores

Todas las respuestas de negocio usan `estado`, `codigo` y `mensaje`; el resto de los campos se incluye cuando corresponde. Los éxitos usan HTTP 200 y `codigo: "OK"`. Los errores `AUTH-*` usan HTTP 401; los demás errores usan HTTP 400. Un error de negocio mantiene `estado: "ERROR"` y no incluye un resultado numérico.

| Código | Significado |
|---|---|
| `AUTH-001` | Credenciales incorrectas o usuario inexistente. |
| `AUTH-002` | Token ausente, inválido o desconocido. |
| `AUTH-003` | Sesión expirada. |
| `UNIT-001` | Unidad no soportada. |
| `DATA-001` | Datos incompletos, incompatibles o fuera de rango. |

## Compilación

Desde esta carpeta, genere el WAR con Java 21 y Maven:

```powershell
mvn package
```

Despliegue `target/WS_CON_UNI_JAVA_REST-1.0-SNAPSHOT.war` en un servidor compatible con Jakarta EE 11 y consulte las rutas bajo su contexto de aplicación.

## Pruebas

Las pruebas del servicio están en `Source Packages > ec.edu.monster.pruebas`. Estas pruebas cubren autenticación, catálogo, conversiones, validaciones, expiración y recuperación de clave; no requieren desplegar el WAR.


Con el WAR desplegado y Payara en ejecución, desde PowerShell en esta carpeta ejecute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

La suite HTTP asume por defecto la URL `http://localhost:8080/WS_CON_UNI_JAVA_REST-1.0-SNAPSHOT/api`. Si el contexto o puerto difiere, indíquelo como primer argumento:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1 -BaseUrl "http://localhost:8080/WS_CON_UNI_JAVA_REST/api"
```

Para ejecutar solo las pruebas locales de autenticación, sesiones, catálogo y conversiones sin iniciar Payara:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1 -SoloServicio
```

La integración prueba y restaura la clave inicial `Monster9` al terminar.
