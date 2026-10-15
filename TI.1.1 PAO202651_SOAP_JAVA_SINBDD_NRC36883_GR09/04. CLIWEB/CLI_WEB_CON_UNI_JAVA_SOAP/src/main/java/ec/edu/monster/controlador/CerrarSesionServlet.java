package ec.edu.monster.controlador;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet("/salir") public class CerrarSesionServlet extends HttpServlet{protected void doPost(HttpServletRequest q,HttpServletResponse s)throws IOException{if(q.getSession(false)!=null)q.getSession(false).invalidate();s.sendRedirect(q.getContextPath()+"/login");}}
