package ec.edu.monster.pruebas;
public class PruebaAutenticacion {
    public static void main(String[] args) {
        var s = Verificaciones.servicio();
        String t1 = Verificaciones.token(s), t2 = Verificaciones.token(s);
        Verificaciones.comprobar(!t1.equals(t2), "Cada sesion debe tener un token diferente");
        for (String[] credenciales : new String[][]{{"Monster", "incorrecta"}, {"desconocido", "Monster9"}, {null, null}, {"", ""}}) {
            var r = s.autenticar(credenciales[0], credenciales[1]);
            Verificaciones.codigo(r, "AUTH-001");
            Verificaciones.comprobar(r.getToken() == null, "Credenciales invalidas no deben generar token");
        }
        System.out.println("PruebaAutenticacion: OK");
    }
}
