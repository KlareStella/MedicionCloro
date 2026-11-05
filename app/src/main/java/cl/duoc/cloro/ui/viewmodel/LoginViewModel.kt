package cl.duoc.cloro.ui.viewmodel

import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.validation.Validadores
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    fun onNombre(valor: String) {
        _estado.update { it.copy(nombre = valor) }
        _errores.update { it.copy(nombre = Validadores.nombre(valor)) }
    }

    fun onCorreo(valor: String) {
        _estado.update { it.copy(correo = valor) }
        _errores.update { it.copy(correo = Validadores.correo(valor)) }
    }

    fun onClave(valor: String) {
        _estado.update { it.copy(clave = valor) }
        _errores.update { it.copy(clave = Validadores.clave(valor)) }
    }

    fun onPerfil(perfil: Perfil) {
        _estado.update { it.copy(perfil = perfil) }
    }

    fun onTerminos(acepta: Boolean) {
        _estado.update { it.copy(aceptaTerminos = acepta) }
        _errores.update { it.copy(terminos = Validadores.terminos(acepta)) }
    }

    fun validarFormulario(): Boolean {
        val e = _estado.value
        val errores = LoginErrores(
            nombre = Validadores.nombre(e.nombre),
            correo = Validadores.correo(e.correo),
            clave = Validadores.clave(e.clave),
            terminos = Validadores.terminos(e.aceptaTerminos)
        )
        _errores.value = errores
        return errores.nombre == null && errores.correo == null &&
            errores.clave == null && errores.terminos == null
    }
}
