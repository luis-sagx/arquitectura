package ec.edu.monster.controlador;
import ec.edu.monster.servicio.ConversorWebService;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet("/login") public class AutenticacionServlet extends HttpServlet{
 protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{q.getRequestDispatcher("/WEB-INF/vista/login.jsp").forward(q,s);}
 protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{String u=q.getParameter("usuario"),c=q.getParameter("clave");if(u==null||u.isBlank()||c==null||c.isBlank()){q.setAttribute("error","Ingresa usuario y clave.");doGet(q,s);return;}try{var r=new ConversorWebService(getServletContext()).autenticar(u.trim(),c);if(r.isExito()&&!r.token().isBlank()){q.changeSessionId();q.getSession().setAttribute("token",r.token());q.getSession().setAttribute("expiraEn",r.expiraEn());s.sendRedirect(q.getContextPath()+"/conversor");return;}q.setAttribute("error",r.mensaje());}catch(IOException|IllegalArgumentException e){q.setAttribute("error",e.getMessage());}doGet(q,s);}
}
