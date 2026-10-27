using System;
using System.Drawing;
using System.Globalization;
using System.Reflection;
using System.Windows.Forms;
using ec.edu.monster.modelo;
using ec.edu.monster.vista;

namespace ec.edu.monster.pruebas
{
    internal static partial class PruebaClienteEscritorio
    {
        private static int comprobaciones;
        private static int fallos;
        private static Type tipoGestor;
        private static object gestor;
        private static LoginForm accesoPrueba;
        private static ConversionForm ventanaConversiones;

        [STAThread]
        private static int Main()
        {
            Ejecutar("VentanasIgualanTamanoExteriorJava", VentanasIgualanTamanoExteriorJava);
            Ejecutar("PaletaCoincideConReferenciaJava", PaletaCoincideConReferenciaJava);
            Ejecutar("BotonesRedondeadosSinMarcoNegro", BotonesRedondeadosSinMarcoNegro);
            Ejecutar("SelectoresYValorTienenAlturaUniforme", SelectoresYValorTienenAlturaUniforme);
            Ejecutar("SesionApareceDebajoConFechaLegible", SesionApareceDebajoConFechaLegible);
            Ejecutar("ValidacionesTienenZonaSeparada", ValidacionesTienenZonaSeparada);
            Ejecutar("ResultadoMuestraMaximoCuatroDecimales", ResultadoMuestraMaximoCuatroDecimales);
            Ejecutar("AbrirConversionesReutilizaInstanciaVigente", AbrirConversionesReutilizaInstanciaVigente);
            Ejecutar("CrearVentanaTrasCerrarLaAnterior", CrearVentanaTrasCerrarLaAnterior);

            if (fallos == 0)
            {
                Console.WriteLine("PruebaClienteEscritorio: OK (" + comprobaciones + " comprobaciones)");
                return 0;
            }

            Console.Error.WriteLine("PruebaClienteEscritorio: " + fallos + " fallo(s) de " + comprobaciones + " comprobaciones");
            return 1;
        }

        private static void Ejecutar(string nombre, Action prueba)
        {
            comprobaciones++;
            try
            {
                prueba();
                Console.WriteLine("OK: " + nombre);
            }
            catch (Exception error)
            {
                fallos++;
                Console.Error.WriteLine("FAIL: " + nombre + " — " + error.Message);
            }
        }

        private static void VerificarLimites(Control contenedor, string nombre)
        {
            foreach (Control hijo in contenedor.Controls)
            {
                Exigir(contenedor.ClientRectangle.Contains(hijo.Bounds), nombre + " recorta " + hijo.GetType().Name
                    + " (" + hijo.Bounds + " dentro de " + contenedor.ClientRectangle + ")");
                if (hijo.HasChildren) VerificarLimites(hijo, nombre + "/" + hijo.GetType().Name);
            }
        }

        private static RespuestaSoap RespuestaExito(double resultado)
        {
            return new RespuestaSoap("EXITO", "OK", "Conversión realizada", String.Empty,
                String.Empty, resultado, "MASA", "GRAMO", "LIBRA", null, null);
        }

        private static Control ControlInterno(ConversionForm form, string nombre)
        {
            FieldInfo campo = typeof(ConversionForm).GetField(nombre, BindingFlags.Instance | BindingFlags.NonPublic);
            Exigir(campo != null, "Falta el control " + nombre);
            return (Control)campo.GetValue(form);
        }

        private static Color ColorDe(Type tipo, string nombre)
        {
            FieldInfo campo = tipo.GetField(nombre, BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
            Exigir(campo != null, "Falta el color compartido " + nombre);
            return (Color)campo.GetValue(null);
        }

        private static void PrepararGestor()
        {
            if (tipoGestor == null)
            {
                tipoGestor = typeof(LoginForm).Assembly.GetType("ec.edu.monster.vista.GestorVentanas", false);
                Exigir(tipoGestor != null, "Debe existir el singleton GestorVentanas");
                PropertyInfo propiedad = tipoGestor.GetProperty("Instancia", BindingFlags.Static | BindingFlags.Public);
                Exigir(propiedad != null, "GestorVentanas debe exponer Instancia");
                gestor = propiedad.GetValue(null, null);
            }

            if (accesoPrueba == null)
            {
                accesoPrueba = new LoginForm();
                MethodInfo registrar = tipoGestor.GetMethod("RegistrarAcceso", new[] { typeof(LoginForm) });
                Exigir(registrar != null, "GestorVentanas debe registrar la ventana de acceso");
                registrar.Invoke(gestor, new object[] { accesoPrueba });
            }
        }

        private static ConversionForm MostrarConversiones(Func<ConversionForm> fabrica)
        {
            MethodInfo mostrar = tipoGestor.GetMethod("MostrarConversiones", new[] { typeof(Func<ConversionForm>) });
            Exigir(mostrar != null, "GestorVentanas debe mostrar conversiones mediante una fábrica");
            return (ConversionForm)mostrar.Invoke(gestor, new object[] { fabrica });
        }

        private static void Exigir(bool condicion, string mensaje)
        {
            comprobaciones++;
            if (!condicion) throw new InvalidOperationException(mensaje);
        }
    }
}
