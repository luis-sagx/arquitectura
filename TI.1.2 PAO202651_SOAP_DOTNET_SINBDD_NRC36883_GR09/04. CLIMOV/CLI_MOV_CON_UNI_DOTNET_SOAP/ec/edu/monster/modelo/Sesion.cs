using System;
using System.Globalization;

namespace ec.edu.monster.modelo
{
    /// <summary>Sesión temporal del cliente; el token solo vive en memoria y puede borrarse localmente.</summary>
    public sealed class Sesion
    {
        private string? token;

        public Sesion(string token, string? expiraEn)
        {
            if (String.IsNullOrWhiteSpace(token))
                throw new ArgumentException("El token de sesión es obligatorio.", nameof(token));
            this.token = token;
            ExpiraEn = expiraEn ?? String.Empty;
        }

        public string Token
        {
            get { return token ?? throw new InvalidOperationException("La sesión ya fue cerrada."); }
        }

        public string ExpiraEn { get; }

        public bool IsActiva
        {
            get { return token != null; }
        }

        /// <summary>Hora local de vencimiento; si el servidor envía otro formato se muestra tal cual.</summary>
        public string HoraExpiracion
        {
            get
            {
                DateTimeOffset fecha;
                return DateTimeOffset.TryParse(ExpiraEn, CultureInfo.InvariantCulture,
                        DateTimeStyles.AssumeUniversal, out fecha)
                    ? fecha.ToLocalTime().ToString("HH:mm", CultureInfo.InvariantCulture)
                    : ExpiraEn;
            }
        }

        public void LimpiarToken()
        {
            token = null;
        }
    }
}
