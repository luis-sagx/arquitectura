using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using ec.edu.monster.cliente.ws;
using ec.edu.monster.consola.vista;
using ec.edu.monster.modelo;

namespace ec.edu.monster.consola.controlador
{
    /// <summary>Coordina el menú, la sesión local y las operaciones SOAP.</summary>
    internal sealed class ControladorConsola
    {
        private static readonly string[] CategoriasConvertibles = { "LONGITUD", "MASA", "TEMPERATURA" };
        private readonly ClienteSoap cliente;
        private readonly VistaConsola vista;
        private string token;

        public ControladorConsola(ClienteSoap cliente, VistaConsola vista)
        {
            this.cliente = cliente;
            this.vista = vista;
        }

        public void Ejecutar()
        {
            try
            {
                while (true)
                {
                    vista.MostrarMenuPrincipal(token != null);
                    string opcion = vista.Leer("Opción: ");
                    bool esperar = true;
                    try
                    {
                        switch (opcion)
                        {
                            case "1": IniciarSesion(); break;
                            case "2": if (HaySesion()) esperar = ConsultarCatalogo(); break;
                            case "3": if (HaySesion()) esperar = Convertir(); break;
                            case "4": token = null; vista.MostrarMensaje("Sesión local cerrada."); break;
                            case "5": RecuperarClave(); break;
                            case "0": vista.MostrarMensaje("Hasta luego."); return;
                            default: vista.MostrarMensaje("Opción inválida. Escriba una opción del menú, del 0 al 5."); break;
                        }
                    }
                    catch (IOException ex)
                    {
                        vista.MostrarError(ex.Message);
                    }
                    if (esperar) vista.EsperarParaContinuar();
                }
            }
            catch (VistaConsola.FinEntradaException)
            {
                vista.MostrarMensaje("\nHasta luego.");
            }
        }

        private void IniciarSesion()
        {
            vista.MostrarSeccion("INICIAR SESIÓN");
            token = null;
            string usuario = vista.LeerLiteral("Usuario: ");
            string clave = vista.LeerClave("Clave: ");
            RespuestaSoap respuesta = cliente.Autenticar(usuario, clave);
            if (!Mostrar(respuesta)) return;
            if (string.IsNullOrWhiteSpace(respuesta.Token))
                throw new IOException("El servidor no entregó un token de sesión.");

            token = respuesta.Token;
            vista.MostrarMensaje("Sesión iniciada.");
            if (!string.IsNullOrWhiteSpace(respuesta.ExpiraEn))
                vista.MostrarMensaje("Sesión vigente hasta: " + respuesta.ExpiraEn);
        }

        private void RecuperarClave()
        {
            vista.MostrarSeccion("RECUPERAR CONTRASEÑA");
            string usuario = vista.LeerLiteral("Usuario: ");
            string nuevaClave = vista.LeerClave("Nueva clave: ");
            if (Mostrar(cliente.RecuperarClave(usuario, nuevaClave))) token = null;
        }

        private bool HaySesion()
        {
            if (token != null) return true;
            vista.MostrarMensaje("Inicie sesión con la opción 1 para continuar.");
            return false;
        }

        private bool ConsultarCatalogo()
        {
            vista.MostrarSeccion("CATÁLOGO DE UNIDADES");
            RespuestaSoap categorias = cliente.ObtenerCategorias(token);
            if (!Mostrar(categorias)) return true;
            foreach (string categoriaCatalogo in categorias.Categorias)
            {
                RespuestaSoap unidades = cliente.ObtenerUnidades(token, categoriaCatalogo);
                if (!Mostrar(unidades)) return true;
                vista.MostrarCategoriaCatalogo(categoriaCatalogo, unidades.Unidades, CategoriasConvertibles.Contains(categoriaCatalogo));
            }
            return true;
        }

        private bool Convertir()
        {
            vista.MostrarPasoConversion(1);
            RespuestaSoap categorias = cliente.ObtenerCategorias(token);
            if (!Mostrar(categorias)) return true;
            IList<string> convertibles = categorias.Categorias
                .Where(c => CategoriasConvertibles.Contains(c, StringComparer.Ordinal))
                .ToList();
            string categoria = vista.Seleccionar("Categoría a convertir", convertibles);
            if (categoria == null) return false;

            vista.MostrarPasoConversion(2, categoria);
            RespuestaSoap unidades = cliente.ObtenerUnidades(token, categoria);
            if (!Mostrar(unidades)) return true;
            string origen = vista.Seleccionar("Unidad de origen", unidades.Unidades);
            if (origen == null) return false;
            vista.MostrarPasoConversion(3, categoria, origen);
            string destino = vista.Seleccionar("Unidad de destino", unidades.Unidades);
            if (destino == null) return false;

            vista.MostrarPasoConversion(4, categoria, origen, destino);
            double valor = vista.LeerNumero();
            RespuestaSoap respuesta = cliente.Convertir(token, categoria, origen, destino, valor);
            if (!Mostrar(respuesta)) return true;
            if (!respuesta.Resultado.HasValue)
                throw new IOException("El servidor no entregó el resultado de conversión.");
            vista.MostrarSeccion("RESULTADO DE CONVERSIÓN | " + categoria);
            vista.MostrarConversion(valor, origen, respuesta.Resultado.Value,
                string.IsNullOrWhiteSpace(respuesta.UnidadDestino) ? destino : respuesta.UnidadDestino);
            return true;
        }

        private bool Mostrar(RespuestaSoap respuesta)
        {
            vista.MostrarRespuesta(respuesta);
            if (respuesta.EsSesionInvalida)
            {
                token = null;
                vista.MostrarMensaje("La sesión ya no es válida. Inicie sesión nuevamente.");
            }
            return respuesta.EsExito;
        }
    }
}
