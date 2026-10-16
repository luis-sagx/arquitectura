<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page import="ec.edu.monster.vista.VistaHtml" %><%!
private String esc(String valor) { return VistaHtml.escapar(valor); }
%>
<!doctype html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Iniciar sesión · Conversor REST</title>
  <link rel="stylesheet" href="<%=request.getContextPath()%>/recursos/css/estilos.css">
</head>
<body>
<main class="panel auth">
  <p class="eyebrow">Conversor de unidades · REST</p>
  <h1>Inicia sesión</h1>
  <p class="muted">Convierte longitud, masa y temperatura.</p>
  <% if (request.getAttribute("error") != null) { %>
    <div class="notice error" role="alert"><%=esc((String) request.getAttribute("error"))%></div>
  <% } %>
  <form method="post" action="<%=request.getContextPath()%>/login">
    <input type="hidden" name="csrf" value="<%=esc((String) session.getAttribute("csrf"))%>">
    <label for="usuario">Usuario</label>
    <input id="usuario" name="usuario" autocomplete="username" required maxlength="100">
    <label for="clave">Clave</label>
    <input id="clave" name="clave" type="password" autocomplete="current-password" required maxlength="200">
    <button type="submit">Entrar</button>
  </form>
  <p><a class="link-button" href="<%=request.getContextPath()%>/recuperar-clave">Recuperar clave</a></p>
</main>
</body>
</html>
