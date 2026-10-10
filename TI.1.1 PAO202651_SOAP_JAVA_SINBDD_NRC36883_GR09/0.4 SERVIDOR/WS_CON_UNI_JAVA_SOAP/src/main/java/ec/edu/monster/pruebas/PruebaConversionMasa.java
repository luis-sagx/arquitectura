package ec.edu.monster.pruebas;
public class PruebaConversionMasa {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        String[] unidades = {"GRAMO", "KILOGRAMO", "ONZA", "LIBRA", "QUINTAL"};
        double[] kilogramos = {0.001, 1, 0.028349523125, 0.45359237, 45.359237};
        for (int i=0; i<unidades.length; i++) {
            Verificaciones.resultado(s.convertir(t, "MASA", unidades[i], "KILOGRAMO", 1.0), kilogramos[i]);
            Verificaciones.resultado(s.convertir(t, "MASA", "KILOGRAMO", unidades[i], kilogramos[i]), 1);
        }
        Verificaciones.resultado(s.convertir(t, "MASA", "QUINTAL", "LIBRA", 1.0), 100);
        System.out.println("PruebaConversionMasa: OK");
    }
}
