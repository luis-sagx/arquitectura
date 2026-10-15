package ec.edu.monster.controlador;
import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet("/inicio") public class InicioServlet extends HttpServlet{protected void doGet(HttpServletRequest q,HttpServletResponse s)throws IOException{String url=q.getSession().getAttribute("token")==null?"/login":"/conversor";s.sendRedirect(q.getContextPath()+url);}}
