package ec.edu.monster.servicio;

import ec.edu.monster.modelo.RespuestaSoap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Adaptador SOAP 1.1 del servicio WSConUni.
 * Android no incluye JAX-WS, por eso el sobre se arma y se lee con HttpURLConnection y DOM;
 * así las vistas y controladores no conocen el XML.
 */
public final class ClienteSoap {
    private static final String NAMESPACE = "http://ws.controladores.monster.edu.ec/";
    private static final String SOAP_ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    private static final int TIEMPO_CONEXION_MS = 10_000;
    private static final int TIEMPO_RESPUESTA_MS = 30_000;

    private final URL endpoint;

    public ClienteSoap(String direccion) {
        try {
            this.endpoint = new URL(normalizarEndpoint(direccion));
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Indique la URL HTTP/HTTPS del servicio SOAP", ex);
        }
    }

    /** Valida la dirección y acepta también la URL terminada en ?wsdl. */
    public static String normalizarEndpoint(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("Escribe la URL del servicio SOAP.");
        }
        try {
            URI uri = new URI(direccion.trim());
            String esquema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!("http".equals(esquema) || "https".equals(esquema)) || uri.getHost() == null
                    || uri.getFragment() != null) {
                throw new IllegalArgumentException("La URL debe empezar con http:// o https:// e incluir el servidor.");
            }
            String texto = uri.toString();
            return "wsdl".equalsIgnoreCase(uri.getQuery()) ? texto.substring(0, texto.indexOf('?')) : texto;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("La URL del servicio no es válida.", ex);
        }
    }

    public String endpoint() {
        return endpoint.toString();
    }

    public RespuestaSoap autenticar(String usuario, String clave) throws IOException {
        return invocar("autenticar", "usuario", usuario, "clave", clave);
    }

    public RespuestaSoap recuperarClave(String usuario, String nuevaClave) throws IOException {
        return invocar("recuperarClave", "usuario", usuario, "nuevaClave", nuevaClave);
    }

    public RespuestaSoap obtenerCategorias(String token) throws IOException {
        return invocar("obtenerCategorias", "token", token);
    }

    public RespuestaSoap obtenerUnidades(String token, String categoria) throws IOException {
        return invocar("obtenerUnidades", "token", token, "categoria", categoria);
    }

    public RespuestaSoap convertir(String token, String categoria, String origen,
            String destino, double valor) throws IOException {
        return invocar("convertir", "token", token, "categoria", categoria,
                "unidadOrigen", origen, "unidadDestino", destino, "valor", Double.toString(valor));
    }

    private RespuestaSoap invocar(String operacion, String... parametros) throws IOException {
        byte[] cuerpo = sobre(operacion, parametros).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) endpoint.openConnection();
            conexion.setConnectTimeout(TIEMPO_CONEXION_MS);
            conexion.setReadTimeout(TIEMPO_RESPUESTA_MS);
            conexion.setRequestMethod("POST");
            conexion.setDoOutput(true);
            conexion.setUseCaches(false);
            conexion.setFixedLengthStreamingMode(cuerpo.length);
            conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conexion.setRequestProperty("Accept", "text/xml");
            conexion.setRequestProperty("SOAPAction", "\"\"");
            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(cuerpo);
            }

            int estadoHttp = conexion.getResponseCode();
            InputStream entrada = estadoHttp >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            if (entrada == null) {
                throw new IOException(mensajeHttp(estadoHttp));
            }
            Document documento;
            try (InputStream contenido = entrada) {
                documento = parsear(contenido);
            } catch (SAXException ex) {
                throw new IOException(estadoHttp >= 400 ? mensajeHttp(estadoHttp)
                        : "El servidor no respondió con un mensaje SOAP. Revise la URL.", ex);
            }

            Element fallo = primero(documento.getElementsByTagNameNS(SOAP_ENV, "Fault"));
            if (fallo != null) {
                String detalle = textoHijo(fallo, "faultstring");
                throw new IOException("El servidor devolvió un fallo SOAP: "
                        + (detalle == null ? "sin detalle" : detalle));
            }
            if (estadoHttp >= 400) {
                throw new IOException(mensajeHttp(estadoHttp));
            }
            Element respuesta = primero(documento.getElementsByTagNameNS(NAMESPACE, operacion + "Response"));
            Element retorno = respuesta == null ? null : hijo(respuesta, "return");
            if (retorno == null) {
                throw new IOException("El servidor devolvió una respuesta que no corresponde a " + operacion + ".");
            }
            return leerRespuesta(retorno);
        } catch (SocketTimeoutException ex) {
            throw new IOException("El servidor tardó demasiado en responder.", ex);
        } catch (ConnectException | UnknownHostException ex) {
            throw new IOException("No se pudo conectar con el servidor. Revise la URL, la red y que Payara esté encendido.", ex);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static String sobre(String operacion, String... parametros) {
        StringBuilder xml = new StringBuilder(256)
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<S:Envelope xmlns:S=\"").append(SOAP_ENV).append("\"><S:Body>")
                .append("<ns:").append(operacion).append(" xmlns:ns=\"").append(NAMESPACE).append("\">");
        for (int i = 0; i < parametros.length; i += 2) {
            String valor = parametros[i + 1];
            if (valor != null) {
                xml.append('<').append(parametros[i]).append('>')
                        .append(escapar(valor))
                        .append("</").append(parametros[i]).append('>');
            }
        }
        return xml.append("</ns:").append(operacion).append("></S:Body></S:Envelope>").toString();
    }

    private static RespuestaSoap leerRespuesta(Element retorno) throws IOException {
        String estado = textoHijo(retorno, "estado");
        String codigo = textoHijo(retorno, "codigo");
        String mensaje = textoHijo(retorno, "mensaje");
        if (estado == null || !("EXITO".equalsIgnoreCase(estado) || "ERROR".equalsIgnoreCase(estado))
                || vacio(codigo) || vacio(mensaje)) {
            throw new IOException("El servidor devolvió una respuesta incompleta.");
        }

        Double resultado = null;
        String textoResultado = textoHijo(retorno, "resultado");
        if (textoResultado != null) {
            try {
                resultado = Double.parseDouble(textoResultado.trim());
            } catch (NumberFormatException ex) {
                throw new IOException("El servidor devolvió un resultado no numérico.", ex);
            }
            if (Double.isNaN(resultado) || Double.isInfinite(resultado)) {
                throw new IOException("El servidor devolvió un resultado no finito.");
            }
        }

        List<String> categorias = lista(retorno, "categorias");
        List<String> unidades = lista(retorno, "unidades");
        return new RespuestaSoap(estado, codigo, mensaje, textoHijo(retorno, "token"),
                textoHijo(retorno, "expiraEn"), resultado, textoHijo(retorno, "unidadOrigen"),
                textoHijo(retorno, "unidadDestino"), categorias, unidades);
    }

    private static List<String> lista(Element padre, String nombre) throws IOException {
        List<String> valores = new ArrayList<>();
        for (Node nodo = padre.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
            if (nodo instanceof Element && nombre.equals(nodo.getLocalName())) {
                String valor = nodo.getTextContent();
                if (vacio(valor)) {
                    throw new IOException("El servidor devolvió una lista incompleta.");
                }
                valores.add(valor);
            }
        }
        return valores;
    }

    private static Document parsear(InputStream entrada) throws IOException, SAXException {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setNamespaceAware(true);
        fabrica.setExpandEntityReferences(false);
        try {
            // Evita DTD y entidades externas en la respuesta (XXE).
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException ignorado) {
            // El parser de Android no admite esta característica y tampoco resuelve DTD externas.
        }
        try {
            DocumentBuilder constructor = fabrica.newDocumentBuilder();
            // Sin esto el parser imprime "[Fatal Error]" en consola; el error ya se informa al usuario.
            constructor.setErrorHandler(new DefaultHandler() {
                @Override
                public void fatalError(SAXParseException ex) throws SAXException {
                    throw ex;
                }
            });
            return constructor.parse(entrada);
        } catch (ParserConfigurationException ex) {
            throw new IOException("No se pudo preparar el lector XML.", ex);
        }
    }

    private static Element primero(NodeList nodos) {
        return nodos.getLength() == 0 ? null : (Element) nodos.item(0);
    }

    private static Element hijo(Element padre, String nombre) {
        for (Node nodo = padre.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
            if (nodo instanceof Element && nombre.equals(nodo.getLocalName())) {
                return (Element) nodo;
            }
        }
        return null;
    }

    private static String textoHijo(Element padre, String nombre) {
        Element elemento = hijo(padre, nombre);
        return elemento == null ? null : elemento.getTextContent();
    }

    private static String mensajeHttp(int estadoHttp) {
        return estadoHttp == 404
                ? "No se encontró el servicio en esa URL (HTTP 404). Revise que el WAR esté desplegado."
                : "El servidor respondió con el estado HTTP " + estadoHttp + ".";
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static String escapar(String valor) {
        StringBuilder texto = new StringBuilder(valor.length() + 16);
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '&': texto.append("&amp;"); break;
                case '<': texto.append("&lt;"); break;
                case '>': texto.append("&gt;"); break;
                case '"': texto.append("&quot;"); break;
                case '\'': texto.append("&apos;"); break;
                default: texto.append(c);
            }
        }
        return texto.toString();
    }
}
