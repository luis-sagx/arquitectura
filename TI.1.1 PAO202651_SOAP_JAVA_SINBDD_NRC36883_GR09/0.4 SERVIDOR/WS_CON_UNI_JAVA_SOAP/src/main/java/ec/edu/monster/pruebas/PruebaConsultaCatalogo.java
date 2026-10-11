package ec.edu.monster.pruebas;
public class PruebaConsultaCatalogo {
    public static void main(String[] args) {
        var s = Verificaciones.servicio(); String t = Verificaciones.token(s);
        var r = s.obtenerCategorias(t); Verificaciones.codigo(r, "OK");
        Verificaciones.comprobar(r.getCategorias().equals(java.util.List.of("LONGITUD", "MASA", "TEMPERATURA")), "Catalogo incompleto");
        for (String c : r.getCategorias()) {
            var u = s.obtenerUnidades(t, c); Verificaciones.codigo(u, "OK");
            Verificaciones.comprobar(!u.getUnidades().isEmpty(), "Categoria sin unidades: " + c);
        }
        Verificaciones.comprobar(s.obtenerUnidades(t, "LONGITUD").getUnidades().contains("METRO"), "Falta metro");
        Verificaciones.comprobar(s.obtenerUnidades(t, "TEMPERATURA").getUnidades()
            .equals(java.util.List.of("CELSIUS", "FAHRENHEIT", "KELVIN")), "Unidades de temperatura incorrectas");
        Verificaciones.codigo(s.obtenerCategorias(null), "AUTH-002");
        Verificaciones.codigo(s.obtenerUnidades(t, "INEXISTENTE"), "DATA-001");
        r.getCategorias().clear();
        Verificaciones.comprobar(s.obtenerCategorias(t).getCategorias().size() == 3, "El cliente no debe modificar el catalogo interno");
        System.out.println("PruebaConsultaCatalogo: OK");
    }
}
