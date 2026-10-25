package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ConversorWebService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class AutenticacionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession().getAttribute("token") != null) {
            response.sendRedirect(request.getContextPath() + "/conversor");
            return;
        }
        if ("expirada".equals(request.getParameter("sesion"))) {
            request.setAttribute("error", "Tu sesión venció. Inicia sesión nuevamente.");
        }
        request.getRequestDispatcher("/WEB-INF/vista/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = request.getParameter("usuario");
        String clave = request.getParameter("clave");
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            request.setAttribute("error", "Ingresa usuario y clave.");
            doGet(request, response);
            return;
        }
        try {
            RespuestaRest resultado = api().autenticar(usuario.trim(), clave);
            if (resultado.esExito() && resultado.getToken() != null && !resultado.getToken().isBlank()) {
                request.changeSessionId();
                request.getSession().setAttribute("token", resultado.getToken());
                request.getSession().setAttribute("expiraEn", resultado.getExpiraEn());
                response.sendRedirect(request.getContextPath() + "/conversor");
                return;
            }
            request.setAttribute("error", resultado.mensajeSeguro());
        } catch (IOException | IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
        }
        doGet(request, response);
    }

    private ConversorWebService api() {
        return new ConversorWebService(getServletContext());
    }
}
