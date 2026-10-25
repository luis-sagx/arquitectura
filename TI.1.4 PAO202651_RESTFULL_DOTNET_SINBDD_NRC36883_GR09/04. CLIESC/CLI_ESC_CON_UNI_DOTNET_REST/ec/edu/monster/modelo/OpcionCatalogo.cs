namespace ec.edu.monster.modelo
{
    public sealed class OpcionCatalogo
    {
        public OpcionCatalogo(string valor, string texto)
        {
            Valor = valor;
            Texto = texto;
        }

        public string Valor { get; private set; }
        public string Texto { get; private set; }

        public override string ToString()
        {
            return Texto;
        }
    }
}
