package ec.edu.monster.consola;

import ec.edu.monster.cliente.ClienteSoap;
import ec.edu.monster.consola.controlador.ControladorConsola;
import ec.edu.monster.consola.vista.VistaConsola;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;

/** Punto de entrada de la aplicación de consola. */
public final class MainConsola {
    private MainConsola() { }

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : System.getProperty("conversor.endpoint",
            "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/WSConUni");
        try {
            var cliente = new ClienteSoap(URI.create(url));
            var salida = new PrintWriter(System.out, true);
            salida.println("Servicio: " + url);
            var vista = new VistaConsola(new BufferedReader(new InputStreamReader(System.in)), salida);
            new ControladorConsola(cliente, vista).ejecutar();
        } catch (IllegalArgumentException e) {
            System.err.println("URL invalida: " + e.getMessage());
            System.err.println("Uso: java -jar CLI_CON_UNI_JAVA_SOAP.jar URL_DEL_SERVICIO");
            System.exit(1);
        }
    }
}
