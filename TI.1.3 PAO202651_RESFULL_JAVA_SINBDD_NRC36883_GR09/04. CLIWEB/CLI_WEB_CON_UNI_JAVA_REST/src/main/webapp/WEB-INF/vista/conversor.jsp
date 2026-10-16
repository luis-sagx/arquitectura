<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,ec.edu.monster.modelo.RespuestaRest,ec.edu.monster.vista.VistaHtml" %><%!
private String esc(String valor) { return VistaHtml.escapar(valor); }
private String sel(String actual, String opcion) { return actual != null && actual.equals(opcion) ? " selected" : ""; }
%><%
List<String> categorias = (List<String>) request.getAttribute("categorias");
List<String> unidades = (List<String>) request.getAttribute("unidades");
String categoria = (String) request.getAttribute("categoriaSeleccionada");
String origen = (String) request.getAttribute("origenSeleccionado");
String destino = (String) request.getAttribute("destinoSeleccionado");
String valor = (String) request.getAttribute("valorIngresado");
RespuestaRest resultado = (RespuestaRest) request.getAttribute("resultado");
%>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Conversor de unidades · REST</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/recursos/css/estilos.css">
</head>
<body>
<header class="bar">
  <a href="<%=request.getContextPath()%>/conversor">Conversor REST</a>
  <form method="post" action="<%=request.getContextPath()%>/cerrar-sesion">
    <input type="hidden" name="csrf" value="<%=esc((String) session.getAttribute("csrf"))%>">
    <button class="secondary" type="submit">Cerrar sesión</button>
  </form>
</header>
<main class="wrap">
  <p class="eyebrow">Conversión</p>
  <h1>Convierte unidades</h1>
  <p class="muted">El catálogo y el cálculo se consultan en el servidor REST.</p>
  <% if (request.getAttribute("error") != null) { %>
    <div class="notice error" role="alert"><%=esc((String) request.getAttribute("error"))%></div>
  <% } %>
  <section class="panel">
    <form method="get" action="<%=request.getContextPath()%>/conversor">
      <label for="categoria">Categoría</label>
      <select id="categoria" name="categoria" onchange="this.form.submit()" required>
        <% if (categorias != null) for (String opcion : categorias) { %>
          <option value="<%=esc(opcion)%>"<%=sel(categoria, opcion)%>><%=esc(opcion)%></option>
        <% } %>
      </select>
      <noscript><button class="secondary" type="submit">Cargar unidades</button></noscript>
    </form>
    <form method="post" action="<%=request.getContextPath()%>/conversor">
      <input type="hidden" name="csrf" value="<%=esc((String) session.getAttribute("csrf"))%>">
      <input type="hidden" name="categoria" value="<%=esc(categoria)%>">
      <label for="valor">Valor</label>
      <input id="valor" name="valor" type="text" inputmode="decimal" required maxlength="50" value="<%=esc(valor)%>">
      <div class="grid">
        <div>
          <label for="origen">Unidad de origen</label>
          <select id="origen" name="origen" required>
            <% if (unidades != null) for (String unidad : unidades) { %>
              <option value="<%=esc(unidad)%>"<%=sel(origen, unidad)%>><%=esc(unidad)%></option>
            <% } %>
          </select>
        </div>
        <div>
          <label for="destino">Unidad de destino</label>
          <select id="destino" name="destino" required>
            <% if (unidades != null) for (String unidad : unidades) { %>
              <option value="<%=esc(unidad)%>"<%=sel(destino, unidad)%>><%=esc(unidad)%></option>
            <% } %>
          </select>
        </div>
      </div>
      <button type="submit">Convertir</button>
    </form>
  </section>
  <% if (resultado != null) { %>
    <section class="result" aria-live="polite">
      <p class="eyebrow">Resultado</p>
      <strong><%=resultado.getResultado()%></strong>
      <p><%=esc(resultado.getUnidadOrigen())%> &rarr; <%=esc(resultado.getUnidadDestino())%></p>
    </section>
  <% } %>
</main>
</body>
</html>
