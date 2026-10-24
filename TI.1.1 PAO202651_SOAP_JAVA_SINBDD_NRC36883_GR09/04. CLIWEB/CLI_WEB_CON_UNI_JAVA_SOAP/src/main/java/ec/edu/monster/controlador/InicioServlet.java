package ec.edu.monster.controlador;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/inicio")
public class InicioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String ruta = request.getSession().getAttribute("token") == null
                ? "/login"
                : "/conversor";
        response.sendRedirect(request.getContextPath() + ruta);
    }
}
