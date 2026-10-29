# Ajuste visual de los clientes de escritorio — 2026-10-28

## Intención
Unificar la presentación de las pantallas de conversión de escritorio de Java SOAP, .NET SOAP, Java REST y .NET REST, tomando como referencia el patrón visual del cliente web.

## Objetivos y límites
- Aumentar el espacio entre el contenido principal y la imagen lateral en los cuatro clientes.
- Mostrar la etiqueta «Resultado» sobre el área del resultado, con el mismo tamaño, color y separación que las demás etiquetas de campo.
- Mantener el área de resultado vacía antes de una conversión y al limpiarla; mostrar el valor convertido con tipografía destacada cuando esté disponible.
- Conservar los flujos, controles, servicios y estilos generales existentes. No rediseñar los clientes web ni la distribución completa de escritorio.

## Enfoques considerados
- **A. Ajustar el diseño actual (seleccionado):** retocar el margen del contenido y el componente de resultado propio de cada toolkit. Conserva estructura y comportamiento y reduce el alcance.
- **B. Reorganizar toda la composición para replicar la web:** daría una semejanza más amplia, pero cambia tamaños y acomodo fuera de lo solicitado.

Se eligió A para resolver los dos puntos indicados sin alterar el resto de las pantallas.

## Restricciones y decisiones de implementación
Java usa Swing y .NET usa WinForms; el resultado se implementará con los componentes existentes en cada entorno, buscando la misma jerarquía visual en los cuatro. Los estados inicial, posterior a limpiar o cambiar categoría y posterior a una conversión deben respetar el patrón descrito. El margen adicional debe mantenerse visible dentro de las dimensiones actuales de las ventanas.

## Criterio de aceptación
En cada uno de los cuatro clientes de escritorio se aprecia separación cómoda antes de la imagen; «Resultado» aparece con el mismo tamaño, color y espaciado que las demás etiquetas sobre un recuadro inicialmente vacío; al convertir, el valor ocupa el recuadro con mayor énfasis tipográfico. El cliente web permanece sin cambios.
