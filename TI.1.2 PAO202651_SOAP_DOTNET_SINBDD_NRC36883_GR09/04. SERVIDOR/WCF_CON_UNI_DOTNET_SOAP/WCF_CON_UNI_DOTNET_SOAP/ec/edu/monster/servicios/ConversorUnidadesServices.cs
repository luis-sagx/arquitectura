using System;
using System.Collections.Generic;
using System.Configuration;
using System.Globalization;
using System.Text;
using ec.edu.monster.modelos;

namespace ec.edu.monster.servicios
{
    /// <summary>Logica de negocio independiente del transporte SOAP y sin base de datos.</summary>
    public class ConversorUnidadesServices
    {
        private readonly object sincronizacion = new object();
        private readonly string usuario;
        private string clave;
        private readonly Func<DateTimeOffset> reloj;
        private readonly TimeSpan duracion;
        private readonly Dictionary<string, DateTimeOffset> sesiones = new Dictionary<string, DateTimeOffset>(StringComparer.Ordinal);
        private readonly List<string> categorias = new List<string> { "LONGITUD", "MASA", "VOLUMEN", "TIEMPO", "TEMPERATURA", "AREA" };
        private readonly Dictionary<string, List<string>> catalogo = new Dictionary<string, List<string>>(StringComparer.Ordinal);
        private readonly Dictionary<string, double> longitud = new Dictionary<string, double>(StringComparer.Ordinal);
        private readonly Dictionary<string, double> masa = new Dictionary<string, double>(StringComparer.Ordinal);

        public ConversorUnidadesServices()
            : this(LeerConfiguracion("conversor.usuario", "Monster"),
                   LeerConfiguracion("conversor.clave", "Monster9"),
                   () => DateTimeOffset.UtcNow,
                   LeerDuracion())
        {
        }

        public ConversorUnidadesServices(string usuario, string clave, Func<DateTimeOffset> reloj, TimeSpan duracion)
        {
            if (String.IsNullOrWhiteSpace(usuario) || String.IsNullOrWhiteSpace(clave))
                throw new ArgumentException("Las credenciales deben estar configuradas");
            if (reloj == null) throw new ArgumentNullException("reloj");
            if (duracion <= TimeSpan.Zero) throw new ArgumentException("La duracion de la sesion debe ser positiva");

            this.usuario = usuario;
            this.clave = clave;
            this.reloj = reloj;
            this.duracion = duracion;

            catalogo.Add("LONGITUD", new List<string> { "KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE" });
            catalogo.Add("MASA", new List<string> { "KILOGRAMO", "GRAMO", "MILIGRAMO", "LIBRA" });
            catalogo.Add("TEMPERATURA", new List<string> { "CELSIUS", "FAHRENHEIT", "KELVIN" });
            catalogo.Add("VOLUMEN", new List<string> { "MILILITRO", "LITRO", "METRO_CUBICO", "GALON_US" });
            catalogo.Add("TIEMPO", new List<string> { "SEGUNDO", "MINUTO", "HORA", "DIA" });
            catalogo.Add("AREA", new List<string> { "METRO_CUADRADO", "KILOMETRO_CUADRADO", "HECTAREA", "ACRE" });

            longitud.Add("KILOMETRO", 1000.0);
            longitud.Add("METRO", 1.0);
            longitud.Add("CENTIMETRO", 0.01);
            longitud.Add("MILIMETRO", 0.001);
            longitud.Add("MILLA", 1609.344);
            longitud.Add("PIE", 0.3048);

            masa.Add("KILOGRAMO", 1.0);
            masa.Add("GRAMO", 0.001);
            masa.Add("MILIGRAMO", 0.000001);
            masa.Add("LIBRA", 0.45359237);
        }

