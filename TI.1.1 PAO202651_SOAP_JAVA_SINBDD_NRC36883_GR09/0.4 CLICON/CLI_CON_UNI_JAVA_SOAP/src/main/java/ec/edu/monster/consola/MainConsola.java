package ec.edu.monster.consola;
import ec.edu.monster.cliente.ClienteSoap;
import java.io.*;
import java.net.URI;
public final class MainConsola {
    private MainConsola() {}
    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getProperty("conversor.endpoint",
            "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni");
        try {
            var cliente = new ClienteSoap(URI.create(url));
            var salida = new PrintWriter(System.out, true);
            salida.println("Servicio: " + url);
            new ConsolaUnidades(cliente, new BufferedReader(new InputStreamReader(System.in)), salida).ejecutar();
        } catch (IllegalArgumentException e) {
            System.err.println("URL invalida: " + e.getMessage());
            System.err.println("Uso: java -jar CLI_CON_UNI_JAVA_SOAP.jar URL_DEL_SERVICIO");
            System.exit(1);
        }
    }
}
