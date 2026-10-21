using System.Globalization;
using System.Text;
using ec.edu.monster.modelos;

namespace ec.edu.monster.servicios;

/// <summary>Lógica de negocio del conversor, independiente del transporte y sin base de datos.</summary>
public sealed class ConversorUnidadesServices
{
    private readonly object sincronizacion = new();
    private readonly string usuario;
    private string clave;
    private readonly Func<DateTimeOffset> reloj;
    private readonly TimeSpan duracion;
    private readonly Dictionary<string, DateTimeOffset> sesiones = new(StringComparer.Ordinal);
    private readonly List<string> categorias = ["LONGITUD", "MASA", "TEMPERATURA"];
    private readonly Dictionary<string, List<string>> catalogo = new(StringComparer.Ordinal);
    private readonly Dictionary<string, double> longitud = new(StringComparer.Ordinal);
    private readonly Dictionary<string, double> masa = new(StringComparer.Ordinal);

    public ConversorUnidadesServices() : this("monster", "monster123", () => DateTimeOffset.UtcNow, TimeSpan.FromMinutes(15))
    {
    }

    public ConversorUnidadesServices(string usuario, string clave, TimeSpan duracion)
        : this(usuario, clave, () => DateTimeOffset.UtcNow, duracion)
    {
    }

