using System.Collections.Generic;
using System.Runtime.Serialization;

namespace ec.edu.monster.modelo
{
    [DataContract]
    public sealed class RespuestaRest
    {
        [DataMember(Name = "estado", IsRequired = true)]
        public string Estado { get; set; }

        [DataMember(Name = "codigo", IsRequired = true)]
        public string Codigo { get; set; }

        [DataMember(Name = "mensaje", IsRequired = true)]
        public string Mensaje { get; set; }

        [DataMember(Name = "token", EmitDefaultValue = false)]
        public string Token { get; set; }

        [DataMember(Name = "expiraEn", EmitDefaultValue = false)]
        public string ExpiraEn { get; set; }

        [DataMember(Name = "resultado", EmitDefaultValue = false)]
        public double? Resultado { get; set; }

        [DataMember(Name = "categoria", EmitDefaultValue = false)]
        public string Categoria { get; set; }

        [DataMember(Name = "unidadOrigen", EmitDefaultValue = false)]
        public string UnidadOrigen { get; set; }

        [DataMember(Name = "unidadDestino", EmitDefaultValue = false)]
        public string UnidadDestino { get; set; }

        [DataMember(Name = "categorias", EmitDefaultValue = false)]
        public List<string> Categorias { get; set; }

        [DataMember(Name = "unidades", EmitDefaultValue = false)]
        public List<string> Unidades { get; set; }

        public bool IsExito { get { return Estado == "EXITO" && Codigo == "OK"; } }

        public RespuestaRest()
        {
            Categorias = new List<string>();
            Unidades = new List<string>();
        }
    }
}
