using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using ec.edu.monster.modelo;

namespace ec.edu.monster.consola.vista
{
    /// <summary>Presenta el menú y valida las entradas de consola.</summary>
    internal sealed class VistaConsola
    {
        private const string Separador = "------------------------------------------------------------";

        public void MostrarMenuPrincipal(bool sesionIniciada)
        {
            Seccion("MENÚ PRINCIPAL");
            Console.WriteLine("Sesión: " + (sesionIniciada ? "iniciada" : "no iniciada"));
            Console.WriteLine();
            Console.WriteLine("Acciones:");
            Console.WriteLine("  1. Iniciar sesión");
            Console.WriteLine("  2. Consultar catálogo");
            Console.WriteLine("  3. Convertir unidades");
            Console.WriteLine("  4. Cerrar sesión local");
            Console.WriteLine("  5. Recuperar contraseña");
            Console.WriteLine("  0. Salir");
            Console.WriteLine(Separador);
        }

        public void MostrarSeccion(string titulo) { Seccion(titulo); }

        public void MostrarPasoConversion(int paso, string categoria = null,
            string origen = null, string destino = null)
        {
            string[] etapas = { "Categoría", "Unidad de origen", "Unidad de destino", "Cantidad" };
            Seccion(string.Format("CONVERTIR UNIDADES | Paso {0}/4: {1}", paso, etapas[paso - 1]));
            if (categoria != null) Console.WriteLine("Categoría: " + categoria);
            if (origen != null) Console.WriteLine("Origen:    " + origen);
            if (destino != null) Console.WriteLine("Destino:   " + destino);
            Console.WriteLine();
        }

        public void EsperarParaContinuar()
        {
            if (Console.IsInputRedirected || Console.IsOutputRedirected) return;
            Console.WriteLine();
            Console.WriteLine(Separador);
            LeerLiteral("Presiona Enter para volver al menú principal...");
        }

        public string Leer(string mensaje)
        {
            Console.Write(mensaje);
            string linea = Console.ReadLine();
            if (linea == null) throw new FinEntradaException();
            return linea.Trim();
        }

        public string LeerLiteral(string mensaje)
        {
            Console.Write(mensaje);
            string linea = Console.ReadLine();
            if (linea == null) throw new FinEntradaException();
            return linea;
        }

        public string LeerClave(string mensaje)
        {
            if (Console.IsInputRedirected) return LeerLiteral(mensaje);

            Console.Write(mensaje);
            var caracteres = new List<char>();
            while (true)
            {
                ConsoleKeyInfo tecla = Console.ReadKey(true);
                if (tecla.Key == ConsoleKey.Enter)
                {
                    Console.WriteLine();
                    return new string(caracteres.ToArray());
                }
                if (tecla.Key == ConsoleKey.Backspace)
                {
                    if (caracteres.Count > 0)
                    {
                        caracteres.RemoveAt(caracteres.Count - 1);
                        Console.Write("\b \b");
                    }
                    continue;
                }
                if (!char.IsControl(tecla.KeyChar))
                {
                    caracteres.Add(tecla.KeyChar);
                    Console.Write('*');
                }
            }
        }

        public string Seleccionar(string titulo, IList<string> opciones)
        {
            if (opciones == null || opciones.Count == 0)
            {
                Console.WriteLine("No hay opciones disponibles.");
                return null;
            }

            Console.WriteLine();
            Console.WriteLine(titulo + ":");
            Console.WriteLine();
            for (int i = 0; i < opciones.Count; i++)
                Console.WriteLine("  {0}. {1}", i + 1, opciones[i]);
            Console.WriteLine("  0. Volver al menú principal");
            Console.WriteLine();

            while (true)
            {
                int opcion;
                if (int.TryParse(Leer(string.Format("Opción (0-{0}): ", opciones.Count)), out opcion))
                {
                    if (opcion == 0) return null;
                    if (opcion >= 1 && opcion <= opciones.Count) return opciones[opcion - 1];
                }
                Console.WriteLine("Opción inválida. Selecciona una de las unidades u opciones de la lista.");
                Console.WriteLine();
            }
        }

        public double LeerNumero()
        {
            Console.WriteLine("Ingresa la cantidad que deseas convertir.");
            Console.WriteLine();
            while (true)
            {
                string entrada = Leer("Valor: ");
                if (entrada.IndexOf(',') >= 0 && entrada.IndexOf('.') < 0 && entrada.IndexOf(',') == entrada.LastIndexOf(','))
                    entrada = entrada.Replace(',', '.');

                double valor;
                if (double.TryParse(entrada, NumberStyles.Float, CultureInfo.InvariantCulture, out valor)
                    && !double.IsNaN(valor) && !double.IsInfinity(valor))
                    return valor;

                Console.WriteLine("Ingrese un número finito; puede usar punto o coma decimal.");
            }
        }

        public void MostrarRespuesta(RespuestaSoap respuesta)
        {
            Console.WriteLine();
            Console.WriteLine("{0}{1}: {2}", respuesta.EsExito ? string.Empty : "[ERROR] ",
                respuesta.Codigo, respuesta.Mensaje);
        }

        public void MostrarError(string mensaje)
        {
            Console.WriteLine();
            Console.WriteLine("Error: " + mensaje);
        }
        public void MostrarMensaje(string mensaje) { Console.WriteLine(mensaje); }

        public void MostrarUnidades(IList<string> unidades)
        {
            Console.WriteLine();
            Console.WriteLine("Unidades disponibles:");
            Console.WriteLine();
            foreach (string unidad in unidades) Console.WriteLine("- " + unidad);
        }

        public void MostrarConversion(double valor, string origen, double resultado, string destino)
        {
            Console.WriteLine();
            Console.WriteLine("Conversión completada:");
            Console.WriteLine(Separador);
            Console.WriteLine("    {0:F4} {1} = {2:F4} {3}", valor, origen, resultado, destino);
            Console.WriteLine(Separador);
        }

        private static void Seccion(string titulo)
        {
            if (!Console.IsInputRedirected && !Console.IsOutputRedirected)
            {
                try { Console.Clear(); }
                catch (IOException) { /* Algunas terminales no permiten limpiar la pantalla. */ }
            }
            Console.WriteLine();
            Console.WriteLine("CONVERTIDOR DE UNIDADES");
            Console.WriteLine(Separador);
            Console.WriteLine(titulo);
            Console.WriteLine(Separador);
            Console.WriteLine();
        }

        internal sealed class FinEntradaException : Exception { }
    }
}
