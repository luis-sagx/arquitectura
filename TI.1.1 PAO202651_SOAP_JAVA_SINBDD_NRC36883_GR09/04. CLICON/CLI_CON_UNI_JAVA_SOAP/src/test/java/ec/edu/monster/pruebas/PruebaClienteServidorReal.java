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
            SoportePruebas.exigir(cliente.obtenerCategorias(token).categorias().equals(java.util.List.of("LONGITUD", "MASA", "TEMPERATURA")), "Catalogo real incorrecto");
            SoportePruebas.exigir(cliente.obtenerUnidades(token, "TEMPERATURA").unidades().equals(java.util.List.of("CELSIUS", "FAHRENHEIT", "KELVIN")), "Unidades reales de temperatura incorrectas");
            SoportePruebas.exigir(cliente.convertir(token, "LONGITUD", "METRO", "CENTIMETRO", 2).resultado() == 200, "Conversion real de longitud");
            SoportePruebas.exigir(cliente.convertir(token, "MASA", "KILOGRAMO", "GRAMO", 2).resultado() == 2000, "Conversion real de masa");
            SoportePruebas.exigir(cliente.convertir(token, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", 100).resultado() == 212, "Conversion real de temperatura");
            SoportePruebas.exigir("AUTH-001".equals(cliente.autenticar("monster", "mala").codigo()), "Credenciales invalidas");
            SoportePruebas.exigir("AUTH-002".equals(cliente.obtenerCategorias("inventado").codigo()), "Token invalido");
            SoportePruebas.exigir("DATA-001".equals(cliente.convertir(token, "INEXISTENTE", "LITRO", "MILILITRO", 1).codigo()), "Categoria inexistente debe rechazarse");
            SoportePruebas.exigir(cliente.recuperarClave("monster", "nueva123").exito(), "Recuperacion real debe cambiar la clave");
            SoportePruebas.exigir("AUTH-001".equals(cliente.autenticar("monster", "monster123").codigo()), "Clave anterior debe rechazarse tras recuperar");
            SoportePruebas.exigir(cliente.autenticar("monster", "nueva123").exito(), "Clave nueva debe autenticar tras recuperar");            System.out.println("PruebaClienteServidorReal: OK");
        } finally {
            tipo.getMethod("stop").invoke(endpoint); servidor.stop(0);
        }
    }
}
