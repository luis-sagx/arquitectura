package ec.edu.monster.servicio;

import com.sun.xml.ws.developer.JAXWSProperties;
import ec.edu.monster.modelo.RespuestaSoap;
import ec.edu.monster.clientesws.Respuesta;
import ec.edu.monster.clientesws.WSConUni;
import ec.edu.monster.clientesws.WSConUni_Service;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/** Adaptador SOAP; mantiene JAX-WS fuera de las vistas y controladores. */
public final class ClienteSoap {
    private final URI endpoint;
    private WSConUni puerto;

    public ClienteSoap(URI endpoint) {
        if (endpoint == null || !List.of("http", "https").contains(endpoint.getScheme())
                || endpoint.getHost() == null || endpoint.getFragment() != null) {
            throw new IllegalArgumentException("Indique la URL HTTP/HTTPS del servicio SOAP");
        }
        this.endpoint = "wsdl".equalsIgnoreCase(endpoint.getQuery())
                ? URI.create(endpoint.toString().substring(0, endpoint.toString().indexOf('?')))
                : endpoint;
    }

    public RespuestaSoap autenticar(String usuario, String clave) throws IOException {
        return invocar(p -> p.autenticar(usuario, clave));
    }

    public RespuestaSoap recuperarClave(String usuario, String nuevaClave) throws IOException {
        return invocar(p -> p.recuperarClave(usuario, nuevaClave));
    }

    public RespuestaSoap obtenerCategorias(String token) throws IOException {
        return invocar(p -> p.obtenerCategorias(token));
    }

    public RespuestaSoap obtenerUnidades(String token, String categoria) throws IOException {
        return invocar(p -> p.obtenerUnidades(token, categoria));
    }

    public RespuestaSoap convertir(String token, String categoria, String origen,
            String destino, double valor) throws IOException {
        return invocar(p -> p.convertir(token, categoria, origen, destino, valor));
    }

    private synchronized RespuestaSoap invocar(Operacion operacion) throws IOException {
        try {
            Respuesta respuesta = operacion.ejecutar(puerto());
            if (respuesta == null || !List.of("EXITO", "ERROR").contains(texto(respuesta.getEstado()).toUpperCase())
                    || texto(respuesta.getCodigo()).isBlank() || texto(respuesta.getMensaje()).isBlank()) {
                throw new IOException("El servidor devolvió una respuesta incompleta.");
            }
            if (respuesta.getResultado() != null && !Double.isFinite(respuesta.getResultado())) {
                throw new IOException("El servidor devolvió un resultado no finito.");
            }
            if (respuesta.getCategorias().stream().anyMatch(valor -> valor == null || valor.isBlank())
                    || respuesta.getUnidades().stream().anyMatch(valor -> valor == null || valor.isBlank())) {
                throw new IOException("El servidor devolvió una lista incompleta.");
            }
            return new RespuestaSoap(respuesta.getEstado(), respuesta.getCodigo(), respuesta.getMensaje(),
                    respuesta.getToken(), respuesta.getExpiraEn(), respuesta.getResultado(),
                    respuesta.getUnidadOrigen(), respuesta.getUnidadDestino(),
                    respuesta.getCategorias(), respuesta.getUnidades());
        } catch (SOAPFaultException ex) {
            String detalle = ex.getFault() == null ? "Fallo SOAP" : ex.getFault().getFaultString();
            throw new IOException("El servidor devolvió un fallo SOAP: " + detalle, ex);
        } catch (WebServiceException ex) {
            throw new IOException("No se pudo comunicar con el servidor. Revise la URL, la red y el despliegue.", ex);
        }
    }

    private WSConUni puerto() throws IOException {
        if (puerto == null) {
            var wsdl = ClienteSoap.class.getResource(
                    "/META-INF/wsdl/localhost_8080/WS_CON_UNI_JAVA_SOAP/WSConUni.wsdl");
            if (wsdl == null) {
                throw new IOException("No se encontró el WSDL local. Ejecute Clean and Build en NetBeans.");
            }
            var servicio = new WSConUni_Service(wsdl);
            puerto = servicio.getWSConUniPort();
            var contexto = ((BindingProvider) puerto).getRequestContext();
            contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint.toString());
            contexto.put(JAXWSProperties.CONNECT_TIMEOUT, 10_000);
            contexto.put(JAXWSProperties.REQUEST_TIMEOUT, 30_000);
        }
        return puerto;
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    @FunctionalInterface
    private interface Operacion {
        Respuesta ejecutar(WSConUni puerto);
    }
}
