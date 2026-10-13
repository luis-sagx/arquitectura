package ec.edu.monster.pruebas;
public class PruebaClienteConsola {
    public static void main(String[] args) throws Exception {
        PruebaTransporteSOAP.main(args);
        PruebaMenuConsola.main(args);
        PruebaCredencialesConEspacios.main(args);
        System.out.println("Todas las pruebas locales del cliente: OK");
    }
}
