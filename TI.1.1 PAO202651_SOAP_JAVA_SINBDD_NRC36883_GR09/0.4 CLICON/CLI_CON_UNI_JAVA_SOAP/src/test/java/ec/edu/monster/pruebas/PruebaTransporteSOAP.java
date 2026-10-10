package ec.edu.monster.pruebas;
import ec.edu.monster.cliente.ClienteSoap;
import java.io.IOException;
public class PruebaTransporteSOAP {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> SoportePruebas.exito("<token>sesion</token><expiraEn>2026-10-09T00:15:00Z</expiraEn>"))) {
            var cliente = new ClienteSoap(servidor.uri());
            var login = cliente.autenticar("usuario<&", "clave<&");
            SoportePruebas.exigir(login.exito() && "sesion".equals(login.token()), "Debe interpretar autenticacion");
            var fabrica = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            var documento = fabrica.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(servidor.ultimaSolicitud)));
            SoportePruebas.exigir("clave<&".equals(documento.getElementsByTagName("clave").item(0).getTextContent()), "Credenciales XML deben conservar caracteres especiales");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("autenticar"), "Operacion SOAP incorrecta");
            servidor.respuesta = xml -> SoportePruebas.exito("<categorias>LONGITUD</categorias><categorias>MASA</categorias>");
            SoportePruebas.exigir(cliente.obtenerCategorias("sesion").categorias().equals(java.util.List.of("LONGITUD", "MASA")), "Debe leer listas SOAP");
            servidor.respuesta = xml -> SoportePruebas.exito("<unidades>METRO</unidades><unidades>CENTIMETRO</unidades>");
            SoportePruebas.exigir(cliente.obtenerUnidades("sesion", "LONGITUD").unidades().size() == 2, "Debe consultar unidades");
            servidor.respuesta = xml -> SoportePruebas.exito("<resultado>200.0</resultado><unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino>");
            SoportePruebas.exigir(cliente.convertir("sesion", "LONGITUD", "METRO", "CENTIMETRO", 2).resultado() == 200, "Debe leer resultado");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<valor>2.0</valor>") && servidor.ultimaSolicitud.contains("<token>sesion</token>"), "Debe enviar valor y token");
            servidor.respuesta = xml -> SoportePruebas.xml("<estado>ERROR</estado><codigo>AUTH-003</codigo><mensaje>Sesion expirada</mensaje>");
            var error = cliente.obtenerCategorias("sesion");
            SoportePruebas.exigir(!error.exito() && error.sesionInvalida() && error.resultado() == null, "Debe reconocer expiracion");
            servidor.respuesta = xml -> "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body>"
                + "<m:autenticarResponse xmlns:m=\"http://ws.controladores.monster.edu.ec/\"><return>"
                + "<estado>EXITO</estado><codigo>OK</codigo><mensaje>Respuesta de otra operacion</mensaje>"
                + "</return></m:autenticarResponse></s:Body></s:Envelope>";
            esperarError(() -> cliente.obtenerCategorias("sesion"));
            servidor.respuesta = xml -> SoportePruebas.exito(
                "<categorias xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>");
            esperarError(() -> cliente.obtenerCategorias("sesion"));
            servidor.respuesta = xml -> SoportePruebas.exito(
                "<unidades xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>");
            esperarError(() -> cliente.obtenerUnidades("sesion", "LONGITUD"));
            servidor.respuesta = xml -> "<html>Respuesta ajena al servicio</html>";
            esperarError(() -> cliente.obtenerCategorias("sesion"));
            servidor.estadoHttp = 500;
            servidor.respuesta = xml -> "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body><s:Fault><faultcode>s:Server</faultcode><faultstring>Fallo controlado</faultstring></s:Fault></s:Body></s:Envelope>";
            esperarError(() -> cliente.obtenerCategorias("sesion"));
            servidor.estadoHttp = 200;
            servidor.respuesta = xml -> "<!DOCTYPE x [<!ENTITY contenido SYSTEM \"file:///no-existe\">]><x>&contenido;</x>";
            esperarError(() -> cliente.obtenerCategorias("sesion"));
        }
        try (var apagado = javax.net.ServerSocketFactory.getDefault().createServerSocket(0)) {
            var uri = java.net.URI.create("http://127.0.0.1:" + apagado.getLocalPort() + "/WSConUni");
            apagado.close();
            esperarError(() -> new ClienteSoap(uri).obtenerCategorias("sesion"));
        }
        System.out.println("PruebaTransporteSOAP: OK");
    }
    private static void esperarError(Accion accion) throws Exception {
        try { accion.ejecutar(); throw new AssertionError("Debe informar fallo de comunicacion/XML"); }
        catch (IOException esperado) { SoportePruebas.exigir(!esperado.getMessage().isBlank(), "Fallo debe incluir mensaje"); }
    }
    @FunctionalInterface private interface Accion { void ejecutar() throws Exception; }
}
