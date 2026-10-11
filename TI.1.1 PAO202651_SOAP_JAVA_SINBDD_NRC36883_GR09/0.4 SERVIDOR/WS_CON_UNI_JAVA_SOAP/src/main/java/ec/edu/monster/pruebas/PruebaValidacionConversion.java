package ec.edu.monster.pruebas;
public class PruebaValidacionConversion {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        for (Double valor : new Double[]{null, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
            Verificaciones.codigo(s.convertir(t, "LONGITUD", "METRO", "PIE", valor), "DATA-001");
        Verificaciones.codigo(s.convertir(t, null, "METRO", "PIE", 1.0), "DATA-001");
        Verificaciones.codigo(s.convertir(t, "LONGITUD", "", "PIE", 1.0), "DATA-001");
        Verificaciones.codigo(s.convertir(t, "LONGITUD", "METRO", "LIBRA", 1.0), "DATA-001");
        Verificaciones.codigo(s.convertir(t, "LONGITUD", "DESCONOCIDA", "METRO", 1.0), "UNIT-001");
        Verificaciones.codigo(s.convertir(t, "INEXISTENTE", "METRO", "PIE", 1.0), "DATA-001");
        Verificaciones.codigo(s.convertir(t, "LONGITUD", "MILLA", "CENTIMETRO", Double.MAX_VALUE), "DATA-001");
        Verificaciones.codigo(s.convertir("inventado", null, null, null, null), "AUTH-002");
        Verificaciones.comprobar("METRO".equals(s.convertir(t, "LONGITUD", "METRO", "PIE", 1.0).getUnidadOrigen()), "Respuesta debe identificar unidades");
        System.out.println("PruebaValidacionConversion: OK");
    }
}
