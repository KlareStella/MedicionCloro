package cl.duoc.cloro.validation


/**
 * Reglas de validación centralizadas (IE 2.2.1).
 * No depende de Android ni de la interfaz: los ViewModels las llaman y la UI solo muestra el resultado.
 * Cada función retorna el mensaje de error, o null si el valor es válido.
 */
object Validadores {
    const val MIN_NOMBRE = 3
    const val MIN_CLAVE = 6
    const val MIN_DESCRIPCION = 10
    const val MAX_DESCRIPCION = 200

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun nombre(valor: String): String? =
        if (valor.trim().length < MIN_NOMBRE) "Ingresa al menos $MIN_NOMBRE caracteres" else null

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !EMAIL.matches(valor.trim()) -> "Ingresa un correo válido (ej: nombre@empresa.cl)"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.length < MIN_CLAVE -> "La clave debe tener al menos $MIN_CLAVE caracteres"
        valor.none { it.isDigit() } -> "La clave debe incluir al menos un número"
        else -> null
    }

    fun terminos(aceptado: Boolean): String? =
        if (!aceptado) "Debes aceptar para continuar" else null

    fun descripcion(valor: String): String? =
        if (valor.trim().length < MIN_DESCRIPCION) "Describe la acción con al menos $MIN_DESCRIPCION caracteres" else null
}
