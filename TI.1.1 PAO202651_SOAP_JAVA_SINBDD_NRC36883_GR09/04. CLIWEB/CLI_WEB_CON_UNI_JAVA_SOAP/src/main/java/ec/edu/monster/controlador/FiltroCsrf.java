package ec.edu.monster.controlador;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter("/*")
public class FiltroCsrf implements Filter {

    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;
        httpRequest.setCharacterEncoding("UTF-8");
        httpResponse.setCharacterEncoding("UTF-8");

        HttpSession session = httpRequest.getSession(true);
        String token = (String) session.getAttribute("csrf");
        if (token == null) {
            byte[] bytes = new byte[32];
            RNG.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrf", token);
        }

        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String sentToken = httpRequest.getParameter("csrf");
            boolean tokenValido = sentToken != null
                    && MessageDigest.isEqual(
                            token.getBytes(StandardCharsets.UTF_8),
                            sentToken.getBytes(StandardCharsets.UTF_8));
            if (!tokenValido) {
                httpResponse.sendError(403, "Formulario vencido. Recarga la página.");
                return;
            }
        }

        chain.doFilter(httpRequest, httpResponse);
    }
}
