package ec.edu.monster.servicio;
import com.sun.xml.ws.developer.JAXWSProperties;
import ec.edu.monster.clientesws.Respuesta;
import ec.edu.monster.clientesws.WSConUni;
import ec.edu.monster.clientesws.WSConUni_Service;
import ec.edu.monster.modelo.RespuestaSoap;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/** Adaptador JAX-WS, en la capa de servicio como en los otros clientes. */
public final class ClienteSoap {
    private final URI endpoint;

    public ClienteSoap(URI endpoint) {
        if (endpoint == null
                || endpoint.getHost() == null
                || !List.of("http", "https").contains(endpoint.getScheme())) {
            throw new IllegalArgumentException("La URL SOAP debe usar HTTP o HTTPS.");
        }
        this.endpoint = endpoint;
    }

    public RespuestaSoap autenticar(String usuario, String clave) throws IOException {
        return invocar(puerto -> puerto.autenticar(usuario, clave));
    }

    public RespuestaSoap recuperarClave(String usuario, String clave) throws IOException {
        return invocar(puerto -> puerto.recuperarClave(usuario, clave));
    }

    public RespuestaSoap obtenerCategorias(String token) throws IOException {
        return invocar(puerto -> puerto.obtenerCategorias(token));
    }

    public RespuestaSoap obtenerUnidades(String token, String categoria) throws IOException {
        return invocar(puerto -> puerto.obtenerUnidades(token, categoria));
    }

    public RespuestaSoap convertir(
            String token, String categoria, String origen, String destino, double valor)
            throws IOException {
        return invocar(puerto -> puerto.convertir(token, categoria, origen, destino, valor));
    }

    private RespuestaSoap invocar(Operacion operacion) throws IOException {
        try {
            var wsdl = ClienteSoap.class.getResource("/wsdl/WSConUni.wsdl");
            if (wsdl == null) {
                throw new IOException("No se encontró el WSDL local.");
            }

            WSConUni puerto = new WSConUni_Service(wsdl).getWSConUniPort();
            var contexto = ((BindingProvider) puerto).getRequestContext();
            contexto.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint.toString());
            contexto.put(JAXWSProperties.CONNECT_TIMEOUT, 10000);
            contexto.put(JAXWSProperties.REQUEST_TIMEOUT, 30000);

            Respuesta respuesta = operacion.run(puerto);
            if (respuesta == null
                    || respuesta.getCodigo() == null
                    || respuesta.getMensaje() == null) {
                throw new IOException("Respuesta SOAP incompleta.");
            }
            if (respuesta.getResultado() != null
                    && !Double.isFinite(respuesta.getResultado())) {
                throw new IOException("Resultado numérico inválido.");
            }

            return new RespuestaSoap(
                    respuesta.getEstado(),
                    respuesta.getCodigo(),
                    respuesta.getMensaje(),
                    respuesta.getToken(),
                    respuesta.getExpiraEn(),
                    respuesta.getResultado(),
                    respuesta.getUnidadOrigen(),
                    respuesta.getUnidadDestino(),
                    respuesta.getCategorias(),
                    respuesta.getUnidades());
        } catch (SOAPFaultException exception) {
            throw new IOException("El servidor SOAP devolvió un error.", exception);
        } catch (WebServiceException exception) {
            throw new IOException(
                    "No se pudo conectar con el servidor SOAP. Comprueba su URL y despliegue.",
                    exception);
        }
    }

    @FunctionalInterface
    private interface Operacion {
        Respuesta run(WSConUni puerto);
    }
}
