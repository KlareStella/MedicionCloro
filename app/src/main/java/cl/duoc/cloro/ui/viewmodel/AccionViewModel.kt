package cl.duoc.cloro.ui.viewmodel

import cl.duoc.cloro.model.AccionEntity
import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.validation.Validadores
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AccionViewModel : ViewModel() {

    private val _estado = MutableStateFlow(AccionUiState())
    val estado: StateFlow<AccionUiState> = _estado.asStateFlow()

    private val _errores = MutableStateFlow(AccionErrores())
    val errores: StateFlow<AccionErrores> = _errores.asStateFlow()

    var original: AccionEntity? = null
        private set

    fun cargar(accion: AccionEntity) {
        original = accion
        _estado.value = AccionUiState(
            tipo = accion.tipo,
            descripcion = accion.descripcion,
            resuelta = accion.resuelta,
            fotoUri = accion.fotoUri,
            latitud = accion.latitud,
            longitud = accion.longitud
        )
        _errores.value = AccionErrores()
    }

    fun onTipo(tipo: String) = _estado.update { it.copy(tipo = tipo) }

    fun onDescripcion(valor: String) {
        if (valor.length > MAX_DESCRIPCION) return
        _estado.update { it.copy(descripcion = valor) }
        _errores.value = AccionErrores(descripcion = Validadores.descripcion(valor))
    }

    fun onResuelta(valor: Boolean) = _estado.update { it.copy(resuelta = valor) }

    fun onFoto(uri: String?) = _estado.update { it.copy(fotoUri = uri) }

    fun onUbicacion(latitud: Double?, longitud: Double?) =
        _estado.update { it.copy(latitud = latitud, longitud = longitud) }

    fun validarFormulario(): Boolean {
        val error = Validadores.descripcion(_estado.value.descripcion)
        _errores.value = AccionErrores(descripcion = error)
        return error == null
    }

    companion object {
        const val MAX_DESCRIPCION = Validadores.MAX_DESCRIPCION
    }
}
