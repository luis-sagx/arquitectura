package ec.edu.monster.controlador;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebFilter;import jakarta.servlet.http.*;import java.io.IOException;import java.nio.charset.StandardCharsets;import java.security.SecureRandom;import java.util.Base64;
@WebFilter("/*") public class FiltroCsrf implements Filter{
 private static final SecureRandom RNG=new SecureRandom();
 public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
  var q=(HttpServletRequest)request;var s=(HttpServletResponse)response;q.setCharacterEncoding("UTF-8");s.setCharacterEncoding("UTF-8");var session=q.getSession(true);String token=(String)session.getAttribute("csrf");
  if(token==null){byte[] b=new byte[32];RNG.nextBytes(b);token=Base64.getUrlEncoder().withoutPadding().encodeToString(b);session.setAttribute("csrf",token);}
  if("POST".equalsIgnoreCase(q.getMethod())){String sent=q.getParameter("csrf");if(sent==null||!java.security.MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),sent.getBytes(StandardCharsets.UTF_8))){s.sendError(403,"Formulario vencido. Recarga la página.");return;}}
  chain.doFilter(q,s);
 }
}
