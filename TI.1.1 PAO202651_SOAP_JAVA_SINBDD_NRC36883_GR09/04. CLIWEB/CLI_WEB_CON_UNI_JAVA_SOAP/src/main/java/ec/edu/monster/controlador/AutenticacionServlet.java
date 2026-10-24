package ec.edu.monster.controlador;

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
            var respuesta = new ConversorWebService(getServletContext())
                    .autenticar(usuario.trim(), clave);
            if (respuesta.isExito() && !respuesta.token().isBlank()) {
                request.changeSessionId();
                request.getSession().setAttribute("token", respuesta.token());
                request.getSession().setAttribute("expiraEn", respuesta.expiraEn());
                response.sendRedirect(request.getContextPath() + "/conversor");
                return;
            }

            request.setAttribute("error", respuesta.mensaje());
        } catch (IOException | IllegalArgumentException exception) {
            request.setAttribute("error", exception.getMessage());
        }

        doGet(request, response);
    }
}
