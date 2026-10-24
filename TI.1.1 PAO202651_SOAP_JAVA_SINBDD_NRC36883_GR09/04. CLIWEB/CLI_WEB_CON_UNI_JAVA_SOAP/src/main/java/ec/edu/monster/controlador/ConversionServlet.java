package ec.edu.monster.controlador;
import ec.edu.monster.servicio.ConversorWebService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        request.setAttribute("categoriaSeleccionada", categoria);
        request.setAttribute("origenSeleccionado", origen);
        request.setAttribute("destinoSeleccionado", destino);

        try {
            double valor = Double.parseDouble(
                    request.getParameter("valor").trim().replace(',', '.'));
            if (!Double.isFinite(valor)) {
                throw new NumberFormatException();
            }

            var respuesta = api().convertir(token(request), categoria, origen, destino, valor);
            if (respuesta.isSesionInvalida()) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (respuesta.isExito()) {
                request.setAttribute("resultado", respuesta);
            } else {
                request.setAttribute("error", respuesta.mensaje());
            }
        } catch (NumberFormatException | NullPointerException exception) {
            request.setAttribute("error", "Escribe un valor numérico válido.");
        } catch (IOException | IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
        }

        cargar(request, response);
    }

    private void cargar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = token(request);
        try {
            var respuesta = api().obtenerCategorias(token);
            if (respuesta.isSesionInvalida()) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (!respuesta.isExito()) {
                request.setAttribute("error", respuesta.mensaje());
            } else {
                request.setAttribute("categorias", respuesta.categorias());
                String categoria = (String) request.getAttribute("categoriaSeleccionada");
                if (categoria == null || categoria.isBlank()) {
                    categoria = respuesta.categorias().isEmpty()
                            ? ""
                            : respuesta.categorias().get(0);
                }
                request.setAttribute("categoriaSeleccionada", categoria);

                if (!categoria.isBlank()) {
                    var unidades = api().obtenerUnidades(token, categoria);
                    if (unidades.isSesionInvalida()) {
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath() + "/login");
                        return;
                    }
                    if (unidades.isExito()) {
                        request.setAttribute("unidades", unidades.unidades());
                    } else {
                        request.setAttribute("error", unidades.mensaje());
                    }
                }
            }
        } catch (IOException | IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/vista/conversor.jsp").forward(request, response);
    }

    private ConversorWebService api() {
        return new ConversorWebService(getServletContext());
    }

    private String token(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("token");
    }
}
