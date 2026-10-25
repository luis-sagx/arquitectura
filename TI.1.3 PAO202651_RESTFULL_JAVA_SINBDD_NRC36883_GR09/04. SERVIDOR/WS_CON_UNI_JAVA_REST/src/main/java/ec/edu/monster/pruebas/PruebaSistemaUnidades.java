package ec.edu.monster.pruebas;

public class PruebaSistemaUnidades {
    public static void main(String[] args) {
        PruebaAutenticacion.main(args);
        PruebaConsultaCatalogo.main(args);
        PruebaConversionLongitud.main(args);
        PruebaConversionMasa.main(args);
        PruebaConversionTemperatura.main(args);
        PruebaValidacionConversion.main(args);
        PruebaSesionExpirada.main(args);
        PruebaCeroAbsoluto.main(args);
        PruebaTemperaturaValoresGrandes.main(args);
        PruebaRecuperacionClave.main(args);
        System.out.println("Sistema REST: " + VerificacionesREST.realizadas() + " verificaciones OK");
    }
}
