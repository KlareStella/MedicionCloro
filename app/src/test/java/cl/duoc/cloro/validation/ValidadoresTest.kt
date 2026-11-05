package cl.duoc.cloro.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {
    @Test fun nombreCortoEsInvalido() = assertNotNull(Validadores.nombre("Al"))
    @Test fun nombreValido() = assertNull(Validadores.nombre("Klare"))

    @Test fun correoSinArrobaEsInvalido() = assertNotNull(Validadores.correo("operario.cloro.cl"))
    @Test fun correoVacioEsInvalido() = assertNotNull(Validadores.correo(""))
    @Test fun correoValido() = assertNull(Validadores.correo("operario@cloro.cl"))

    @Test fun claveCortaEsInvalida() = assertNotNull(Validadores.clave("ab1"))
    @Test fun claveSinNumeroEsInvalida() = assertNotNull(Validadores.clave("abcdefg"))
    @Test fun claveValida() = assertNull(Validadores.clave("abc123"))

    @Test fun terminosNoAceptadosEsInvalido() = assertNotNull(Validadores.terminos(false))
    @Test fun terminosAceptadosEsValido() = assertNull(Validadores.terminos(true))

    @Test fun descripcionCortaEsInvalida() = assertNotNull(Validadores.descripcion("corta"))
    @Test fun descripcionValida() = assertNull(Validadores.descripcion("Se ajustó la dosificación del clorador"))
}
