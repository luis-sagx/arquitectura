package ec.edu.monster.cliente;

import ec.edu.monster.ws.Respuesta;
import ec.edu.monster.ws.WSConUni;
import ec.edu.monster.ws.WSConUni_Service;
import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/** Adaptador de la consola al Web Service Client generado por NetBeans. */
public final class ClienteSoap {
    private final URI endpoint;
    private WSConUni puerto;

    public ClienteSoap(URI endpoint) {
        if (endpoint == null || !List.of("http", "https").contains(endpoint.getScheme())
                || endpoint.getHost() == null || endpoint.getFragment() != null)
            throw new IllegalArgumentException("Indique una URL HTTP/HTTPS del servicio SOAP");
        this.endpoint = "wsdl".equalsIgnoreCase(endpoint.getQuery())
            ? URI.create(endpoint.toString().substring(0, endpoint.toString().indexOf('?'))) : endpoint;
    }

    public RespuestaSoap autenticar(String usuario, String clave) throws IOException {
        return invocar(p -> p.autenticar(usuario, clave));
    }
    public RespuestaSoap obtenerCategorias(String token) throws IOException {
        return invocar(p -> p.obtenerCategorias(token));
    }
    public RespuestaSoap obtenerUnidades(String token, String categoria) throws IOException {
        return invocar(p -> p.obtenerUnidades(token, categoria));
    }
    public RespuestaSoap convertir(String token, String categoria, String origen, String destino, double valor) throws IOException {
        return invocar(p -> p.convertir(token, categoria, origen, destino, valor));
    }

    private WSConUni puerto() throws IOException {
        if (puerto == null) {
            var wsdl = ClienteSoap.class.getResource(
                "/META-INF/wsdl/localhost_8080/WS_CON_UNI_JAVA_SOAP/WSConUni.wsdl");
            if (wsdl == null) throw new IOException("No se encontro el WSDL del cliente; compile el proyecto con Maven");
            var servicio = new WSConUni_Service(wsdl);
            puerto = servicio.getWSConUniPort();
            var contexto = ((BindingProvider) puerto).getRequestContext();
            contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint.toString());
            contexto.put(JAXWSProperties.CONNECT_TIMEOUT, 10_000);
            contexto.put(JAXWSProperties.REQUEST_TIMEOUT, 30_000);
        }
        return puerto;
    }

    private RespuestaSoap invocar(Operacion operacion) throws IOException {
        try {
            Respuesta r = operacion.ejecutar(puerto());
            if (r == null || !List.of("EXITO", "ERROR").contains(texto(r.getEstado()))
                    || texto(r.getCodigo()).isBlank() || texto(r.getMensaje()).isBlank())
                throw new IOException("El servidor devolvio una respuesta incompleta");
            if (r.getResultado() != null && !Double.isFinite(r.getResultado()))
                throw new IOException("El servidor devolvio un resultado no finito");
            if (r.getCategorias().stream().anyMatch(java.util.Objects::isNull)
                    || r.getUnidades().stream().anyMatch(java.util.Objects::isNull))
                throw new IOException("El servidor devolvio una lista incompleta");
            return new RespuestaSoap(r.getEstado(), r.getCodigo(), r.getMensaje(),
                texto(r.getToken()), texto(r.getExpiraEn()), r.getResultado(),
                texto(r.getUnidadOrigen()), texto(r.getUnidadDestino()), r.getCategorias(), r.getUnidades());
        } catch (SOAPFaultException e) {
            throw new IOException("El servidor devolvio un fallo SOAP: " + e.getFault().getFaultString(), e);
        } catch (WebServiceException e) {
            throw new IOException("No se pudo comunicar con el servidor o interpretar su respuesta. Revise la URL, la red y el despliegue.", e);
        }
    }
    private static String texto(String texto) { return texto == null ? "" : texto; }
    @FunctionalInterface private interface Operacion { Respuesta ejecutar(WSConUni puerto); }
}