        public Respuesta Autenticar(string usuario, string clave)
        {
            lock (sincronizacion)
            {
                if (!String.Equals(this.usuario, usuario, StringComparison.Ordinal) || !ClavesIguales(this.clave, clave))
                    return Respuesta.Error("AUTH-001", "Credenciales invalidas");

                DateTimeOffset ahora = reloj().ToUniversalTime();
                DateTimeOffset umbral = ahora.AddDays(-1);
                var vencidas = new List<string>();
                foreach (KeyValuePair<string, DateTimeOffset> sesion in sesiones)
                    if (sesion.Value < umbral) vencidas.Add(sesion.Key);
                foreach (string tokenVencido in vencidas) sesiones.Remove(tokenVencido);

                DateTimeOffset vencimiento = ahora.Add(duracion);
                string token = Guid.NewGuid().ToString("D").ToLowerInvariant();
                sesiones[token] = vencimiento;
                Respuesta respuesta = Respuesta.Exito("Sesion iniciada");
                respuesta.Token = token;
                respuesta.ExpiraEn = FormatearInstante(vencimiento);
                return respuesta;
            }
        }

        public Respuesta RecuperarClave(string usuario, string nuevaClave)
        {
            lock (sincronizacion)
            {
                if (usuario == null || !String.Equals(this.usuario, usuario, StringComparison.Ordinal))
                    return Respuesta.Error("AUTH-001", "Usuario inexistente");
                if (String.IsNullOrWhiteSpace(nuevaClave))
                    return Respuesta.Error("DATA-001", "Indique una nueva clave no vacia");
                clave = nuevaClave;
                sesiones.Clear();
                return Respuesta.Exito("Clave actualizada");
            }
        }

        public Respuesta ObtenerCategorias(string token)
        {
            lock (sincronizacion)
            {
                Respuesta error = ValidarToken(token);
                if (error != null) return error;
                Respuesta respuesta = Respuesta.Exito("Categorias disponibles; conversion habilitada para longitud, masa y temperatura");
                respuesta.Categorias = new List<string>(categorias);
                return respuesta;
            }
        }

        public Respuesta ObtenerUnidades(string token, string categoria)
        {
            lock (sincronizacion)
            {
                Respuesta error = ValidarToken(token);
                if (error != null) return error;
                string c = Normalizar(categoria);
                List<string> unidades;
                if (!catalogo.TryGetValue(c, out unidades))
                    return Respuesta.Error("DATA-001", "Categoria inexistente o vacia");
                Respuesta respuesta = Respuesta.Exito("Unidades disponibles");
                respuesta.Categoria = c;
                respuesta.Unidades = new List<string>(unidades);
                return respuesta;
            }
        }

        public Respuesta Convertir(string token, string categoria, string unidadOrigen, string unidadDestino, double? valor)
        {
            lock (sincronizacion)
            {
                Respuesta error = ValidarToken(token);
                if (error != null) return error;

                string c = Normalizar(categoria);
                string origen = Normalizar(unidadOrigen);
                string destino = Normalizar(unidadDestino);
                if (!valor.HasValue || Double.IsNaN(valor.Value) || Double.IsInfinity(valor.Value) ||
                    !catalogo.ContainsKey(c) || origen.Length == 0 || destino.Length == 0)
                    return Respuesta.Error("DATA-001", "Indique categoria, unidades y un valor numerico finito");
                if (c != "LONGITUD" && c != "MASA" && c != "TEMPERATURA")
                    return Respuesta.Error("DATA-001", "La conversion solo esta habilitada para longitud, masa y temperatura");

                List<string> unidadesCategoria = catalogo[c];
                string[] unidades = { origen, destino };
                foreach (string unidad in unidades)
                {
                    if (!unidadesCategoria.Contains(unidad))
                    {
                        bool otraMagnitud = ContieneUnidad(unidad);
                        return Respuesta.Error(otraMagnitud ? "DATA-001" : "UNIT-001",
                            otraMagnitud
                            ? "Las unidades pertenecen a magnitudes incompatibles"
                            : "Unidad no soportada: " + unidad);
                    }
                }

                double entrada = valor.Value;
                if (!String.Equals(c, "TEMPERATURA", StringComparison.Ordinal) && entrada < 0)
                    return Respuesta.Error("DATA-001", "La masa y la longitud no pueden ser negativas");

                double resultado;
                if (String.Equals(c, "TEMPERATURA", StringComparison.Ordinal))
                {
                    if (entrada < CeroAbsoluto(origen))
                        return Respuesta.Error("DATA-001", "Temperatura inferior al cero absoluto");
                    double celsius = Math.Max(-273.15, ACelsius(origen, entrada));
                    resultado = String.Equals(origen, destino, StringComparison.Ordinal)
                        ? entrada
                        : DesdeCelsius(destino, celsius);
                }
                else
                {
                    Dictionary<string, double> factores = String.Equals(c, "LONGITUD", StringComparison.Ordinal) ? longitud : masa;
                    resultado = entrada * (factores[origen] / factores[destino]);
                }

                if (Double.IsNaN(resultado) || Double.IsInfinity(resultado))
                    return Respuesta.Error("DATA-001", "Resultado fuera del rango numerico");
                Respuesta exito = Respuesta.Exito("Conversion realizada");
                exito.Resultado = resultado;
                exito.Categoria = c;
                exito.UnidadOrigen = origen;
                exito.UnidadDestino = destino;
                return exito;
            }
        }

