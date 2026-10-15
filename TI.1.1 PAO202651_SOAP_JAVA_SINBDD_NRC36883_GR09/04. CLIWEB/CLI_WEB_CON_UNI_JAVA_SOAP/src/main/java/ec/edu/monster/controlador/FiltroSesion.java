package ec.edu.monster.controlador;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebFilter;import jakarta.servlet.http.*;import java.io.IOException;
@WebFilter("/conversor") public class FiltroSesion implements Filter{
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{var q=(HttpServletRequest)request;var s=(HttpServletResponse)response;var session=q.getSession(false);if(session==null||session.getAttribute("token")==null){s.sendRedirect(q.getContextPath()+"/login");return;}chain.doFilter(q,s);}
}
