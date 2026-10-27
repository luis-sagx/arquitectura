package ec.edu.monster.pruebas;

public final class PruebaClienteSoap {
    public static void main(String[] args) throws Exception {
        PruebaAutenticacion.ejecutar();
        PruebaRecuperacionClave.ejecutar();
        PruebaConsultaCatalogo.ejecutar();
        PruebaConsultaUnidades.ejecutar();
        PruebaConversion.ejecutar();
        System.out.println("Pruebas SOAP del cliente: OK");
    }
}

