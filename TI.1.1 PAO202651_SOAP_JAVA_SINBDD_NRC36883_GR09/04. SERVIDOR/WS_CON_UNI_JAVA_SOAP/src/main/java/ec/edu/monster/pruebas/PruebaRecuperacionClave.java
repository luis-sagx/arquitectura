package ec.edu.monster.pruebas;

public class PruebaRecuperacionClave {
    public static void main(String[] args) {
        var servicio = Verificaciones.servicio();
        String tokenAnterior = Verificaciones.token(servicio);

        Verificaciones.codigo(servicio.recuperarClave("desconocido", "nueva123"), "AUTH-001");
        Verificaciones.codigo(servicio.recuperarClave("Monster", "  "), "DATA-001");
        Verificaciones.codigo(servicio.recuperarClave("Monster", "nueva123"), "OK");

        Verificaciones.codigo(servicio.autenticar("Monster", "Monster9"), "AUTH-001");
        Verificaciones.codigo(servicio.autenticar("Monster", "nueva123"), "OK");
        Verificaciones.codigo(servicio.obtenerCategorias(tokenAnterior), "AUTH-002");
        System.out.println("PruebaRecuperacionClave: OK");
    }
}
