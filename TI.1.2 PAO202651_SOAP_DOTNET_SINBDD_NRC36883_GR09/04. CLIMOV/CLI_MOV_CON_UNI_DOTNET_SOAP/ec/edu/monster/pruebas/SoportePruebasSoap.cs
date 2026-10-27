using System;

namespace ec.edu.monster.pruebas;

internal static class SoportePruebasSoap
{
    internal static void Exigir(bool condicion, string mensaje)
    {
        if (!condicion) throw new InvalidOperationException(mensaje);
    }
}
