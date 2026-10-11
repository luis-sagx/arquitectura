package ec.edu.monster.pruebas;
public class PruebaConversionTemperatura {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        String[] unidades = {"CELSIUS", "FAHRENHEIT", "KELVIN"};
        double[] ebullicion = {100, 212, 373.15};
        for (int i=0; i<unidades.length; i++) {
            Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "CELSIUS", unidades[i], 100.0), ebullicion[i]);
            Verificaciones.resultado(s.convertir(t, "TEMPERATURA", unidades[i], "CELSIUS", ebullicion[i]), 100);
        }
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "CELSIUS", "FAHRENHEIT", -40.0), -40);
        Verificaciones.resultado(s.convertir(t, "TEMPERATURA", "KELVIN", "CELSIUS", 0.0), -273.15);
        Verificaciones.codigo(s.convertir(t, "TEMPERATURA", "KELVIN", "CELSIUS", -1.0), "DATA-001");
        System.out.println("PruebaConversionTemperatura: OK");
    }
}
