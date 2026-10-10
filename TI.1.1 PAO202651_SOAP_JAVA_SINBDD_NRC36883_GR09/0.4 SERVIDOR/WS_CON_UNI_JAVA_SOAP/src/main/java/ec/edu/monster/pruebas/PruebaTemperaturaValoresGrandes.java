package ec.edu.monster.pruebas;
public class PruebaTemperaturaValoresGrandes {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "FAHRENHEIT", "CELSIUS", 1e308), 5.555555555555556e307);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "FAHRENHEIT", "FAHRENHEIT", 1e308), 1e308);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "CELSIUS", "NEWTON", 1e308), 3.3e307);
        Verificaciones.codigo(s.convertir(t, "TEMPERATURA", "NEWTON", "KELVIN", 1e308), "DATA-001");
        System.out.println("PruebaTemperaturaValoresGrandes: OK");
    }
}
