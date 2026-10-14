package ec.edu.monster.pruebas;

import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaAutenticacion {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        VerificacionesREST.codigo(servicio.autenticar("otro", "monster123"), "AUTH-001", "usuario incorrecto");
        VerificacionesREST.codigo(servicio.autenticar("monster", "incorrecta"), "AUTH-001", "clave incorrecta");

        Respuesta respuesta = servicio.autenticar("monster", "monster123");
        VerificacionesREST.codigo(respuesta, "OK", "credenciales correctas");
        VerificacionesREST.comprobar(respuesta.getToken() != null && !respuesta.getToken().isBlank(),
            "la autenticación debe entregar token");
        VerificacionesREST.comprobar(respuesta.getExpiraEn() != null && !respuesta.getExpiraEn().isBlank(),
            "la autenticación debe entregar vencimiento");
        VerificacionesREST.informar("PruebaAutenticacion");
    }
}
