using System;
using System.Globalization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;

namespace ec.edu.monster.controlador
{
    /// <summary>Lectura y escritura del token del servicio en la sesión HTTP.</summary>
    internal static class SesionWeb
    {
        private const string ClaveToken = "token";
        private const string ClaveExpira = "expiraEn";

        public static string? Token(ISession sesion)
        {
            return sesion.GetString(ClaveToken);
        }

        public static void Iniciar(ISession sesion, string token, string expiraEn)
        {
            sesion.SetString(ClaveToken, token);
            sesion.SetString(ClaveExpira, expiraEn ?? "");
        }

        public static void Cerrar(ISession sesion)
        {
            sesion.Clear();
        }

        /// <summary>Hora local de vencimiento; si el servidor envía otro formato se muestra tal cual.</summary>
        public static string HoraExpiracion(ISession sesion)
        {
            string expira = sesion.GetString(ClaveExpira) ?? "";
            DateTimeOffset fecha;
            return DateTimeOffset.TryParse(expira, CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal, out fecha)
                ? fecha.ToLocalTime().ToString("HH:mm", CultureInfo.InvariantCulture)
                : expira;
        }
    }

    /// <summary>Equivale al FiltroSesion del cliente web Java: sin token se vuelve al acceso.</summary>
    public sealed class FiltroSesionAttribute : ActionFilterAttribute
    {
        public override void OnActionExecuting(ActionExecutingContext contexto)
        {
            if (String.IsNullOrEmpty(SesionWeb.Token(contexto.HttpContext.Session)))
                contexto.Result = new RedirectToActionResult("Login", "Acceso", null);
        }
    }
}
