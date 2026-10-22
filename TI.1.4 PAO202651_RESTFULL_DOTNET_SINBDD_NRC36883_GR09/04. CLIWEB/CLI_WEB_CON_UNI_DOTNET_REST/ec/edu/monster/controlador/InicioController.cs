using System;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>Página raíz: envía al acceso o al conversor según haya sesión.</summary>
    public sealed class InicioController : Controller
    {
        [HttpGet("/")]
        public IActionResult Index()
        {
            return String.IsNullOrEmpty(SesionWeb.Token(HttpContext.Session))
                ? RedirectToAction("Login", "Acceso")
                : RedirectToAction("Index", "Conversor");
        }

        [HttpGet("/error")]
        public IActionResult Error()
        {
            return View();
        }
    }
}
