# Respuestas uniformes en clientes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Limitar resultados visibles a cuatro decimales y unificar su panel azul con texto blanco en los 16 clientes.

**Architecture:** El formato se aplica en la capa de presentación de cada cliente; los servicios y los valores de cálculo permanecen intactos. Web, escritorio y móvil compartirán el aspecto azul/blanco del panel web existente. La consola presentará el resultado en una franja azul con texto blanco cuando sea interactiva, manteniendo su salida legible al redirigirla.

**Tech Stack:** Java Swing, Android, Java web/JSP; .NET WinForms, MAUI, ASP.NET MVC/Razor; consola Java y .NET.

**Spec:** Requerimiento del usuario en esta conversación (2026-10-28).

## Global Constraints

- Alcance: consola, escritorio, móvil y web en cada proyecto SOAP/REST Java/.NET (16 clientes).
- Máximo cuatro decimales, redondeo al presentar y sin ceros finales innecesarios.
- Panel de resultado azul con texto blanco en todas las interfaces; terminal mantiene salida simple al ser redirigida.
- No modificar servicios, cálculos, contratos, pruebas ni los dos `web.xml` que ya estaban modificados.

## Review Focus

- Resultados enteros o con menos de cuatro decimales: no añadir ceros de relleno.
- Resultados negativos y valores grandes: conservar signo y limitar la parte decimal a cuatro posiciones.
- Consolas redirigidas: no insertar secuencias de color en archivos o tuberías.
- Texto inicial/sin resultado y errores: mantener legibles en el panel azul y no mezclarlos con el estilo de éxito.

### Task 1: Consolas Java y .NET

**Files:** `VistaConsola.java` y `VistaConsola.cs` de los cuatro clientes de consola.

- [x] Presentar los resultados con formato de hasta cuatro decimales, usando separador decimal invariante.
- [x] Mostrar el resultado en azul/blanco cuando la salida sea interactiva y conservar salida plana si está redirigida.

### Task 2: Escritorio Java Swing y .NET WinForms

**Files:** vistas/controladores de resultados y estilos de ambos clientes de escritorio en los cuatro proyectos.

- [x] Normalizar el formato mostrado a cuatro decimales máximos.
- [x] Igualar el fondo azul y el texto blanco del panel de resultado al panel web.

### Task 3: Móvil Android y .NET MAUI

**Files:** vistas/controladores de conversión Android y MAUI en los cuatro proyectos.

- [x] Normalizar el formato mostrado a cuatro decimales máximos.
- [x] Aplicar el mismo panel de resultado azul con texto blanco.

### Task 4: Web Java y ASP.NET

**Files:** vistas/controladores y CSS de ambos clientes web en los cuatro proyectos.

- [x] Formatear los resultados numéricos mostrados a cuatro decimales máximos.
- [x] Usar las mismas reglas visuales del panel azul/blanco en las cuatro páginas web.

### Task 5: Revisión final

- [x] Revisar las diferencias para confirmar que solo cambian clientes y que se preservan los cambios previos de `web.xml`.
- [x] No ejecutar pruebas automatizadas en esta solicitud; no se ejecutaron.
