package ec.edu.monster.pruebas;
public class PruebaTemperaturaValoresGrandes {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", 1e308), 5.555555555555556e307);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "FAHRENHEIT", "FAHRENHEIT", 1e308), 1e308);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "CELSIUS", "KELVIN", 1e308), 1e308);
        System.out.println("PruebaTemperaturaValoresGrandes: OK");
    }
}
