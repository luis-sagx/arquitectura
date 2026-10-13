package ec.edu.monster.pruebas;
import ec.edu.monster.cliente.ws.ClienteSoap;
import ec.edu.monster.consola.controlador.ControladorConsola;
import ec.edu.monster.consola.vista.VistaConsola;
import java.io.*;
public class PruebaCredencialesConEspacios {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml ->
            xml.contains("<usuario> monster </usuario>") && xml.contains("<clave> clave </clave>")
            ? SoportePruebas.exito("<token>sesion</token>")
            : SoportePruebas.xml("<estado>ERROR</estado><codigo>AUTH-001</codigo><mensaje>Credenciales invalidas</mensaje>"))) {
            var salida = new StringWriter();
            var vista = new VistaConsola(new BufferedReader(new StringReader("1\n monster \n clave \n0\n")), new PrintWriter(salida, true));
            new ControladorConsola(new ClienteSoap(servidor.uri()), vista).ejecutar();
            SoportePruebas.exigir(salida.toString().contains("Sesión iniciada") && !salida.toString().contains("AUTH-001"),
                "Debe conservar espacios de usuario y clave configurados");
        }
        System.out.println("PruebaCredencialesConEspacios: OK");
    }
}
