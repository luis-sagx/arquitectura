package ec.edu.monster.controlador;
import ec.edu.monster.servicio.ConversorWebService;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet("/recuperar-clave") public class RecuperacionServlet extends HttpServlet{
 protected void doGet(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{q.getRequestDispatcher("/WEB-INF/vista/recuperar-clave.jsp").forward(q,s);}
 protected void doPost(HttpServletRequest q,HttpServletResponse s)throws ServletException,IOException{String u=q.getParameter("usuario"),c=q.getParameter("clave"),v=q.getParameter("confirmar");if(u==null||u.isBlank()||c==null||c.isBlank())q.setAttribute("error","Completa todos los campos.");else if(!c.equals(v))q.setAttribute("error","Las claves no coinciden.");else try{var r=new ConversorWebService(getServletContext()).recuperarClave(u.trim(),c);q.setAttribute(r.isExito()?"exito":"error",r.mensaje());}catch(IOException|IllegalArgumentException e){q.setAttribute("error",e.getMessage());}doGet(q,s);}
}
