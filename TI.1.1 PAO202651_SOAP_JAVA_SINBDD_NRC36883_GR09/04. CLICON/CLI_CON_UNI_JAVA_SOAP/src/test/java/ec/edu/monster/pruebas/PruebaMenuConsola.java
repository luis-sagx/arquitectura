package ec.edu.monster.pruebas;
import ec.edu.monster.cliente.ws.ClienteSoap;
import ec.edu.monster.consola.controlador.ControladorConsola;
import ec.edu.monster.consola.vista.VistaConsola;
import java.io.*;
public class PruebaMenuConsola {
    public static void main(String[] args) throws Exception {
        try (var servidor = new SoportePruebas(xml -> {
            if (xml.contains(":autenticar")) return SoportePruebas.exito("<token>sesion</token>");
            if (xml.contains(":recuperarClave")) return SoportePruebas.exito("");
            if (xml.contains(":obtenerUnidades")) return SoportePruebas.exito("<unidades>METRO</unidades><unidades>CENTIMETRO</unidades>");
            if (xml.contains(":convertir")) return SoportePruebas.exito("<resultado>250</resultado><unidadOrigen>METRO</unidadOrigen><unidadDestino>CENTIMETRO</unidadDestino>");
            return SoportePruebas.exito("<categorias>LONGITUD</categorias>");
        })) {
            String entrada = "9\n3\n1\nmonster\nmonster123\n3\n1\n1\n2\nNaN\nInfinity\nabc\n2,5\n0\n";
            var salida = new StringWriter();
            new ControladorConsola(new ClienteSoap(servidor.uri()), new VistaConsola(new BufferedReader(new StringReader(entrada)), new PrintWriter(salida, true))).ejecutar();
            String texto = salida.toString();
            SoportePruebas.exigir(texto.contains("MENÚ PRINCIPAL"), "El menú debe mostrar una sección principal clara");
            String separador = "------------------------------------------------------------";
            SoportePruebas.exigir(texto.contains(separador), "El menu debe separar visualmente sus secciones");
            SoportePruebas.exigir(texto.matches("(?s).*Conversión completada:\\R" + separador + "\\R\\s+2\\.5000 METRO = 250\\.0000 CENTIMETRO\\R" + separador + ".*"), "La conversión debe aparecer separada visualmente");
            String[] pasos = { "Paso 1/4: Categoría", "Paso 2/4: Unidad de origen",
                "Paso 3/4: Unidad de destino", "Paso 4/4: Cantidad", "RESULTADO DE CONVERSIÓN | LONGITUD" };
            int anterior = -1;
            for (String paso : pasos) {
                int posicion = texto.indexOf(paso);
                SoportePruebas.exigir(posicion > anterior, "Los pasos deben conservar el orden categoría, origen, destino y cantidad: " + paso);
                anterior = posicion;
            }
            SoportePruebas.exigir(texto.matches("(?s).*Paso 4/4: Cantidad.*Categoría: LONGITUD\\ROrigen:    METRO\\RDestino:   CENTIMETRO.*"),
                "Antes de ingresar la cantidad deben verse la categoría y ambas unidades");
            SoportePruebas.exigir(!texto.contains("Presiona Enter") && !texto.contains("\033["),
                "Las entradas automatizadas no deben consumir pausas ni contener secuencias de limpieza");
            var salidaPausa = new StringWriter();
            var vistaPausa = new VistaConsola(new BufferedReader(new StringReader("\ncontinuar\n")),
                new PrintWriter(salidaPausa, true), true);
            vistaPausa.esperarParaContinuar();
            SoportePruebas.exigir(salidaPausa.toString().contains("Presiona Enter")
                && vistaPausa.leer("").equals("continuar"), "La pausa interactiva debe consumir solo Enter");
            SoportePruebas.exigir(texto.contains("Opcion invalida"), "Menu debe validar opciones");
            SoportePruebas.exigir(texto.contains("Inicie sesion"), "No debe convertir sin iniciar sesion");
            SoportePruebas.exigir(texto.contains("numero finito"), "Debe validar dato numerico");
            SoportePruebas.exigir(texto.contains("250.0000 CENTIMETRO"), "Debe mostrar resultado del servidor");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<valor>2.5</valor>"), "Debe aceptar coma decimal");
            var recuperacion = new StringWriter();
            new ControladorConsola(new ClienteSoap(servidor.uri()), new VistaConsola(
                new BufferedReader(new StringReader("5\nmonster\nnueva123\n0\n")), new PrintWriter(recuperacion, true))).ejecutar();
            SoportePruebas.exigir(recuperacion.toString().contains("Operacion correcta"), "Menu debe permitir recuperar clave sin iniciar sesion");
            SoportePruebas.exigir(servidor.ultimaSolicitud.contains("<usuario>monster</usuario>")
                && servidor.ultimaSolicitud.contains("<nuevaClave>nueva123</nuevaClave>"), "Menu debe enviar usuario y nueva clave");
            var recuperacionConSesion = new StringWriter();
            new ControladorConsola(new ClienteSoap(servidor.uri()), new VistaConsola(
                new BufferedReader(new StringReader("1\nmonster\nmonster123\n5\nmonster\nnueva123\n2\n0\n")),
                new PrintWriter(recuperacionConSesion, true))).ejecutar();
            SoportePruebas.exigir(recuperacionConSesion.toString().contains("Inicie sesion con la opcion 1"),
                "La recuperacion debe cerrar la sesion local actual");            var fin = new StringWriter();
            new ControladorConsola(new ClienteSoap(servidor.uri()), new VistaConsola(new BufferedReader(new StringReader("")), new PrintWriter(fin, true))).ejecutar();
            SoportePruebas.exigir(fin.toString().contains("Hasta luego"), "EOF debe cerrar limpiamente");
            servidor.respuesta = xml -> xml.contains(":autenticar") ? SoportePruebas.exito("<token>sesion</token>")
                : SoportePruebas.xml("<estado>ERROR</estado><codigo>AUTH-003</codigo><mensaje>Sesion expirada</mensaje>");
            var expirada = new StringWriter();
            new ControladorConsola(new ClienteSoap(servidor.uri()), new VistaConsola(new BufferedReader(new StringReader("1\nmonster\nmonster123\n2\n3\n0\n")), new PrintWriter(expirada, true))).ejecutar();
            SoportePruebas.exigir(expirada.toString().contains("Sesion expirada") && expirada.toString().contains("Inicie sesion"), "Expiracion debe borrar sesion local");
        }
        System.out.println("PruebaMenuConsola: OK");
    }
}
