using System;
using System.IO;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>Inicio de sesión, recuperación de clave y cierre de sesión.</summary>
    public sealed class AccesoController : Controller
    {
        private readonly ClienteSoap cliente;

        public AccesoController(ClienteSoap cliente)
        {
            this.cliente = cliente;
        }

        [HttpGet("/login")]
        public IActionResult Login()
        {
            return View(new LoginModelo { Aviso = TempData["aviso"] as string });
        }

        [HttpPost("/login")]
        public async Task<IActionResult> Login(string? usuario, string? clave)
        {
            var modelo = new LoginModelo { Usuario = usuario?.Trim() ?? "" };
            if (String.IsNullOrWhiteSpace(usuario) || String.IsNullOrEmpty(clave))
            {
                modelo.Error = "Ingresa usuario y clave.";
                return View(modelo);
            }
            try
            {
                RespuestaSoap respuesta = await cliente.AutenticarAsync(modelo.Usuario, clave);
                if (respuesta.IsExito && !String.IsNullOrWhiteSpace(respuesta.Token))
                {
                    // Sesión nueva al autenticarse (evita reutilizar una sesión anterior).
                    SesionWeb.Cerrar(HttpContext.Session);
                    SesionWeb.Iniciar(HttpContext.Session, respuesta.Token, respuesta.ExpiraEn);
                    return RedirectToAction("Index", "Conversor");
                }
                modelo.Error = respuesta.Codigo + " · " + respuesta.Mensaje;
            }
            catch (IOException ex)
            {
                modelo.Error = ex.Message;
            }
            return View(modelo);
        }

        [HttpGet("/recuperar-clave")]
        public IActionResult Recuperar()
        {
            return View(new RecuperarModelo());
        }

        [HttpPost("/recuperar-clave")]
        public async Task<IActionResult> Recuperar(string? usuario, string? clave, string? confirmar)
        {
            var modelo = new RecuperarModelo { Usuario = usuario?.Trim() ?? "" };
            if (String.IsNullOrWhiteSpace(usuario) || String.IsNullOrEmpty(clave))
            {
                modelo.Error = "Completa todos los campos.";
            }
            else if (clave != confirmar)
            {
                modelo.Error = "Las claves no coinciden.";
            }
            else
            {
                try
                {
                    RespuestaSoap respuesta = await cliente.RecuperarClaveAsync(modelo.Usuario, clave);
                    if (respuesta.IsExito) modelo.Exito = respuesta.Mensaje;
                    else modelo.Error = respuesta.Codigo + " · " + respuesta.Mensaje;
                }
                catch (IOException ex)
                {
                    modelo.Error = ex.Message;
                }
            }
            return View(modelo);
        }

        [HttpPost("/salir")]
        public IActionResult Salir()
        {
            SesionWeb.Cerrar(HttpContext.Session);
            TempData["aviso"] = "La sesión se cerró correctamente.";
            return RedirectToAction("Login");
        }
    }
}
