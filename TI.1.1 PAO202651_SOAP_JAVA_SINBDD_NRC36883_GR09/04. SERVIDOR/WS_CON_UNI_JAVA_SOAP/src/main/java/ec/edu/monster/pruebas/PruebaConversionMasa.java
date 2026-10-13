package ec.edu.monster.pruebas;
public class PruebaConversionMasa {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        String[] unidades = {"KILOGRAMO", "GRAMO", "MILIGRAMO", "LIBRA"};
        double[] kilogramos = {1, 0.001, 0.000001, 0.45359237};
        for (int i=0; i<unidades.length; i++) {
            Verificaciones.resultado(s.convertir(t, "MASA", unidades[i], "KILOGRAMO", 1.0), kilogramos[i]);
            Verificaciones.resultado(s.convertir(t, "MASA", "KILOGRAMO", unidades[i], kilogramos[i]), 1);
        }
        System.out.println("PruebaConversionMasa: OK");
    }
}