        private Respuesta ValidarToken(string token)
        {
            DateTimeOffset vencimiento;
            if (token == null || !sesiones.TryGetValue(token, out vencimiento))
                return Respuesta.Error("AUTH-002", "Token ausente o invalido");
            if (reloj().ToUniversalTime() >= vencimiento)
                return Respuesta.Error("AUTH-003", "Sesion expirada; inicie sesion nuevamente");
            return null;
        }

        private bool ContieneUnidad(string unidad)
        {
            foreach (List<string> unidades in catalogo.Values)
                if (unidades.Contains(unidad)) return true;
            return false;
        }

        private static string Normalizar(string texto)
        {
            return texto == null ? String.Empty : texto.Trim().ToUpperInvariant();
        }

        private static double CeroAbsoluto(string unidad)
        {
            switch (unidad)
            {
                case "CELSIUS": return -273.15;
                case "FAHRENHEIT": return -459.67;
                case "KELVIN": return 0.0;
                default: throw new ArgumentException("Escala no soportada");
            }
        }

        private static double ACelsius(string unidad, double valor)
        {
            switch (unidad)
            {
                case "CELSIUS": return valor;
                case "FAHRENHEIT": return (valor - 32.0) * (5.0 / 9.0);
                case "KELVIN": return valor - 273.15;
                default: throw new ArgumentException("Escala no soportada");
            }
        }

        private static double DesdeCelsius(string unidad, double valor)
        {
            switch (unidad)
            {
                case "CELSIUS": return valor;
                case "FAHRENHEIT": return valor * 1.8 + 32.0;
                case "KELVIN": return valor + 273.15;
                default: throw new ArgumentException("Escala no soportada");
            }
        }

        private static bool ClavesIguales(string esperada, string recibida)
        {
            if (recibida == null) return false;
            byte[] a = Encoding.UTF8.GetBytes(esperada);
            byte[] b = Encoding.UTF8.GetBytes(recibida);
            int diferencia = a.Length ^ b.Length;
            int limite = Math.Max(a.Length, b.Length);
            for (int i = 0; i < limite; i++)
            {
                byte x = i < a.Length ? a[i] : (byte)0;
                byte y = i < b.Length ? b[i] : (byte)0;
                diferencia |= x ^ y;
            }
            return diferencia == 0;
        }

        private static string FormatearInstante(DateTimeOffset valor)
        {
            string fecha = valor.UtcDateTime.ToString("yyyy-MM-dd'T'HH:mm:ss.fffffff", CultureInfo.InvariantCulture);
            fecha = fecha.TrimEnd('0').TrimEnd('.');
            return fecha + "Z";
        }

        private static string LeerConfiguracion(string nombre, string predeterminado)
        {
            string valor = ConfigurationManager.AppSettings[nombre];
            return valor == null ? predeterminado : valor;
        }

        private static TimeSpan LeerDuracion()
        {
            string valor = LeerConfiguracion("conversor.sesion.minutos", "15");
            long minutos;
            if (!Int64.TryParse(valor, NumberStyles.Integer, CultureInfo.InvariantCulture, out minutos))
                throw new ConfigurationErrorsException("La duracion de sesion no es un entero valido");
            if (minutos <= 0) throw new ConfigurationErrorsException("La duracion de la sesion debe ser positiva");
            return TimeSpan.FromMinutes(minutos);
        }
    }
}
