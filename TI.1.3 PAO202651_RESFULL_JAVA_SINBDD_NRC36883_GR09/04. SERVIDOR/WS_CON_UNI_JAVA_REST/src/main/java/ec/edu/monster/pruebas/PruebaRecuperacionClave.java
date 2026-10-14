package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaRecuperacionClave {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);

        VerificacionesREST.codigo(servicio.recuperarClave("desconocido", "nueva"), "AUTH-001",
            "recuperación para usuario inexistente");
        VerificacionesREST.codigo(servicio.recuperarClave("monster", " "), "DATA-001",
            "recuperación con clave vacía");

        Respuesta recuperacion = servicio.recuperarClave("monster", "clave-temporal");
        VerificacionesREST.codigo(recuperacion, "OK", "recuperación válida");
        VerificacionesREST.codigo(servicio.obtenerCategorias(token), "AUTH-002", "la recuperación invalida tokens");
        VerificacionesREST.codigo(servicio.autenticar("monster", "monster123"), "AUTH-001",
            "la clave anterior deja de servir");
        VerificacionesREST.codigo(servicio.autenticar("monster", "clave-temporal"), "OK",
            "la clave nueva permite autenticarse");

        VerificacionesREST.informar("PruebaRecuperacionClave");
    }
}
