<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="ec.edu.monster.vista.VistaHtml" %><%!
private String esc(String valor) { return VistaHtml.escapar(valor); }
%>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Recuperar clave · Conversor REST</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/recursos/css/estilos.css">
</head>
<body>
<div class="screen-layout screen-layout--auth"><section class="screen-content"><main class="panel auth">
  <p class="eyebrow">Conversor de unidades · REST</p>
  <h1>Recupera tu clave</h1>
  <p class="muted">Ingresa tu usuario y define una nueva clave.</p>
  <% if (request.getAttribute("error") != null) { %>
    <div class="notice error" role="alert"><%=esc((String) request.getAttribute("error"))%></div>
  <% } %>
  <% if (request.getAttribute("exito") != null) { %>
    <div class="notice success" role="status"><%=esc((String) request.getAttribute("exito"))%></div>
  <% } %>
  <form method="post" action="<%=request.getContextPath()%>/recuperar-clave">
    <input type="hidden" name="csrf" value="<%=esc((String) session.getAttribute("csrf"))%>">
    <label for="usuario">Usuario</label>
    <input id="usuario" name="usuario" autocomplete="username" required maxlength="100">
    <label for="clave">Nueva clave</label>
    <input id="clave" name="clave" type="password" autocomplete="new-password" required maxlength="200">
    <label for="confirmar">Confirma la clave</label>
    <input id="confirmar" name="confirmar" type="password" autocomplete="new-password" required maxlength="200">
    <button type="submit">Actualizar clave</button>
  </form>
  <p><a class="link-button link-button--back" href="<%=request.getContextPath()%>/login">Volver al inicio</a></p>
</main></section><aside class="visual-sidebar"><img src="<%=request.getContextPath()%>/recursos/img/monster.jpg" alt="Monster usando una computadora"></aside></div>
</body>
</html>
