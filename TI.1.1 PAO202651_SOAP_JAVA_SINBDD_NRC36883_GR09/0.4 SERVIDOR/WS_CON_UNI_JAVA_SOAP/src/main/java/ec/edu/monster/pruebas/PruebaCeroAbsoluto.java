package ec.edu.monster.pruebas;
public class PruebaCeroAbsoluto {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        for (Object[] caso : new Object[][]{{"CELSIUS", -273.15}, {"FAHRENHEIT", -459.67}, {"KELVIN", 0.0}, {"NEWTON", -90.1395}, {"REAUMUR", -218.52}}) {
            Verificaciones.resultado(s.convertir(t, "TEMPERATURA", (String) caso[0], "KELVIN", (Double) caso[1]), 0);
            Verificaciones.codigo(s.convertir(t, "TEMPERATURA", (String) caso[0], "KELVIN", (Double) caso[1] - 0.000001), "DATA-001");
        }
        System.out.println("PruebaCeroAbsoluto: OK");
    }
}
