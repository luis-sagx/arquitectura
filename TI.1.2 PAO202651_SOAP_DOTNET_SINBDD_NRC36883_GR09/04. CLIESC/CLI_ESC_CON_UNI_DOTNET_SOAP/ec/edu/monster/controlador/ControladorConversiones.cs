using System;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    public sealed class ControladorConversiones
    {
        private readonly IClienteSoap cliente;
        private readonly ConversionForm vista;
        private readonly Action<string> volverALogin;
        private Sesion sesion;
        private bool cerrado;

        public ControladorConversiones(IClienteSoap cliente, Sesion sesion,
            ConversionForm vista, Action<string> volverALogin)
        {
            this.cliente = cliente;
            this.sesion = sesion;
            this.vista = vista;
            this.volverALogin = volverALogin;
            vista.CategoriaCambiadaSolicitado += CategoriaCambiada;
            vista.ConvertirSolicitado += Convertir;
            vista.CatalogoSolicitado += ConsultarCatalogo;
            vista.CerrarSesionSolicitado += CerrarSesion;
            vista.FormClosed += (sender, evento) =>
            {
                cerrado = true;
                sesion = null;
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
                RespuestaSoap respuesta = await Task.Run(() => cliente.ObtenerCategorias(sesion.Token));
                if (!VentanaActiva()) return;
                if (ManejarAutorizacion(respuesta)) return;
                if (!respuesta.IsExito)
                {
                    vista.MostrarMensaje(respuesta.Mensaje, true);
                    return;
                }

                var convertibles = respuesta.Categorias.Where(c => c == "LONGITUD" || c == "MASA" || c == "TEMPERATURA").ToList();
                vista.CargarCategorias(convertibles);
                if (convertibles.Count == 0)
                {
                    vista.MostrarMensaje("El servicio no devolvió categorías.", true);
                    return;
                }
                await CargarUnidadesAsync();
            }
            catch (IOException error)
            {
                if (!VentanaActiva()) return;
                vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                if (!VentanaActiva()) return;
                vista.MostrarMensaje("No se pudieron consultar las categorías.", true);
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
            try
            {
                await CargarUnidadesAsync();
            }
            catch (IOException error)
            {
                if (!VentanaActiva()) return;
                vista.MostrarMensaje(error.Message, true);
            }
            catch (Exception)
            {
                if (!VentanaActiva()) return;
                vista.MostrarMensaje("No se pudieron consultar las unidades.", true);
            }
            finally
            {
                if (!vista.IsDisposed) vista.EstablecerOcupado(false);
            }
        }

        private async Task CargarUnidadesAsync()
        {
            OpcionCatalogo categoria = vista.CategoriaSeleccionada;
            if (categoria == null) return;
            RespuestaSoap respuesta = await Task.Run(() => cliente.ObtenerUnidades(sesion.Token, categoria.Valor));
            if (!VentanaActiva()) return;
            if (ManejarAutorizacion(respuesta)) return;
            if (!respuesta.IsExito)
            {
                vista.LimpiarUnidades();
                vista.MostrarMensaje(respuesta.Mensaje, true);
                return;
            }

            vista.CargarUnidades(respuesta.Unidades);
            vista.MostrarMensaje(respuesta.Unidades.Count == 0
                ? "La categoría no contiene unidades." : "Selecciona las unidades e ingresa el valor.",
                respuesta.Unidades.Count == 0);
        }

        private async void ConsultarCatalogo(object sender, EventArgs evento)
        {
            try
            {
                RespuestaSoap respuesta = await Task.Run(() => cliente.ObtenerCategorias(sesion.Token));
                if (!VentanaActiva() || ManejarAutorizacion(respuesta) || !respuesta.IsExito) return;
                var texto = new StringBuilder();
                foreach (string categoria in respuesta.Categorias)
                {
                    RespuestaSoap unidades = await Task.Run(() => cliente.ObtenerUnidades(sesion.Token, categoria));
                    if (ManejarAutorizacion(unidades) || !unidades.IsExito) return;
                    texto.AppendLine(OpcionCatalogo.Desde(categoria).Texto);
                    foreach (string unidad in unidades.Unidades) texto.AppendLine("  • " + OpcionCatalogo.Desde(unidad).Texto);
                    texto.AppendLine();
                }
                texto.Append("Conversiones habilitadas: longitud, masa/peso y temperatura.");
                vista.MostrarCatalogo(texto.ToString());
            }
            catch (IOException error) { if (VentanaActiva()) vista.MostrarMensaje(error.Message, true); }
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
            if (!Double.TryParse(vista.ValorIngresado, NumberStyles.Float,
                CultureInfo.CurrentCulture, out valor) || Double.IsNaN(valor) || Double.IsInfinity(valor))
            {
                vista.LimpiarResultado();
                vista.MostrarMensaje("Escribe un valor numérico finito con el separador decimal de tu región.", true);
                return;
            }

            vista.EstablecerOcupado(true);
            vista.MostrarMensaje("Convirtiendo…", false);
            try
            {
                RespuestaSoap respuesta = await Task.Run(() => cliente.Convertir(sesion.Token,
                    categoria.Valor, unidadOrigen.Valor, unidadDestino.Valor, valor));
                if (!VentanaActiva()) return;
                if (ManejarAutorizacion(respuesta)) return;
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

        private bool ManejarAutorizacion(RespuestaSoap respuesta)
        {
            if (!VentanaActiva()) return true;
            if (respuesta == null || (respuesta.Codigo != "AUTH-002" && respuesta.Codigo != "AUTH-003"))
                return false;

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
            return !cerrado && !vista.IsDisposed;
        }
    }
}
