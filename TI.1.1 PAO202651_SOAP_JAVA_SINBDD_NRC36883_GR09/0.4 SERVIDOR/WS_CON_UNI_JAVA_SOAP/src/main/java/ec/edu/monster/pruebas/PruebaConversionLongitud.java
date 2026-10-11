package ec.edu.monster.pruebas;
public class PruebaConversionLongitud {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        String[] unidades = {"KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE"};
        double[] metros = {1000, 1, 0.01, 0.001, 1609.344, 0.3048};
        for (int i=0; i<unidades.length; i++) {
            Verificaciones.resultado(s.convertir(t, "LONGITUD", unidades[i], "METRO", 1.0), metros[i]);
            Verificaciones.resultado(s.convertir(t, "LONGITUD", "METRO", unidades[i], metros[i]), 1);
            Verificaciones.resultado(s.convertir(t, "LONGITUD", unidades[i], unidades[i], 7.5), 7.5);
        }
        Verificaciones.resultado(s.convertir(t, "longitud", "metro", "centimetro", 2.0), 200);
        Verificaciones.resultado(s.convertir(t, "LONGITUD", "METRO", "CENTIMETRO", 0.0), 0);
        Verificaciones.resultado(s.convertir(t, "LONGITUD", "METRO", "CENTIMETRO", -2.0), -200);
        System.out.println("PruebaConversionLongitud: OK");
    }
}
