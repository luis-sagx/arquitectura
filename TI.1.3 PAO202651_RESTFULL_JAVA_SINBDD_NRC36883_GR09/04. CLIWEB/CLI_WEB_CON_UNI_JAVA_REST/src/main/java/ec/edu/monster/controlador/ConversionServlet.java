package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ConversorWebService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/conversor")
public class ConversionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("categoriaSeleccionada", request.getParameter("categoria"));
        cargar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String categoria = request.getParameter("categoria");
        String origen = request.getParameter("origen");
        String destino = request.getParameter("destino");
        String valorTexto = request.getParameter("valor");
        request.setAttribute("categoriaSeleccionada", categoria);
        request.setAttribute("origenSeleccionado", origen);
        request.setAttribute("destinoSeleccionado", destino);
        request.setAttribute("valorIngresado", valorTexto);

        try {
            double valor = Double.parseDouble(valorTexto == null ? "" : valorTexto.trim().replace(',', '.'));
            if (!Double.isFinite(valor)) {
                request.setAttribute("error", "Escribe un valor numérico finito.");
            } else {
                RespuestaRest resultado = api().convertir(token(request), categoria, origen, destino, valor);
                if (resultado.esSesionInvalida()) {
                    irALoginPorSesion(request, response);
                    return;
                }
                if (resultado.esExito()) {
                    request.setAttribute("resultado", resultado);
                } else {
                    request.setAttribute("error", resultado.mensajeSeguro());
                }
            }
        } catch (NumberFormatException e) {
            request.setAttribute("error", "Escribe un valor numérico válido.");
        } catch (IOException | IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }
        cargar(request, response);
    }

    private void cargar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = token(request);
        try {
            RespuestaRest respuestaCategorias = api().obtenerCategorias(token);
            if (respuestaCategorias.esSesionInvalida()) {
                irALoginPorSesion(request, response);
                return;
            }
            if (!respuestaCategorias.esExito()) {
                ponerError(request, respuestaCategorias.mensajeSeguro());
                mostrar(request, response);
                return;
            }

            List<String> categorias = respuestaCategorias.getCategorias() == null
                ? List.of() : respuestaCategorias.getCategorias();
            request.setAttribute("categorias", categorias);
            String categoria = (String) request.getAttribute("categoriaSeleccionada");
            if (categoria == null || !categorias.contains(categoria)) {
                categoria = categorias.isEmpty() ? "" : categorias.get(0);
            }
            request.setAttribute("categoriaSeleccionada", categoria);

            if (!categoria.isBlank()) {
                RespuestaRest respuestaUnidades = api().obtenerUnidades(token, categoria);
                if (respuestaUnidades.esSesionInvalida()) {
                    irALoginPorSesion(request, response);
                    return;
                }
                if (respuestaUnidades.esExito()) {
                    request.setAttribute("unidades", respuestaUnidades.getUnidades());
                } else {
                    ponerError(request, respuestaUnidades.mensajeSeguro());
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            ponerError(request, e.getMessage());
        }
        mostrar(request, response);
    }

    private void mostrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vista/conversor.jsp").forward(request, response);
    }

    private void irALoginPorSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
        response.sendRedirect(request.getContextPath() + "/login?sesion=expirada");
    }

    private void ponerError(HttpServletRequest request, String error) {
        if (request.getAttribute("error") == null) request.setAttribute("error", error);
    }

    private String token(HttpServletRequest request) {
        return (String) request.getSession(false).getAttribute("token");
    }

    private ConversorWebService api() {
        return new ConversorWebService(getServletContext());
    }
}
