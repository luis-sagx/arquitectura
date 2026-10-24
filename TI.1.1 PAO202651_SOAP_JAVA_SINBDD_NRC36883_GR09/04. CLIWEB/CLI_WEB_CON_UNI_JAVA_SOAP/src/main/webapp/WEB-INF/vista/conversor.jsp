<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, ec.edu.monster.modelo.RespuestaSoap, ec.edu.monster.vista.VistaHtml" %>
<%!
    private String esc(String valor) {
        return VistaHtml.escapar(valor);
    }

    private String sel(String actual, String seleccionado) {
        return actual != null && actual.equals(seleccionado) ? " selected" : "";
    }
%>
<%
    List<String> categorias = (List<String>) request.getAttribute("categorias");
    List<String> unidades = (List<String>) request.getAttribute("unidades");
    String categoria = (String) request.getAttribute("categoriaSeleccionada");
    String origen = (String) request.getAttribute("origenSeleccionado");
    String destino = (String) request.getAttribute("destinoSeleccionado");
    RespuestaSoap resultado = (RespuestaSoap) request.getAttribute("resultado");
%>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Conversor de unidades</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/recursos/css/estilos.css">
</head>
<body>
    <header class="bar">
                    <a href="<%= request.getContextPath() %>/conversor">Conversor</a>
        <form method="post" action="<%= request.getContextPath() %>/salir">
            <input type="hidden" name="csrf"
                   value="<%= esc((String) session.getAttribute("csrf")) %>">
            <button class="secondary">Cerrar sesión</button>
        </form>
    </header>

    <div class="screen-layout screen-layout--workspace">
        <main class="wrap">
            <h1>Convierte unidades</h1>
            <p class="muted">El catálogo y el cálculo se consultan en el servidor.</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="notice error"><%= esc((String) request.getAttribute("error")) %></div>
            <% } %>

            <section class="panel">
                <form method="get" action="<%= request.getContextPath() %>/conversor">
                    <label for="categoria">Categoría</label>
                    <select id="categoria" name="categoria" onchange="this.form.submit()">
                        <% if (categorias != null) { %>
                            <% for (String valorCategoria : categorias) { %>
                                <option value="<%= esc(valorCategoria) %>"<%=
                                        sel(valorCategoria, categoria)
                                %>><%= esc(valorCategoria) %></option>
                            <% } %>
                        <% } %>
                    </select>
                    <noscript><button class="secondary">Cargar</button></noscript>
                </form>

                <form method="post" action="<%= request.getContextPath() %>/conversor">
                    <input type="hidden" name="csrf"
                           value="<%= esc((String) session.getAttribute("csrf")) %>">
                    <input type="hidden" name="categoria" value="<%= esc(categoria) %>">
                    <label for="valor">Valor</label>
                    <input id="valor" name="valor" type="number" step="any" required
                           value="<%= esc(request.getParameter("valor")) %>">

                    <div class="grid">
                        <div>
                            <label for="origen">Unidad de origen</label>
                            <select id="origen" name="origen" required>
                                <% if (unidades != null) { %>
                                    <% for (String unidad : unidades) { %>
                                        <option value="<%= esc(unidad) %>"<%=
                                                sel(unidad, origen)
                                        %>><%= esc(unidad) %></option>
                                    <% } %>
                                <% } %>
                            </select>
                        </div>
                        <div>
                            <label for="destino">Unidad de destino</label>
                            <select id="destino" name="destino" required>
                                <% if (unidades != null) { %>
                                    <% for (String unidad : unidades) { %>
                                        <option value="<%= esc(unidad) %>"<%=
                                                sel(unidad, destino)
                                        %>><%= esc(unidad) %></option>
                                    <% } %>
                                <% } %>
                            </select>
                        </div>
                    </div>
                    <button>Convertir</button>
                </form>
            </section>

            <% if (resultado != null) { %>
                <section class="result">
                    <p class="eyebrow">Resultado</p>
                    <strong><%= resultado.resultado() %></strong>
                    <p><%= esc(resultado.unidadOrigen()) %> → <%= esc(resultado.unidadDestino()) %></p>
                </section>
            <% } %>
        </main>
        <aside class="visual-sidebar">
            <img src="<%= request.getContextPath() %>/recursos/img/monster.jpg"
                 alt="Monster usando una computadora">
        </aside>
    </div>
</body>
</html>
