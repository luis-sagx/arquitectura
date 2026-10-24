package ec.edu.monster.pruebas;

import com.sun.net.httpserver.HttpServer;
import ec.edu.monster.controladores.ws.WSConUni;
import jakarta.xml.ws.Endpoint;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/** Integracion de contrato WSDL y transporte SOAP real, sin servidor externo. */
public class PruebaIntegracionSOAP {
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();
    private static URI direccion;
    public static void main(String[] args) throws Exception {
        System.setProperty("conversor.usuario", "Monster");
        System.setProperty("conversor.clave", "Monster9");
        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        Endpoint endpoint = Endpoint.create(new WSConUni());
        try {
            endpoint.publish(servidor.createContext("/WSConUni"));
            servidor.start();
            direccion = URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/WSConUni");
            String wsdl = CLIENTE.send(HttpRequest.newBuilder(URI.create(direccion + "?wsdl")).GET().build(),
                HttpResponse.BodyHandlers.ofString()).body();
            exigir(wsdl.contains("autenticar") && wsdl.contains("recuperarClave") && wsdl.contains("obtenerCategorias")
                && wsdl.contains("obtenerUnidades") && wsdl.contains("convertir"), "WSDL incompleto");
            Document recuperacion = solicitar("recuperarClave", "<usuario>Monster</usuario><nuevaClave>nueva123</nuevaClave>");
            exigir("OK".equals(texto(recuperacion, "codigo")), "SOAP debe permitir recuperar la clave por usuario");
            Document claveAnterior = solicitar("autenticar", "<usuario>Monster</usuario><clave>Monster9</clave>");
            exigir("AUTH-001".equals(texto(claveAnterior, "codigo")), "La clave anterior debe dejar de funcionar");
            Document claveNueva = solicitar("autenticar", "<usuario>Monster</usuario><clave>nueva123</clave>");
            exigir("OK".equals(texto(claveNueva, "codigo")), "La clave nueva debe permitir iniciar sesion");
            String token = texto(claveNueva, "token");
            exigir(!token.isBlank(), "SOAP debe serializar token");
            Document conversion = solicitar("convertir", "<token>" + token + "</token><categoria>LONGITUD</categoria>"
                + "<unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino><valor>2</valor>");
            exigir("OK".equals(texto(conversion, "codigo")), "Token debe persistir entre llamadas SOAP");
            exigir(Double.parseDouble(texto(conversion, "resultado")) == 200, "Resultado SOAP incorrecto");
            Document catalogo = solicitar("obtenerCategorias", "<token>" + token + "</token>");
            exigir("OK".equals(texto(catalogo, "codigo"))
                && catalogo.getElementsByTagName("categorias").getLength() == 3, "Catalogo SOAP incorrecto");
            Document unidadesTemperatura = solicitar("obtenerUnidades", "<token>" + token + "</token><categoria>TEMPERATURA</categoria>");
            exigir("OK".equals(texto(unidadesTemperatura, "codigo"))
                && unidadesTemperatura.getElementsByTagName("unidades").getLength() == 3, "Unidades de temperatura SOAP incorrectas");
            Document unidades = solicitar("obtenerUnidades", "<token>" + token + "</token><categoria>MASA</categoria>");
            exigir("OK".equals(texto(unidades, "codigo"))
                && lista(unidades, "unidades").equals(java.util.List.of("KILOGRAMO", "GRAMO", "MILIGRAMO", "LIBRA")),
                "Unidades de masa SOAP incorrectas");
            Document unidadesLongitud = solicitar("obtenerUnidades", "<token>" + token + "</token><categoria>LONGITUD</categoria>");
            exigir("OK".equals(texto(unidadesLongitud, "codigo"))
                && lista(unidadesLongitud, "unidades").equals(java.util.List.of(
                    "KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE")),
                "Unidades de longitud SOAP incorrectas");
            Document error = solicitar("convertir", "<categoria>LONGITUD</categoria>"
                + "<unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino><valor>2</valor>");
            exigir("AUTH-002".equals(texto(error, "codigo")), "SOAP debe rechazar token ausente");
            exigir(texto(error, "resultado").isBlank(), "Error SOAP no debe devolver resultado");
            Document sinValor = solicitar("convertir", "<token>" + token + "</token><categoria>LONGITUD</categoria>"
                + "<unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino>");
            exigir("DATA-001".equals(texto(sinValor, "codigo")), "SOAP debe distinguir valor ausente de cero");
            System.out.println("PruebaIntegracionSOAP: OK (WSDL, autenticacion, catalogo, conversion y errores)");
        } finally {
            endpoint.stop();
            servidor.stop(0);
        }
    }
    private static Document solicitar(String operacion, String parametros) throws Exception {
        String xml = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>"
            + "<m:" + operacion + " xmlns:m=\"http://ws.controladores.monster.edu.ec/\">"
            + parametros + "</m:" + operacion + "></s:Body></s:Envelope>";
        var respuesta = CLIENTE.send(HttpRequest.newBuilder(direccion).header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", "\"\"").POST(HttpRequest.BodyPublishers.ofString(xml)).build(),
            HttpResponse.BodyHandlers.ofString());
        exigir(respuesta.statusCode() == 200, "Solicitud SOAP fallo: " + respuesta.body());
        var fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return fabrica.newDocumentBuilder().parse(new InputSource(new StringReader(respuesta.body())));
    }
    private static String texto(Document doc, String nombre) {
        var nodos = doc.getElementsByTagName(nombre);
        return nodos.getLength() == 0 ? "" : nodos.item(0).getTextContent();
    }
    private static java.util.List<String> lista(Document doc, String nombre) {
        var nodos = doc.getElementsByTagName(nombre);
        var valores = new java.util.ArrayList<String>();
        for (int i = 0; i < nodos.getLength(); i++) valores.add(nodos.item(i).getTextContent());
        return valores;
    }
    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}
