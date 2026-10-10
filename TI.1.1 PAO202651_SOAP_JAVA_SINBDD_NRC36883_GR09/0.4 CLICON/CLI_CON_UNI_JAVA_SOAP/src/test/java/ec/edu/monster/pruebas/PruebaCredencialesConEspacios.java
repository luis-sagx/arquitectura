package ec.edu.monster.pruebas;
import ec.edu.monster.cliente.ClienteSoap;
import ec.edu.monster.consola.ConsolaUnidades;
import java.io.*;
public class PruebaCredencialesConEspacios {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml ->
            xml.contains("<usuario> monster </usuario>") && xml.contains("<clave> clave </clave>")
            ? SoportePruebas.exito("<token>sesion</token>")
            : SoportePruebas.xml("<estado>ERROR</estado><codigo>AUTH-001</codigo><mensaje>Credenciales invalidas</mensaje>"))) {
            var salida = new StringWriter();
            new ConsolaUnidades(new ClienteSoap(servidor.uri()), new BufferedReader(new StringReader("1\n monster \n clave \n0\n")),
                new PrintWriter(salida, true)).ejecutar();
            SoportePruebas.exigir(salida.toString().contains("[OK]") && !salida.toString().contains("AUTH-001"),
                "Debe conservar espacios de usuario y clave configurados");
        }
        System.out.println("PruebaCredencialesConEspacios: OK");
    }
}
