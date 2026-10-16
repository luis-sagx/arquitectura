package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ConversorWebService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/recuperar-clave")
public class RecuperacionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vista/recuperar-clave.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = request.getParameter("usuario");
        String clave = request.getParameter("clave");
        String confirmar = request.getParameter("confirmar");
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            request.setAttribute("error", "Completa todos los campos.");
        } else if (!clave.equals(confirmar)) {
            request.setAttribute("error", "Las claves no coinciden.");
        } else {
            try {
                RespuestaRest resultado = api().recuperarClave(usuario.trim(), clave);
                request.setAttribute(resultado.esExito() ? "exito" : "error", resultado.mensajeSeguro());
            } catch (IOException | IllegalArgumentException e) {
                request.setAttribute("error", e.getMessage());
            }
        }
        doGet(request, response);
    }

    private ConversorWebService api() {
        return new ConversorWebService(getServletContext());
    }
}