    public ConversorUnidadesServices(string usuario, string clave, Func<DateTimeOffset> reloj, TimeSpan duracion)
    {
        if (string.IsNullOrWhiteSpace(usuario) || string.IsNullOrWhiteSpace(clave))
            throw new ArgumentException("Las credenciales deben estar configuradas");
        ArgumentNullException.ThrowIfNull(reloj);
        if (duracion <= TimeSpan.Zero)
            throw new ArgumentException("La duracion de la sesion debe ser positiva", nameof(duracion));

        this.usuario = usuario;
        this.clave = clave;
        this.reloj = reloj;
        this.duracion = duracion;

        catalogo.Add("LONGITUD", ["KILOMETRO", "METRO", "CENTIMETRO", "MILIMETRO", "MILLA", "PIE"]);
        catalogo.Add("MASA", ["KILOGRAMO", "GRAMO", "MILIGRAMO", "LIBRA"]);
        catalogo.Add("TEMPERATURA", ["CELSIUS", "FAHRENHEIT", "KELVIN"]);

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

    public Respuesta Autenticar(string? usuario, string? clave)
    {
        lock (sincronizacion)
        {
            if (!string.Equals(this.usuario, usuario, StringComparison.Ordinal) || !ClavesIguales(this.clave, clave))
                return Respuesta.Error("AUTH-001", "Credenciales invalidas");

            DateTimeOffset ahora = reloj().ToUniversalTime();
            DateTimeOffset umbral = ahora.AddDays(-1);
            foreach (string tokenVencido in sesiones.Where(sesion => sesion.Value < umbral).Select(sesion => sesion.Key).ToArray())
                sesiones.Remove(tokenVencido);

            DateTimeOffset vencimiento = ahora.Add(duracion);
            string token = Guid.NewGuid().ToString("D").ToLowerInvariant();
            sesiones[token] = vencimiento;
            Respuesta respuesta = Respuesta.Exito("Sesion iniciada");
            respuesta.Token = token;
            respuesta.ExpiraEn = FormatearInstante(vencimiento);
            return respuesta;
        }
    }

    public Respuesta RecuperarClave(string? usuario, string? nuevaClave)
    {
        lock (sincronizacion)
        {
            if (usuario is null || !string.Equals(this.usuario, usuario, StringComparison.Ordinal))
                return Respuesta.Error("AUTH-001", "Usuario inexistente");
            if (string.IsNullOrWhiteSpace(nuevaClave))
                return Respuesta.Error("DATA-001", "Indique una nueva clave no vacia");
            clave = nuevaClave;
            sesiones.Clear();
            return Respuesta.Exito("Clave actualizada");
        }
    }

    public Respuesta ObtenerCategorias(string? token)
    {
        lock (sincronizacion)
        {
            Respuesta? error = ValidarToken(token);
            if (error is not null) return error;
            Respuesta respuesta = Respuesta.Exito("Categorias disponibles; conversion habilitada para longitud, masa y temperatura");
            respuesta.Categorias = [.. categorias];
            return respuesta;
        }
    }

    public Respuesta ObtenerUnidades(string? token, string? categoria)
    {
        lock (sincronizacion)
        {
            Respuesta? error = ValidarToken(token);
            if (error is not null) return error;
            string c = Normalizar(categoria);
            if (!catalogo.TryGetValue(c, out List<string>? unidades))
                return Respuesta.Error("DATA-001", "Categoria inexistente o vacia");
            Respuesta respuesta = Respuesta.Exito("Unidades disponibles");
            respuesta.Categoria = c;
            respuesta.Unidades = [.. unidades];
            return respuesta;
        }
    }

    public Respuesta Convertir(string? token, string? categoria, string? unidadOrigen, string? unidadDestino, double? valor)
    {
        lock (sincronizacion)
        {
            Respuesta? error = ValidarToken(token);
            if (error is not null) return error;

            string c = Normalizar(categoria);
            string origen = Normalizar(unidadOrigen);
            string destino = Normalizar(unidadDestino);
            if (!valor.HasValue || double.IsNaN(valor.Value) || double.IsInfinity(valor.Value) ||
                !catalogo.ContainsKey(c) || origen.Length == 0 || destino.Length == 0)
                return Respuesta.Error("DATA-001", "Indique categoria, unidades y un valor numerico finito");

            List<string> unidadesCategoria = catalogo[c];
            foreach (string unidad in new[] { origen, destino })
            {
                if (unidadesCategoria.Contains(unidad)) continue;
                bool otraMagnitud = ContieneUnidad(unidad);
                return Respuesta.Error(otraMagnitud ? "DATA-001" : "UNIT-001",
                    otraMagnitud ? "Las unidades pertenecen a magnitudes incompatibles" : "Unidad no soportada: " + unidad);
            }

            double entrada = valor.Value;
            if (!string.Equals(c, "TEMPERATURA", StringComparison.Ordinal) && entrada < 0)
                return Respuesta.Error("DATA-001", "La masa y la longitud no pueden ser negativas");

            double resultado;
            if (string.Equals(c, "TEMPERATURA", StringComparison.Ordinal))
            {
                if (entrada < CeroAbsoluto(origen))
                    return Respuesta.Error("DATA-001", "Temperatura inferior al cero absoluto");
                double celsius = Math.Max(-273.15, ACelsius(origen, entrada));
                resultado = string.Equals(origen, destino, StringComparison.Ordinal) ? entrada : DesdeCelsius(destino, celsius);
            }
            else
            {
                Dictionary<string, double> factores = string.Equals(c, "LONGITUD", StringComparison.Ordinal) ? longitud : masa;
                resultado = entrada * (factores[origen] / factores[destino]);
            }

            if (double.IsNaN(resultado) || double.IsInfinity(resultado))
                return Respuesta.Error("DATA-001", "Resultado fuera del rango numerico");
            Respuesta exito = Respuesta.Exito("Conversion realizada");
            exito.Resultado = resultado;
            exito.Categoria = c;
            exito.UnidadOrigen = origen;
            exito.UnidadDestino = destino;
            return exito;
        }
    }

    private Respuesta? ValidarToken(string? token)
    {
        if (token is null || !sesiones.TryGetValue(token, out DateTimeOffset vencimiento))
            return Respuesta.Error("AUTH-002", "Token ausente o invalido");
        if (reloj().ToUniversalTime() >= vencimiento)
            return Respuesta.Error("AUTH-003", "Sesion expirada; inicie sesion nuevamente");
        return null;
    }

    private bool ContieneUnidad(string unidad) => catalogo.Values.Any(unidades => unidades.Contains(unidad));

    private static string Normalizar(string? texto) => texto?.Trim().ToUpperInvariant() ?? string.Empty;

    private static double CeroAbsoluto(string unidad) => unidad switch
    {
        "CELSIUS" => -273.15,
        "FAHRENHEIT" => -459.67,
        "KELVIN" => 0.0,
        _ => throw new ArgumentException("Escala no soportada")
    };

    private static double ACelsius(string unidad, double valor) => unidad switch
    {
        "CELSIUS" => valor,
        "FAHRENHEIT" => (valor - 32.0) * (5.0 / 9.0),
        "KELVIN" => valor - 273.15,
        _ => throw new ArgumentException("Escala no soportada")
    };

    private static double DesdeCelsius(string unidad, double valor) => unidad switch
    {
        "CELSIUS" => valor,
        "FAHRENHEIT" => valor * 1.8 + 32.0,
        "KELVIN" => valor + 273.15,
        _ => throw new ArgumentException("Escala no soportada")
    };

    private static bool ClavesIguales(string esperada, string? recibida)
    {
        if (recibida is null) return false;
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
        return fecha.TrimEnd('0').TrimEnd('.') + "Z";
    }
}
