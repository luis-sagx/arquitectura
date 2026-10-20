using System;
using System.Drawing;
using System.Globalization;
using System.Reflection;
using System.Windows.Forms;
using ec.edu.monster.modelo;
using ec.edu.monster.vista;

namespace ec.edu.monster.pruebas
{
    internal static class PruebaClienteEscritorio
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

        private static void VentanasIgualanTamanoExteriorJava()
        {
            using (var acceso = new LoginForm())
            using (var conversiones = new ConversionForm())
            {
                Exigir(acceso.Size == new Size(790, 660), "LoginForm debe medir 790 x 660 incluyendo el marco; mide " + acceso.Size
                    + ", ClientSize=" + acceso.ClientSize + ", AutoScaleMode=" + acceso.AutoScaleMode
                    + ", AutoScaleDimensions=" + acceso.AutoScaleDimensions + ", CurrentAutoScaleDimensions=" + acceso.CurrentAutoScaleDimensions);
                Exigir(conversiones.Size == new Size(790, 660), "ConversionForm debe medir 790 x 660 incluyendo el marco; mide " + conversiones.Size
                    + ", ClientSize=" + conversiones.ClientSize + ", AutoScaleMode=" + conversiones.AutoScaleMode
                    + ", AutoScaleDimensions=" + conversiones.AutoScaleDimensions + ", CurrentAutoScaleDimensions=" + conversiones.CurrentAutoScaleDimensions);
                VerificarLimites(acceso, "LoginForm");
                VerificarLimites(conversiones, "ConversionForm");
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

        private static void PaletaCoincideConReferenciaJava()
        {
            Type estilos = typeof(LoginForm).Assembly.GetType("ec.edu.monster.vista.EstilosUI", true);
            Exigir(ColorDe(estilos, "Fondo") == Color.FromArgb(241, 244, 248), "Fondo debe ser RGB(241,244,248)");
            Exigir(ColorDe(estilos, "Tinta") == Color.FromArgb(29, 41, 57), "Tinta debe ser RGB(29,41,57)");
            Exigir(ColorDe(estilos, "TextoSecundario") == Color.FromArgb(91, 105, 122), "TextoSecundario debe ser RGB(91,105,122)");
            Exigir(ColorDe(estilos, "Acento") == Color.FromArgb(24, 50, 87), "Acento debe ser el azul marino RGB(24,50,87)");
            Exigir(ColorDe(estilos, "AcentoHover") == Color.FromArgb(35, 68, 112), "AcentoHover debe ser RGB(35,68,112)");
            Exigir(ColorDe(estilos, "Suave") == Color.FromArgb(232, 237, 244), "Suave debe ser RGB(232,237,244)");
            Exigir(ColorDe(estilos, "Borde") == Color.FromArgb(211, 219, 229), "Borde debe ser RGB(211,219,229)");
            Exigir(ColorDe(estilos, "Blanco") == Color.FromArgb(252, 253, 254), "Blanco debe ser RGB(252,253,254)");
        }

        private static void BotonesRedondeadosSinMarcoNegro()
        {
            Type estilos = typeof(LoginForm).Assembly.GetType("ec.edu.monster.vista.EstilosUI", true);
            MethodInfo fabrica = estilos.GetMethod("Boton", BindingFlags.Static | BindingFlags.Public | BindingFlags.NonPublic);
            Exigir(fabrica != null, "Debe existir la fábrica compartida de botones");
            using (var botonPrincipal = (Button)fabrica.Invoke(null, new object[] { "Primario", 0, 0, 180, 40, true }))
            using (var botonSecundario = (Button)fabrica.Invoke(null, new object[] { "Secundario", 0, 0, 180, 40, false }))
            {
                Exigir(botonPrincipal.FlatAppearance.BorderSize == 0, "El botón principal no debe pintar un borde nativo");
                Exigir(botonPrincipal.FlatAppearance.BorderColor == Color.FromArgb(24, 50, 87), "El botón principal debe usar borde azul marino");
                Exigir(botonSecundario.FlatAppearance.BorderSize == 0, "El botón secundario no debe pintar un borde nativo");
                Exigir(botonSecundario.FlatAppearance.BorderColor == Color.FromArgb(211, 219, 229), "El borde secundario debe ser gris suave");
            }
        }

        private static void SelectoresYValorTienenAlturaUniforme()
        {
            using (var form = new ConversionForm())
            {
                int alturaCategorias = ControlInterno(form, "categorias").Height;
                int alturaOrigen = ControlInterno(form, "origen").Height;
                int alturaDestino = ControlInterno(form, "destino").Height;
                int alturaValor = ControlInterno(form, "valor").Height;
                Exigir(alturaCategorias == 42, "El selector de categoría debe tener 42 px; tiene " + alturaCategorias);
                Exigir(alturaOrigen == alturaCategorias, "El selector de origen debe igualar la altura de categoría");
                Exigir(alturaDestino == alturaCategorias, "El selector de destino debe igualar la altura de categoría");
                Exigir(alturaValor == alturaCategorias, "El campo de valor debe igualar la altura de los selectores; tiene " + alturaValor);
            }
        }

        private static void SesionApareceDebajoConFechaLegible()
        {
            using (var form = new ConversionForm())
            {
                var fecha = new Sesion("token", "2026-10-15T18:26:44.5306397Z");
                form.MostrarSesion(fecha);
                Label etiquetaSesion = (Label)ControlInterno(form, "sesion");
                Label etiquetaMensaje = (Label)ControlInterno(form, "mensaje");
                Exigir(Object.ReferenceEquals(etiquetaSesion.Parent, form), "La sesión debe aparecer debajo de la tarjeta, fuera de ella");
                Exigir(etiquetaSesion.Top > 556, "La sesión debe estar en el pie de la ventana");
                Exigir(etiquetaSesion.Bottom <= form.ClientSize.Height - 10, "La sesión debe conservar al menos 10 px de margen inferior");
                Exigir(etiquetaSesion.Text.Contains("15/10/2026"), "La fecha debe usar un formato corto legible: " + etiquetaSesion.Text);
                Exigir(!etiquetaSesion.Text.Contains("T18") && !etiquetaSesion.Text.EndsWith("Z"), "La fecha no debe mostrarse como timestamp ISO crudo");
                Exigir(etiquetaSesion.Top == etiquetaMensaje.Top, "La sesión y la validación deben compartir una sola franja horizontal");
                Exigir(etiquetaSesion.TextAlign == ContentAlignment.MiddleRight, "La sesión debe alinearse a la derecha");
            }
        }

        private static void ValidacionesTienenZonaSeparada()
        {
            using (var form = new ConversionForm())
            {
                form.MostrarMensaje("DATA-001 · La masa y la longitud no pueden ser negativas", true);
                Label etiquetaMensaje = (Label)ControlInterno(form, "mensaje");
                Label etiquetaSesion = (Label)ControlInterno(form, "sesion");
                Exigir(Object.ReferenceEquals(etiquetaMensaje.Parent, form), "Las validaciones deben usar el área inferior del formulario");
                Exigir(etiquetaMensaje.Text.StartsWith("DATA-001"), "El mensaje de validación debe permanecer visible");
                Exigir(etiquetaMensaje.ForeColor == Color.FromArgb(190, 44, 58), "La validación debe conservar el color de error");
                Exigir(etiquetaMensaje.Height == 24, "El mensaje debe ocupar una sola línea compacta");
                Exigir(etiquetaMensaje.AutoEllipsis, "El mensaje largo debe ajustarse a su columna con puntos suspensivos");
                Exigir(etiquetaMensaje.Top == etiquetaSesion.Top, "El mensaje y la fecha deben compartir una sola franja horizontal");
                Exigir(etiquetaMensaje.Right < etiquetaSesion.Left, "El mensaje debe quedar a la izquierda de la fecha de sesión");
                ToolTip ayuda = (ToolTip)typeof(ConversionForm).GetField("ayudaMensajes", BindingFlags.Instance | BindingFlags.NonPublic).GetValue(form);
                Exigir(ayuda.GetToolTip(etiquetaMensaje) == etiquetaMensaje.Text, "El tooltip debe conservar el texto completo de validación");
            }
        }

        private static void ResultadoMuestraMaximoCuatroDecimales()
        {
            CultureInfo culturaAnterior = CultureInfo.CurrentCulture;
            CultureInfo.CurrentCulture = new CultureInfo("es-EC");
            try
            {
                using (var form = new ConversionForm())
                {
                    Label etiquetaResultado = (Label)ControlInterno(form, "resultado");
                    form.MostrarMensaje("Convirtiendo…", false);
                    form.MostrarResultado(100, RespuestaExito(0.220462262184878));
                    Exigir(etiquetaResultado.Text == "100 GRAMO = 0,2205 LIBRA", "El resultado debe redondearse a cuatro decimales: " + etiquetaResultado.Text);
                    Exigir(((Label)ControlInterno(form, "mensaje")).Text == String.Empty, "Al completar la conversión no debe aparecer 'Conversión realizada' ni otro mensaje de éxito");

                    form.MostrarResultado(1, RespuestaExito(1.5));
                    Exigir(etiquetaResultado.Text == "1 GRAMO = 1,5 LIBRA", "El resultado debe quitar ceros decimales innecesarios: " + etiquetaResultado.Text);
                }
            }
            finally { CultureInfo.CurrentCulture = culturaAnterior; }
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

        private static void AbrirConversionesReutilizaInstanciaVigente()
        {
            PrepararGestor();
            int creaciones = 0;
            Func<ConversionForm> fabrica = () =>
            {
                creaciones++;
                return new ConversionForm();
            };
            ventanaConversiones = MostrarConversiones(fabrica);
            ConversionForm segundaSolicitud = MostrarConversiones(fabrica);
            Exigir(Object.ReferenceEquals(ventanaConversiones, segundaSolicitud), "El gestor debe devolver la misma ventana activa");
            Exigir(creaciones == 1, "El gestor no debe invocar la fábrica otra vez si ya hay ventana activa");
        }

        private static void CrearVentanaTrasCerrarLaAnterior()
        {
            PrepararGestor();
            Exigir(ventanaConversiones != null, "El caso anterior debe registrar una ventana activa");
            ConversionForm anterior = ventanaConversiones;
            anterior.Close();
            anterior.Dispose();
            Application.DoEvents();

            int creaciones = 0;
            ConversionForm nueva = MostrarConversiones(() =>
            {
                creaciones++;
                return new ConversionForm();
            });
            Exigir(!Object.ReferenceEquals(anterior, nueva), "Tras cerrar la ventana debe crearse una instancia nueva");
            Exigir(creaciones == 1 && !nueva.IsDisposed, "La ventana nueva debe quedar activa después de cerrar la anterior");
            nueva.Close();
            nueva.Dispose();
            Application.DoEvents();
            accesoPrueba.Close();
            accesoPrueba.Dispose();
            accesoPrueba = null;
            gestor = null;
            tipoGestor = null;
            ventanaConversiones = null;
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
