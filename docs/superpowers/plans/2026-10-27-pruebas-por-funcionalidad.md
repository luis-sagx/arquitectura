# Pruebas por funcionalidad Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Organizar y completar pruebas ejecutables en archivos separados por funcionalidad para los 16 clientes y 4 servidores de TI.1.1 a TI.1.4.

**Architecture:** Mantener el estilo existente de cada proyecto. Java usa clases de prueba independientes y scripts PowerShell existentes o nuevos; .NET usa archivos C# independientes dentro de los proyectos de pruebas. Los clientes prueban adaptadores SOAP/REST mediante servicios locales simulados, sin requerir una interfaz de usuario ni un servidor de aplicación externo.

**Tech Stack:** Java 21, Maven/Gradle donde ya se usan; C#/.NET Framework y .NET SDK; PowerShell para runners.

**Spec:** Solicitud del usuario del 2026-10-27: un archivo por funcionalidad como las pruebas del servidor TI.1.1, en todos los clientes y servidores.

## Global Constraints

- No cambiar el comportamiento de producción ni añadir dependencias de prueba externas.
- Mantener un archivo independiente por área funcional: autenticación, recuperación de clave, catálogo, conversiones/validaciones y transporte/contrato cuando aplique.
- Las pruebas deben ser locales y repetibles, sin necesitar servicios remotos.
- Conservar los proyectos Java y .NET en sus toolchains ya configurados.

## Review Focus

- Los clientes de cada tecnología tienen rutas y modelos de transporte diferentes; cada suite debe apuntar al adaptador de ese proyecto.
- Android/Maui no deben requerir cargar UI para verificar llamadas de servicio.
- Los runners deben ejecutar todos los archivos funcionales y propagar fallos.
- Las suites Java de servidor existentes bajo `src/main/java` no deben duplicarse ni borrarse.
- Los clientes o servidores con suite actual deben conservar su cobertura al separarla.

---

### Task 1: Servidores Java

**Files:** Mantener `src/main/java/ec/edu/monster/pruebas/Prueba*.java` en TI.1.1 y TI.1.3; actualizar ambos `ejecutar-pruebas.ps1` para ejecutar cada `Prueba*.java` como suite independiente.

- [ ] Mantener los archivos de prueba actuales, uno por funcionalidad.
- [ ] Cambiar los runners para compilar utilidades y pruebas y ejecutar cada clase de prueba.

### Task 2: Servidor TI.1.2 SOAP .NET

**Files:** Crear archivos `Prueba*.cs` bajo `ec/edu/monster/pruebas`; actualizar `Ejecutar-Pruebas.ps1`.

- [ ] Separar la suite monolítica en autenticación, catálogo, conversiones/validaciones, sesión/recuperación y contrato WSDL.
- [ ] Mantener una utilidad compartida de aserciones y un runner que invoque las suites por separado.

### Task 3: Servidor TI.1.4 REST .NET

**Files:** Separar `PruebasServidor.cs` bajo `ec/edu/monster/pruebas` y ajustar el proyecto de pruebas.

- [ ] Dividir las pruebas existentes por endpoint/funcionalidad en archivos ejecutables independientes.
- [ ] Conservar la referencia al servidor y la configuración actual del test project.

### Task 4: Clientes Java SOAP y REST

**Files:** Agregar pruebas bajo `src/test/java/ec/edu/monster/pruebas` para CLICON, CLIESC, CLIWEB y CLIMOV en TI.1.1 y TI.1.3.

- [ ] Crear suites independientes para autenticación, recuperación, catálogo, conversiones y manejo de errores del adaptador.
- [ ] Reutilizar un servidor local simulado por proyecto y agregar runners compatibles con Maven o Gradle.
- [ ] Mantener las pruebas existentes del cliente TI.1.1 CLICON e incorporar sus suites al runner.

### Task 5: Clientes .NET SOAP y REST

**Files:** Agregar suites bajo `ec/edu/monster/pruebas` en los cuatro clientes de TI.1.2 y TI.1.4.

- [ ] Crear pruebas independientes para autenticación, recuperación, catálogo, conversiones y errores del transporte.
- [ ] Configurar proyectos de pruebas ejecutables que referencien el cliente correspondiente y usar handlers/servicios locales simulados.
- [ ] Mantener las pruebas existentes de TI.1.2 CLIESC y TI.1.4 CLIESC e incorporarlas al runner.

### Task 6: Revisión estática de cobertura y cambios

- [ ] Confirmar por inspección que cada cliente y servidor contiene carpeta `pruebas` con varios archivos por funcionalidad.
- [ ] Revisar `git diff --check` y reportar que las suites no se ejecutaron en esta solicitud.
