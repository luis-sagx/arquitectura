package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;
import java.util.List;

public class PruebaConsultaCatalogo {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);

        Respuesta categorias = servicio.obtenerCategorias(token);
        VerificacionesREST.codigo(categorias, "OK", "consultar categorías");
        VerificacionesREST.comprobar(categorias.getCategorias().equals(
            List.of("LONGITUD", "MASA", "TEMPERATURA")), "catálogo de categorías incorrecto");

        verificarUnidades(servicio, token, "LONGITUD",
            List.of("KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE"));
        verificarUnidades(servicio, token, "MASA",
            List.of("KILOGRAMO", "GRAMO", "MILIGRAMO", "LIBRA"));
        verificarUnidades(servicio, token, "TEMPERATURA",
            List.of("CELSIUS", "FAHRENHEIT", "KELVIN"));

        VerificacionesREST.codigo(servicio.obtenerUnidades(token, "inexistente"), "DATA-001",
            "categoría inexistente");
        VerificacionesREST.codigo(servicio.obtenerCategorias(null), "AUTH-002", "catálogo sin token");
        VerificacionesREST.informar("PruebaConsultaCatalogo");
    }

    private static void verificarUnidades(ConversorUnidadesServices servicio, String token,
            String categoria, List<String> esperadas) {
        Respuesta respuesta = servicio.obtenerUnidades(token, categoria.toLowerCase());
        VerificacionesREST.codigo(respuesta, "OK", "consultar " + categoria);
        VerificacionesREST.comprobar(categoria.equals(respuesta.getCategoria()), "categoría normalizada incorrecta");
        VerificacionesREST.comprobar(esperadas.equals(respuesta.getUnidades()),
            "unidades incorrectas para " + categoria);
    }
}
