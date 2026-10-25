package ec.edu.monster.controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/cerrar-sesion")
public class CerrarSesionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var sesion = request.getSession(false);
        if (sesion != null) sesion.invalidate();
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
