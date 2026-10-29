# Ajuste visual de clientes de escritorio Implementation Plan

> **For agentic workers:** Implement each task in order and review its diff before moving on.

**Goal:** Unificar el espacio junto a la imagen lateral y el patrón del área de resultado en los clientes Java SOAP, .NET SOAP, Java REST y .NET REST.

**Architecture:** Conservar cada formulario y sus flujos. En Swing, agrupar la etiqueta pequeña y el valor destacado dentro de una tarjeta de resultado; en WinForms, hacer lo mismo con controles existentes y un panel. Aumentar el margen lateral de contenido conservando el tamaño actual de ventana.

**Tech Stack:** Java Swing, C# WinForms.

**Spec:** `docs/superpowers/specs/2026-10-28-ajuste-visual-clientes-escritorio-design.md`

## Global Constraints

- Cambiar únicamente los cuatro clientes de escritorio.
- La zona se titula «Resultado» con el mismo tamaño y color que las demás etiquetas; queda vacía antes de convertir y al limpiarse, y destaca el valor tras convertir.
- Aumentar la separación entre contenido e imagen lateral manteniendo las dimensiones actuales de ventana.
- No alterar servicios, conversiones ni los clientes web.
- No agregar ni ejecutar pruebas en este cambio.

## Review Focus

- El resultado debe permanecer vacío al cargar y al cambiar categoría: revisar `limpiarResultado` y sus llamadas ya existentes en cada cliente.
- Al recibir una conversión válida, debe aparecer texto destacado: revisar el método de presentación ya existente en cada cliente.
- La etiqueta «Resultado» conserva el tamaño, color y espaciado de las demás etiquetas incluso sin convertir.
- El margen adicional no debe solapar el formulario con la imagen ni recortar la ventana.
- Confirmar que las pantallas de catálogo y autenticación no reciban cambios colaterales.

---

### Task 1: Java SOAP Swing

**Files:**
- Modify: `04. CLIESC/CLI_ESC_CON_UNI_JAVA_SOAP/src/main/java/ec/edu/monster/vista/VentanaConversiones.java`

**Interfaces:** Conservar las firmas públicas `mostrarResultado(String texto)` y `limpiarResultado()`.

- [ ] Aumentar el margen derecho del panel `contenido` para separar la tarjeta de `ImagenLateral` sin cambiar dimensiones de ventana.
- [ ] Reemplazar el JLabel único del resultado por una composición con «Resultado» y área azul vacía al limpiar; igualar el tamaño, color y espaciado de la etiqueta a las demás filas. `mostrarResultado` aplica tipografía grande al texto recibido.
- [ ] Revisar el diff para confirmar que eventos, servicios y catálogo siguen intactos.

### Task 2: Java REST Swing

**Files:**
- Modify: `04. CLIESC/CLI_ESC_CON_UNI_JAVA_REST/src/main/java/ec/edu/monster/vista/VentanaConversiones.java`

**Interfaces:** Conservar las firmas públicas `mostrarResultado(String texto)` y `limpiarResultado()`.

- [ ] Aplicar el mismo margen, tamaño y espaciado de etiqueta del cliente Java SOAP, manteniendo el distintivo REST.
- [ ] Revisar el diff para confirmar que eventos, servicios y catálogo siguen intactos.

### Task 3: .NET SOAP WinForms

**Files:**
- Modify: `04. CLIESC/CLI_ESC_CON_UNI_DOTNET_SOAP/ec/edu/monster/vista/ConversionForm.cs`
- Modify: `04. CLIESC/CLI_ESC_CON_UNI_DOTNET_SOAP/ec/edu/monster/vista/EstilosUI.cs` si la separación requiere ajustar el panel de imagen lateral.

**Interfaces:** Conservar las firmas públicas `MostrarResultado(double valorIngresado, RespuestaSoap respuesta)` y `LimpiarResultado()`.

- [ ] Ajustar el espacio horizontal entre tarjeta de conversión e imagen lateral sin cambiar el tamaño de ventana.
- [ ] Separar la etiqueta «Resultado» del panel de valor, con tamaño, color y espaciado iguales a las demás etiquetas; dejar el panel vacío al limpiar y destacar el resultado tras convertir.
- [ ] Revisar el diff para confirmar que eventos, servicios y catálogo siguen intactos.

### Task 4: .NET REST WinForms

**Files:**
- Modify: `04. CLIESC/CLI_ESC_CON_UNI_DOTNET_REST/ec/edu/monster/vista/ConversionForm.cs`
- Modify: `04. CLIESC/CLI_ESC_CON_UNI_DOTNET_REST/ec/edu/monster/vista/EstilosUI.cs` si la separación requiere ajustar el panel de imagen lateral.

**Interfaces:** Conservar las firmas públicas `MostrarResultado(double valorIngresado, RespuestaRest respuesta)` y `LimpiarResultado()`.

- [ ] Aplicar el mismo margen, tamaño y espaciado de etiqueta del cliente .NET SOAP, conservando la respuesta REST.
- [ ] Revisar el diff para confirmar que eventos, servicios y catálogo siguen intactos.

## Verificación

- Inspeccionar los cuatro diffs y comparar las implementaciones Java entre sí y las WinForms entre sí.
- No ejecutar pruebas ni builds, siguiendo el límite de alcance indicado.
