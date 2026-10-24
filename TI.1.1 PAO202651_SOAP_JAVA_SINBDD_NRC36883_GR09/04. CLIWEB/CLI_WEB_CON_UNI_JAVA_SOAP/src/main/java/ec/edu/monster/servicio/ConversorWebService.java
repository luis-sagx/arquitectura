package ec.edu.monster.servicio;
import ec.edu.monster.modelo.RespuestaSoap;
import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.net.URI;

public final class ConversorWebService {

    private final ServletContext contexto;

    public ConversorWebService(ServletContext contexto) {
        this.contexto = contexto;
    }

    private ClienteSoap cliente() {
        String url = System.getProperty("conversor.soap.endpoint");
        if (url == null || url.isBlank()) {
            url = contexto.getInitParameter("soap.endpoint");
        }
        return new ClienteSoap(URI.create(url));
    }

    public RespuestaSoap autenticar(String usuario, String clave) throws IOException {
        return cliente().autenticar(usuario, clave);
    }

    public RespuestaSoap recuperarClave(String usuario, String clave) throws IOException {
        return cliente().recuperarClave(usuario, clave);
    }

    public RespuestaSoap obtenerCategorias(String token) throws IOException {
        return cliente().obtenerCategorias(token);
    }

    public RespuestaSoap obtenerUnidades(String token, String categoria) throws IOException {
        return cliente().obtenerUnidades(token, categoria);
    }

    public RespuestaSoap convertir(
            String token, String categoria, String origen, String destino, double valor)
            throws IOException {
        return cliente().convertir(token, categoria, origen, destino, valor);
    }
}
