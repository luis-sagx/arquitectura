package ec.edu.monster.pruebas;
import ec.edu.monster.cliente.ClienteSoap;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
public class PruebaClienteServidorReal {
    public static void main(String[] args) throws Exception {
        System.setProperty("conversor.usuario", "monster");
        System.setProperty("conversor.clave", "monster123");
        var servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        Class<?> tipo = Class.forName("jakarta.xml.ws.Endpoint");
        Object ws = Class.forName("ec.edu.monster.controladores.ws.WSConUni").getConstructor().newInstance();
        Object endpoint = tipo.getMethod("create", Object.class).invoke(null, ws);
        try {
            tipo.getMethod("publish", Object.class).invoke(endpoint, servidor.createContext("/WSConUni"));
            servidor.start();
            var cliente = new ClienteSoap(URI.create("http://127.0.0.1:" + servidor.getAddress().getPort() + "/WSConUni"));
            var login = cliente.autenticar("monster", "monster123");
            SoportePruebas.exigir(login.exito() && login.token() != null, "Autenticacion real debe funcionar");
            String token = login.token();
            SoportePruebas.exigir(cliente.obtenerCategorias(token).categorias().size() == 6, "Catalogo real incompleto");
            SoportePruebas.exigir(cliente.obtenerUnidades(token, "TEMPERATURA").unidades().contains("KELVIN"), "Unidades reales incorrectas");
            SoportePruebas.exigir(cliente.convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 2).resultado() == 200, "Conversion real de longitud");
            SoportePruebas.exigir(cliente.convertir(token, "MASA", "KILOGRAMO", "GRAMO", 2).resultado() == 2000, "Conversion real de masa");
            SoportePruebas.exigir(cliente.convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 100).resultado() == 212, "Conversion real de temperatura");
            SoportePruebas.exigir("AUTH-001".equals(cliente.autenticar("monster", "mala").codigo()), "Credenciales invalidas");
            SoportePruebas.exigir("AUTH-002".equals(cliente.obtenerCategorias("inventado").codigo()), "Token invalido");
            SoportePruebas.exigir("UNIT-001".equals(cliente.convertir(token, "VOLUMEN", "LITRO", "MILILITRO", 1).codigo()), "Categoria solo consultable");
            System.out.println("PruebaClienteServidorReal: OK");
        } finally {
            tipo.getMethod("stop").invoke(endpoint); servidor.stop(0);
        }
    }
}
