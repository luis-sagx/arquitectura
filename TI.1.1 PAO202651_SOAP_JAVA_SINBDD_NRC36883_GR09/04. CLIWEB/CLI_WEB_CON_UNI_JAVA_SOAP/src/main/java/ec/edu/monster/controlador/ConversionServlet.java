package ec.edu.monster.controlador;
import ec.edu.monster.servicio.ConversorWebService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                Map<String, List<String>> catalogoCompleto = new LinkedHashMap<>();
                for (String categoriaCatalogo : respuesta.categorias()) {
                    var detalleCatalogo = api().obtenerUnidades(token, categoriaCatalogo);
                    if (detalleCatalogo.isSesionInvalida()) {
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath() + "/login");
                        return;
                    }
                    if (detalleCatalogo.isExito()) catalogoCompleto.put(categoriaCatalogo, detalleCatalogo.unidades());
                }
                request.setAttribute("catalogoCompleto", catalogoCompleto);
                List<String> convertibles = respuesta.categorias().stream()
                    .filter(c -> List.of("LONGITUD", "MASA", "TEMPERATURA").contains(c)).toList();
                request.setAttribute("categorias", convertibles);
                String categoria = (String) request.getAttribute("categoriaSeleccionada");
                if (categoria == null || !convertibles.contains(categoria)) {
                    categoria = convertibles.isEmpty()
                            ? ""
                            : convertibles.get(0);
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
