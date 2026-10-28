using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>Catálogo remoto y conversiones; requiere sesión.</summary>
    [FiltroSesion]
    public sealed class ConversorController : Controller
    {
        /// <summary>Formato es-EC fijo (coma decimal), igual que en los clientes móviles.</summary>
        private static readonly NumberFormatInfo FormatoLocal = new NumberFormatInfo
        {
            NumberDecimalSeparator = ",",
            NumberGroupSeparator = ".",
            NegativeSign = "-"
        };

        private readonly ClienteSoap cliente;

        public ConversorController(ClienteSoap cliente)
        {
            this.cliente = cliente;
        }

        [HttpGet("/conversor")]
        public async Task<IActionResult> Index(string? categoria)
        {
            var modelo = new ConversorModelo { Categoria = categoria };
            return await MostrarAsync(modelo);
        }

        [HttpPost("/conversor")]
        public async Task<IActionResult> Index(string? categoria, string? origen, string? destino, string? valor)
        {
            var modelo = new ConversorModelo
            {
                Categoria = categoria,
                Origen = origen,
                Destino = destino,
                Valor = valor ?? ""
            };

            double numero;
            if (String.IsNullOrWhiteSpace(categoria) || String.IsNullOrWhiteSpace(origen)
                || String.IsNullOrWhiteSpace(destino))
            {
                modelo.Error = "Selecciona la categoría y las unidades de origen y destino.";
            }
            else if (!TryParsearValor(valor, out numero, out string? errorValor))
            {
                modelo.Error = errorValor;
            }
            else
            {
                try
                {
                    RespuestaSoap respuesta = await cliente.ConvertirAsync(Token(), categoria, origen, destino, numero);
                    if (respuesta.IsSesionInvalida) return Expirar(respuesta.Mensaje);
                    if (respuesta.IsExito && respuesta.Resultado.HasValue)
                    {
                        modelo.Resultado = Formatear(respuesta.Resultado.Value) + " "
                            + OpcionCatalogo.Desde(destino).Etiqueta;
                        modelo.DetalleResultado = Formatear(numero) + " " + OpcionCatalogo.Desde(origen).Etiqueta
                            + " → " + OpcionCatalogo.Desde(destino).Etiqueta;
                    }
                    else
                    {
                        modelo.Error = respuesta.Codigo + " · " + respuesta.Mensaje;
                    }
                }
                catch (IOException ex)
                {
                    modelo.Error = ex.Message;
                }
            }
            return await MostrarAsync(modelo);
        }

        /// <summary>Carga categorías y unidades del servidor y pinta la página.</summary>
        private async Task<IActionResult> MostrarAsync(ConversorModelo modelo)
        {
            modelo.HoraExpiracion = SesionWeb.HoraExpiracion(HttpContext.Session);
            try
            {
                RespuestaSoap categorias = await cliente.ObtenerCategoriasAsync(Token());
                if (categorias.IsSesionInvalida) return Expirar(categorias.Mensaje);
                if (!categorias.IsExito)
                {
                    modelo.Error ??= categorias.Mensaje;
                    return View("Index", modelo);
                }
                foreach (string categoriaCatalogo in categorias.Categorias)
                {
                    RespuestaSoap unidadesCatalogo = await cliente.ObtenerUnidadesAsync(Token(), categoriaCatalogo);
                    if (unidadesCatalogo.IsSesionInvalida) return Expirar(unidadesCatalogo.Mensaje);
                    if (unidadesCatalogo.IsExito) modelo.Catalogo[categoriaCatalogo] = Opciones(unidadesCatalogo.Unidades);
                }
                modelo.Categorias = Opciones(categorias.Categorias)
                    .Where(c => c.Id == "LONGITUD" || c.Id == "MASA" || c.Id == "TEMPERATURA").ToList();
                if (String.IsNullOrWhiteSpace(modelo.Categoria) || !modelo.Categorias.Any(c => c.Id == modelo.Categoria))
                    modelo.Categoria = modelo.Categorias.FirstOrDefault()?.Id;

                if (modelo.Categoria != null)
                {
                    RespuestaSoap unidades = await cliente.ObtenerUnidadesAsync(Token(), modelo.Categoria);
                    if (unidades.IsSesionInvalida) return Expirar(unidades.Mensaje);
                    if (unidades.IsExito)
                    {
                        modelo.Unidades = Opciones(unidades.Unidades);
                        modelo.Origen ??= modelo.Unidades.ElementAtOrDefault(0)?.Id;
                        modelo.Destino ??= modelo.Unidades.ElementAtOrDefault(1)?.Id ?? modelo.Origen;
                    }
                    else
                    {
                        modelo.Error ??= unidades.Mensaje;
                    }
                }
            }
            catch (IOException ex)
            {
                modelo.Error ??= ex.Message;
            }
            return View("Index", modelo);
        }

        private IActionResult Expirar(string mensaje)
        {
            SesionWeb.Cerrar(HttpContext.Session);
            TempData["aviso"] = String.IsNullOrWhiteSpace(mensaje)
                ? "La sesión expiró. Inicia sesión nuevamente." : mensaje;
            return RedirectToAction("Login", "Acceso");
        }

        private string Token()
        {
            return SesionWeb.Token(HttpContext.Session) ?? "";
        }

        private static List<OpcionCatalogo> Opciones(IEnumerable<string> ids)
        {
            return ids.Select(OpcionCatalogo.Desde).ToList();
        }

        internal static string Formatear(double numero)
        {
            double redondeado = Math.Round(numero, 4, MidpointRounding.AwayFromZero);
            if (redondeado != 0 && (Math.Abs(redondeado) < 0.0001 || Math.Abs(redondeado) >= 1e12))
                return redondeado.ToString("0.0000E+0", CultureInfo.InvariantCulture);
            return redondeado.ToString("#,##0.####", FormatoLocal);
        }

        /// <summary>Acepta punto o coma decimal y valores negativos, igual que los clientes móviles.</summary>
        internal static bool TryParsearValor(string? texto, out double numero, out string? error)
        {
            numero = 0;
            error = null;
            if (String.IsNullOrWhiteSpace(texto))
            {
                error = "Escribe el valor que deseas convertir.";
                return false;
            }
            string limpio = texto.Trim();
            if (limpio.Contains(',') && limpio.Contains('.'))
            {
                error = "Usa un solo separador decimal: punto o coma.";
                return false;
            }
            if (!Double.TryParse(limpio.Replace(',', '.'), NumberStyles.Float, CultureInfo.InvariantCulture, out numero)
                || Double.IsNaN(numero) || Double.IsInfinity(numero))
            {
                error = "Escribe un número válido; puedes usar punto o coma decimal.";
                return false;
            }
            return true;
        }
    }
}
