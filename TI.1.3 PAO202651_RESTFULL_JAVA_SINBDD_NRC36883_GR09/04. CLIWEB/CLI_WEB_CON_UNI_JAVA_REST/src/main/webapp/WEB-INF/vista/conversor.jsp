<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="java.util.List,java.util.Map,ec.edu.monster.modelo.RespuestaRest,ec.edu.monster.vista.VistaHtml" %><%!
private String esc(String valor) { return VistaHtml.escapar(valor); }
private String sel(String actual, String opcion) { return actual != null && actual.equals(opcion) ? " selected" : ""; }
private String unidadLegible(String id) { String nombre=id.replace('_',' ').toLowerCase(java.util.Locale.ROOT); nombre=nombre.substring(0,1).toUpperCase(java.util.Locale.ROOT)+nombre.substring(1); String simbolo=switch(id){case "KILOMETRO"->"km";case "METRO"->"m";case "CENTIMETRO"->"cm";case "MILIMETRO"->"mm";case "MILLA"->"mi";case "PIE"->"ft";case "KILOGRAMO"->"kg";case "GRAMO"->"g";case "MILIGRAMO"->"mg";case "LIBRA"->"lb";case "CELSIUS"->"°C";case "FAHRENHEIT"->"°F";case "KELVIN"->"K";case "MILILITRO"->"mL";case "LITRO"->"L";case "METRO_CUBICO"->"m³";case "GALON_US"->"gal";case "SEGUNDO"->"s";case "MINUTO"->"min";case "HORA"->"h";case "DIA"->"d";case "METRO_CUADRADO"->"m²";case "KILOMETRO_CUADRADO"->"km²";case "HECTAREA"->"ha";case "ACRE"->"ac";default->"";};return simbolo.isEmpty()?nombre:nombre+" ("+simbolo+")"; }
private String categoriaLegible(String id) { return switch(id){case "LONGITUD"->"Longitud";case "MASA"->"Masa/peso";case "VOLUMEN"->"Volumen";case "TIEMPO"->"Tiempo";case "TEMPERATURA"->"Temperatura";case "AREA"->"Área";default->id;}; }
%><%
List<String> categorias = (List<String>) request.getAttribute("categorias");
List<String> unidades = (List<String>) request.getAttribute("unidades");
String categoria = (String) request.getAttribute("categoriaSeleccionada");
String origen = (String) request.getAttribute("origenSeleccionado");
String destino = (String) request.getAttribute("destinoSeleccionado");
String valor = (String) request.getAttribute("valorIngresado");
RespuestaRest resultado = (RespuestaRest) request.getAttribute("resultado");
Map<String,List<String>> catalogoCompleto = (Map<String,List<String>>) request.getAttribute("catalogoCompleto");
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
<div class="screen-layout screen-layout--workspace"><main class="wrap">
  <h1>Convierte unidades</h1>
  <p class="muted">El catálogo y el cálculo se consultan en el servidor REST.</p>
  <% if (request.getAttribute("error") != null) { %>
    <div class="notice error" role="alert"><%=esc((String) request.getAttribute("error"))%></div>
  <% } %>
            <div id="conversionView">
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
      <div class="form-actions">
                <button>Convertir</button>
                <button type="button" class="secondary" onclick="mostrarCatalogo(true)">Ver catalogo</button>
            </div>
    </form>
  </section>
  <% if (resultado != null) { %>
    <section class="result" aria-live="polite">
      <p class="eyebrow">Resultado</p>
      <strong><%=VistaHtml.formatearResultado(resultado.getResultado())%></strong>
      <p><%=esc(resultado.getUnidadOrigen())%> &rarr; <%=esc(resultado.getUnidadDestino())%></p>
    </section>
  <% } %>
  </div>
  <section id="catalogView" class="catalog-view" hidden>
    <button type="button" class="secondary" onclick="mostrarCatalogo(false)">Volver al conversor</button>
    <h2>Catálogo de unidades</h2>
    <div class="catalog-grid">
      <% if (catalogoCompleto != null) for (Map.Entry<String, List<String>> grupo : catalogoCompleto.entrySet()) { %>
        <article class="catalog-card"><h3><%=esc(categoriaLegible(grupo.getKey()))%></h3>
          <ul><% for (String unidad : grupo.getValue()) { %><li><%=esc(unidadLegible(unidad))%></li><% } %></ul>
        </article>
      <% } %>
    </div>
    <p class="muted">Las conversiones están habilitadas para longitud, masa/peso y temperatura.</p>
  </section></main><aside class="visual-sidebar"><img src="<%=request.getContextPath()%>/recursos/img/monster.jpg" alt="Monster usando una computadora"></aside></div>
    <script>
    function mostrarCatalogo(mostrar) {
        document.getElementById('conversionView').hidden = mostrar;
  document.querySelector('.wrap > h1').hidden = mostrar;
  document.querySelector('.wrap > p.muted').hidden = mostrar;
  var aviso = document.querySelector('.wrap > .notice'); if (aviso) aviso.hidden = mostrar;
        document.getElementById('catalogView').hidden = !mostrar;
        document.querySelector('.visual-sidebar').hidden = mostrar;
  document.querySelector('.visual-sidebar').style.display = mostrar ? 'none' : '';
        document.querySelector('.screen-layout').classList.toggle('catalog-mode', mostrar);
    }
    </script></body>
</html>
