package ec.edu.monster.pruebas;

public final class PruebaClienteRest {
    public static void main(String[] args) throws Exception {
        PruebaAutenticacion.ejecutar();
        PruebaRecuperacionClave.ejecutar();
        PruebaConsultaCatalogo.ejecutar();
        PruebaConsultaUnidades.ejecutar();
        PruebaConversion.ejecutar();
        System.out.println("Pruebas REST del cliente: OK");
    }
}

