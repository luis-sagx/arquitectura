package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ConversorUnidadesServices;

public class PruebaValidacionConversion {
    public static void main(String[] args) {
        ConversorUnidadesServices servicio = VerificacionesREST.servicio();
        String token = VerificacionesREST.token(servicio);
        VerificacionesREST.codigo(servicio.convertir(token, "INEXISTENTE", "METRO", "PIE", 1.0),
            "DATA-001", "categoría desconocida");
        VerificacionesREST.codigo(servicio.convertir(token, "LONGITUD", "METRO", "YARDA", 1.0),
            "UNIT-001", "unidad no soportada");
        VerificacionesREST.codigo(servicio.convertir(token, "LONGITUD", "METRO", "GRAMO", 1.0),
            "DATA-001", "magnitudes incompatibles");
        VerificacionesREST.codigo(servicio.convertir(token, "LONGITUD", "", "METRO", 1.0),
            "DATA-001", "unidad de origen vacía");
        VerificacionesREST.codigo(servicio.convertir(token, "LONGITUD", "METRO", "PIE", null),
            "DATA-001", "valor ausente");
        VerificacionesREST.codigo(servicio.convertir(token, "LONGITUD", "METRO", "PIE", Double.NaN),
            "DATA-001", "valor NaN");
        VerificacionesREST.codigo(servicio.convertir(token, "LONGITUD", "METRO", "PIE", -0.01),
            "DATA-001", "longitud negativa");
        VerificacionesREST.codigo(servicio.convertir(token, "MASA", "KILOGRAMO", "GRAMO", -0.01),
            "DATA-001", "masa negativa");
        VerificacionesREST.resultado(servicio.convertir(token, "MASA", "KILOGRAMO", "GRAMO", 0.0),
            0, "masa cero permitida");
        VerificacionesREST.codigo(servicio.convertir(null, "LONGITUD", "METRO", "PIE", 1.0),
            "AUTH-002", "conversión sin token");
        VerificacionesREST.informar("PruebaValidacionConversion");
    }
}
