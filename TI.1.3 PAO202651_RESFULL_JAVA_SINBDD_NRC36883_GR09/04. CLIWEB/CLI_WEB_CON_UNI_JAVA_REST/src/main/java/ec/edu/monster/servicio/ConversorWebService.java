package ec.edu.monster.servicio;

import ec.edu.monster.modelo.RespuestaRest;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.net.URI;

/** Fachada de servicio que mantiene los controladores desacoplados del transporte. */
public final class ConversorWebService {
    private final ServletContext contexto;

    public ConversorWebService(ServletContext contexto) {
        this.contexto = contexto;
    }

    private ClienteRest cliente() {
        String url = contexto.getInitParameter("rest.endpoint");
        if (url == null || url.isBlank()) {
            url = System.getProperty("conversor.rest.endpoint");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Configura rest.endpoint para conectar con el servidor REST.");
        }
        try {
            return new ClienteRest(URI.create(url.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El endpoint REST configurado no es válido.", e);
        }
    }

    public RespuestaRest autenticar(String usuario, String clave) throws IOException {
        return cliente().autenticar(usuario, clave);
    }

    public RespuestaRest recuperarClave(String usuario, String nuevaClave) throws IOException {
        return cliente().recuperarClave(usuario, nuevaClave);
    }

    public RespuestaRest obtenerCategorias(String token) throws IOException {
        return cliente().obtenerCategorias(token);
    }

    public RespuestaRest obtenerUnidades(String token, String categoria) throws IOException {
        return cliente().obtenerUnidades(token, categoria);
    }

    public RespuestaRest convertir(String token, String categoria, String origen,
            String destino, double valor) throws IOException {
        return cliente().convertir(token, categoria, origen, destino, valor);
    }
}
