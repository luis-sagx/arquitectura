using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    /// <summary>Controla la sesión, el catálogo remoto y las conversiones.</summary>
    public sealed class ControladorConversiones
    {
        /// <summary>Formato es-EC fijo (coma decimal) para no depender de los datos regionales del equipo.</summary>
        private static readonly NumberFormatInfo FormatoLocal = new NumberFormatInfo
        {
            NumberDecimalSeparator = ",",
            NumberGroupSeparator = ".",
            NegativeSign = "-"
        };

        private readonly IVistaConversiones vista;
        private readonly ClienteSoap cliente;
        private readonly Sesion sesion;
        /// <summary>Categoría cuyas unidades se cargaron o se están cargando.</summary>
        private string? categoriaActual;
        /// <summary>Al cerrar la pantalla las respuestas pendientes ya no se entregan.</summary>
        private bool liberado;

        public ControladorConversiones(IVistaConversiones vista, ClienteSoap cliente, Sesion sesion)
        {
            this.vista = vista;
            this.cliente = cliente;
            this.sesion = sesion;
        }

        public Task IniciarAsync()
        {
            return CargarCategoriasAsync();
        }

        private async Task CargarCategoriasAsync()
        {
            vista.EstablecerOcupado(true);
            vista.MostrarEstado("Consultando categorías del servidor…");
            try
            {
                RespuestaSoap respuesta = await cliente.ObtenerCategoriasAsync(sesion.Token);
                if (liberado) return;
                vista.EstablecerOcupado(false);
                if (respuesta.IsSesionInvalida)
                {
                    ExpirarSesion(respuesta.Mensaje);
                }
                else if (!respuesta.IsExito)
                {
                    vista.MostrarEstado("No se pudieron cargar las categorías.");
                    vista.MostrarAviso("Catálogo", respuesta.Mensaje);
                }
                else
                {
                    List<OpcionCatalogo> opciones = Opciones(respuesta.Categorias);
                    categoriaActual = null;
                    vista.EstablecerCategorias(opciones);
                    if (opciones.Count == 0)
                    {
                        vista.MostrarEstado("El servidor no publicó categorías.");
                    }
                    else
                    {
                        vista.MostrarEstado("Catálogo cargado · sesión hasta las " + sesion.HoraExpiracion);
                        await CambiarCategoriaAsync(opciones[0]);
                    }
                }
            }
            catch (IOException ex)
            {
                if (!liberado) ErrorConexion(ex.Message);
            }
        }

        /// <summary>La vista lo llama al elegir categoría; ignora la misma selección repetida.</summary>
        public async Task CambiarCategoriaAsync(OpcionCatalogo? categoria)
        {
            if (categoria == null || !sesion.IsActiva || categoria.Id == categoriaActual) return;

            categoriaActual = categoria.Id;
            vista.LimpiarUnidades();
            vista.LimpiarResultado();
            vista.EstablecerOcupado(true);
            vista.MostrarEstado("Consultando unidades de " + categoria.Etiqueta + "…");
            try
            {
                RespuestaSoap respuesta = await cliente.ObtenerUnidadesAsync(sesion.Token, categoria.Id);
                if (liberado || categoria.Id != categoriaActual) return; // El usuario ya eligió otra categoría.
                vista.EstablecerOcupado(false);
                if (respuesta.IsSesionInvalida)
                {
                    ExpirarSesion(respuesta.Mensaje);
                }
                else if (!respuesta.IsExito)
                {
                    categoriaActual = null;
                    vista.MostrarEstado("No se pudieron cargar las unidades.");
                    vista.MostrarAviso("Unidades", respuesta.Mensaje);
                }
                else
                {
                    List<OpcionCatalogo> opciones = Opciones(respuesta.Unidades);
                    vista.EstablecerUnidades(opciones);
                    vista.MostrarEstado(opciones.Count == 0
                        ? "Esta categoría no tiene unidades disponibles."
                        : "Sesión activa hasta las " + sesion.HoraExpiracion);
                }
            }
            catch (IOException ex)
            {
                if (!liberado && categoria.Id == categoriaActual)
                {
                    categoriaActual = null;
                    ErrorConexion(ex.Message);
                }
            }
        }

        public async Task ConvertirAsync()
        {
            OpcionCatalogo? categoria = vista.CategoriaSeleccionada;
            OpcionCatalogo? origen = vista.OrigenSeleccionado;
            OpcionCatalogo? destino = vista.DestinoSeleccionado;
            if (categoria == null || origen == null || destino == null)
            {
                vista.MostrarAviso("Faltan datos", "Selecciona la categoría y las unidades de origen y destino.");
                return;
            }

            double valor;
            try
            {
                valor = ParsearValor(vista.ValorIngresado);
            }
            catch (ArgumentException ex)
            {
                vista.MostrarAviso("Valor inválido", ex.Message);
                return;
            }

            vista.EstablecerOcupado(true);
            vista.LimpiarResultado();
            vista.MostrarEstado("Convirtiendo en el servidor…");
            try
            {
                RespuestaSoap respuesta = await cliente.ConvertirAsync(sesion.Token, categoria.Id,
                    origen.Id, destino.Id, valor);
                if (liberado) return;
                vista.EstablecerOcupado(false);
                if (respuesta.IsSesionInvalida)
                {
                    ExpirarSesion(respuesta.Mensaje);
                }
                else if (respuesta.IsExito && respuesta.Resultado.HasValue)
                {
                    vista.MostrarResultado(Formatear(respuesta.Resultado.Value) + " " + destino.Etiqueta);
                    vista.MostrarEstado(respuesta.Mensaje);
                }
                else
                {
                    vista.MostrarEstado(respuesta.Codigo + " · " + respuesta.Mensaje);
                    vista.MostrarAviso("No se pudo convertir", respuesta.Mensaje);
                }
            }
            catch (IOException ex)
            {
                if (!liberado) ErrorConexion(ex.Message);
            }
        }

        public void CerrarSesion()
        {
            VolverAlAcceso("La sesión se cerró correctamente.", false);
        }

        public void Liberar()
        {
            liberado = true;
        }

        private void ExpirarSesion(string detalle)
        {
            VolverAlAcceso(String.IsNullOrWhiteSpace(detalle)
                ? "La sesión expiró. Inicia sesión nuevamente." : detalle, true);
        }

        private void VolverAlAcceso(string mensaje, bool error)
        {
            if (liberado) return;
            sesion.LimpiarToken();
            liberado = true;
            vista.VolverAlAcceso(mensaje, error);
        }

        private void ErrorConexion(string mensaje)
        {
            vista.EstablecerOcupado(false);
            vista.MostrarEstado("No se pudo completar la operación.");
            vista.MostrarAviso("Error de comunicación", mensaje);
        }

        private static List<OpcionCatalogo> Opciones(IEnumerable<string> ids)
        {
            return ids.Select(OpcionCatalogo.Desde).ToList();
        }

        internal static string Formatear(double numero)
        {
            double absoluto = Math.Abs(numero);
            if (numero != 0 && (absoluto < 0.0001 || absoluto >= 1e12))
                return numero.ToString("0.0000E+0", CultureInfo.InvariantCulture);
            return numero.ToString("#,##0.####", FormatoLocal);
        }

        internal static double ParsearValor(string? texto)
        {
            if (String.IsNullOrWhiteSpace(texto))
                throw new ArgumentException("Escribe el valor que deseas convertir.");
            string limpio = texto.Trim();
            if (limpio.Contains(',') && limpio.Contains('.'))
                throw new ArgumentException("Usa un solo separador decimal: punto o coma.");
            limpio = limpio.Replace(',', '.');
            double numero;
            if (!Double.TryParse(limpio, NumberStyles.Float, CultureInfo.InvariantCulture, out numero))
                throw new ArgumentException("Escribe un número válido; puedes usar punto o coma decimal.");
            if (Double.IsNaN(numero) || Double.IsInfinity(numero))
                throw new ArgumentException("El valor debe ser un número finito.");
            return numero;
        }
    }
}
