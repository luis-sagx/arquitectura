package ec.edu.monster.pruebas;
public class PruebaClienteConsola {
    public static void main(String[] args) throws Exception {
        PruebaAutenticacion.main(args);
        PruebaRecuperacionClave.main(args);
        PruebaConsultaCatalogo.main(args);
        PruebaConsultaUnidades.main(args);
        PruebaConversion.main(args);
        PruebaTransporteSOAP.main(args);
        PruebaMenuConsola.main(args);
        PruebaCredencialesConEspacios.main(args);
        System.out.println("Todas las pruebas locales del cliente: OK");
    }
}
