using System;
using System.Globalization;
using System.IO;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    public sealed class ControladorConversiones
    {
        private readonly IClienteRest cliente;
        private readonly ConversionForm vista;
        private readonly Action<string> volverALogin;
        private Sesion sesion;
        private bool cerrado;

        public ControladorConversiones(IClienteRest cliente, Sesion sesion, ConversionForm vista, Action<string> volverALogin)
        {
            this.cliente = cliente ?? throw new ArgumentNullException("cliente");
            this.sesion = sesion ?? throw new ArgumentNullException("sesion");
            this.vista = vista ?? throw new ArgumentNullException("vista");
            this.volverALogin = volverALogin ?? throw new ArgumentNullException("volverALogin");
            vista.CategoriaCambiadaSolicitado += CategoriaCambiada;
            vista.ConvertirSolicitado += Convertir;
            vista.CerrarSesionSolicitado += CerrarSesion;
            vista.FormClosed += (sender, evento) =>
            {
                cerrado = true;
                this.sesion = null;
            };
        }

        public void Iniciar()
        {
            vista.MostrarSesion(sesion);
            CargarCategorias();
        }

        private async void CargarCategorias()
        {
            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Consultando categorías…", false);
            try
            {
                RespuestaRest respuesta = await cliente.ObtenerCategoriasAsync(sesion.Token);
                if (!VentanaActiva() || ManejarAutorizacion(respuesta)) return;
                if (!respuesta.IsExito)
                {
                    vista.MostrarMensaje(respuesta.Mensaje, true);
                    return;
                }

                vista.CargarCategorias(respuesta.Categorias);
                if (respuesta.Categorias == null || respuesta.Categorias.Count == 0)
                {
                    vista.MostrarMensaje("El servicio no devolvió categorías.", true);
                    return;
                }
                await CargarUnidadesAsync();
            }
            catch (IOException error)
            {
                if (VentanaActiva()) vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                if (VentanaActiva()) vista.MostrarMensaje("No se pudieron consultar las categorías.", true);
            }
            finally
            {
                if (!vista.IsDisposed) vista.EstablecerOcupado(false);
            }
        }

        private async void CategoriaCambiada(object sender, EventArgs evento)
        {
            vista.LimpiarUnidades();
            if (vista.CategoriaSeleccionada == null) return;
            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Consultando unidades…", false);
            try { await CargarUnidadesAsync(); }
            catch (IOException error)
            {
                if (VentanaActiva()) vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                if (VentanaActiva()) vista.MostrarMensaje("No se pudieron consultar las unidades.", true);
            }
            finally
            {
                if (!vista.IsDisposed) vista.EstablecerOcupado(false);
            }
        }

        private async Task CargarUnidadesAsync()
        {
            OpcionCatalogo categoria = vista.CategoriaSeleccionada;
            if (categoria == null || sesion == null) return;
            RespuestaRest respuesta = await cliente.ObtenerUnidadesAsync(sesion.Token, categoria.Valor);
            if (!VentanaActiva() || ManejarAutorizacion(respuesta)) return;
            if (!respuesta.IsExito)
            {
                vista.LimpiarUnidades();
                vista.MostrarMensaje(respuesta.Mensaje, true);
                return;
            }

            vista.CargarUnidades(respuesta.Unidades);
            vista.MostrarMensaje(respuesta.Unidades == null || respuesta.Unidades.Count == 0
                ? "La categoría no contiene unidades." : "Selecciona las unidades e ingresa el valor.",
                respuesta.Unidades == null || respuesta.Unidades.Count == 0);
        }

        private async void Convertir(object sender, EventArgs evento)
        {
            OpcionCatalogo categoria = vista.CategoriaSeleccionada;
            OpcionCatalogo unidadOrigen = vista.UnidadOrigenSeleccionada;
            OpcionCatalogo unidadDestino = vista.UnidadDestinoSeleccionada;
            double valor;
            if (categoria == null || unidadOrigen == null || unidadDestino == null)
            {
                vista.MostrarMensaje("Selecciona la categoría y ambas unidades.", true);
                return;
            }
            if (!Double.TryParse(vista.ValorIngresado, NumberStyles.Float, CultureInfo.CurrentCulture, out valor) ||
                Double.IsNaN(valor) || Double.IsInfinity(valor))
            {
                vista.LimpiarResultado();
                vista.MostrarMensaje("Escribe un valor numérico finito con el separador decimal de tu región.", true);
                return;
            }

            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Convirtiendo…", false);
            try
            {
                RespuestaRest respuesta = await cliente.ConvertirAsync(sesion.Token, categoria.Valor,
                    unidadOrigen.Valor, unidadDestino.Valor, valor);
                if (!VentanaActiva() || ManejarAutorizacion(respuesta)) return;
                if (!respuesta.IsExito || !respuesta.Resultado.HasValue)
                {
                    vista.LimpiarResultado();
                    vista.MostrarMensaje(respuesta.Mensaje, true);
                    return;
                }
                vista.MostrarResultado(valor, respuesta);
            }
            catch (IOException error)
            {
                if (!VentanaActiva()) return;
                vista.LimpiarResultado();
                vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                if (!VentanaActiva()) return;
                vista.LimpiarResultado();
                vista.MostrarMensaje("No se pudo completar la conversión.", true);
            }
            finally
            {
                if (!vista.IsDisposed) vista.EstablecerOcupado(false);
            }
        }

        private bool ManejarAutorizacion(RespuestaRest respuesta)
        {
            if (!VentanaActiva()) return true;
            if (respuesta == null || (respuesta.Codigo != "AUTH-002" && respuesta.Codigo != "AUTH-003")) return false;
            string aviso = respuesta.Mensaje;
            sesion = null;
            volverALogin(aviso);
            return true;
        }

        private void CerrarSesion(object sender, EventArgs evento)
        {
            sesion = null;
            volverALogin(null);
        }

        private bool VentanaActiva()
        {
            return !cerrado && !vista.IsDisposed && sesion != null;
        }
    }
}
