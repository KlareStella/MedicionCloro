package cl.duoc.cloro.ui.viewmodel

import cl.duoc.cloro.model.Perfil





data class LoginUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val perfil: Perfil = Perfil.OPERARIO,
    val aceptaTerminos: Boolean = false
)

data class LoginErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val terminos: String? = null
)

// ---- Formulario de registro de acción ----
val TIPOS_ACCION = listOf(
    "Control de cloro",
    "Ajuste de dosificación",
    "Recloración",
    "Revisión de equipo",
    "Aviso a supervisor"
)

data class AccionUiState(
    val tipo: String = TIPOS_ACCION[0],
    val descripcion: String = "",
    val resuelta: Boolean = false,
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class AccionErrores(val descripcion: String? = null)
