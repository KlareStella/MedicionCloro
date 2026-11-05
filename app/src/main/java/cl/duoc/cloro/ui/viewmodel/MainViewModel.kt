package cl.duoc.cloro.ui.viewmodel

import cl.duoc.cloro.ClorApp
import cl.duoc.cloro.data.remote.Simulador
import cl.duoc.cloro.model.AccionEntity
import cl.duoc.cloro.model.AlertaEntity
import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.model.PuntoConEstado
import cl.duoc.cloro.model.Sesion
import cl.duoc.cloro.notif.Notificador
import cl.duoc.cloro.util.Evidencias
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val clorApp = app as ClorApp
    private val repo = clorApp.repo
    private val preferencias = clorApp.preferencias

    private fun <T> Flow<T>.estado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), inicial)

    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    private val _sesionLista = MutableStateFlow(false)
    val sesionLista: StateFlow<Boolean> = _sesionLista.asStateFlow()

    val puntos: StateFlow<List<PuntoConEstado>> =
        combine(repo.puntos, repo.ultimas) { ps, us ->
            val porPunto = us.associateBy { it.puntoId }
            ps.map { PuntoConEstado(it, porPunto[it.id]) }
        }.estado(emptyList())

    val alertas: StateFlow<List<AlertaEntity>> = repo.alertas.estado(emptyList())
    val ultimasAcciones: StateFlow<List<AccionEntity>> = repo.ultimasAcciones.estado(emptyList())
    val bitacora: StateFlow<List<AccionEntity>> = repo.todasAcciones.estado(emptyList())

    val soloProblemas: StateFlow<Boolean> = preferencias.soloProblemas.estado(false)

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _sinConexion = MutableStateFlow(false)
    val sinConexion: StateFlow<Boolean> = _sinConexion.asStateFlow()

    private val _simulaOffline = MutableStateFlow(Simulador.sinConexion)
    val simulaOffline: StateFlow<Boolean> = _simulaOffline.asStateFlow()

    val mensajes = MutableSharedFlow<String>(extraBufferCapacity = 4)

    init {
        viewModelScope.launch {
            val guardada = preferencias.sesion.first()
            _sesion.value = guardada
            _sesionLista.value = true
            if (guardada != null) refrescar()
        }
    }

    fun punto(id: Int) = repo.punto(id)
    fun historial(id: Int) = repo.historial(id)
    fun acciones(id: Int) = repo.acciones(id)

    fun refrescar() {
        viewModelScope.launch {
            _cargando.value = true
            repo.refrescar()
                .onSuccess { nuevas ->
                    _sinConexion.value = false
                    Notificador.notificarNuevas(getApplication(), repo, nuevas)
                }
                .onFailure {
                    _sinConexion.value = true
                    mensajes.tryEmit("Sin conexión: mostrando los últimos datos guardados")
                }
            _cargando.value = false
        }
    }

    fun alternarSimulacionSinConexion() {
        Simulador.sinConexion = !Simulador.sinConexion
        _simulaOffline.value = Simulador.sinConexion
        refrescar()
    }

    fun cambiarSoloProblemas(valor: Boolean) {
        viewModelScope.launch { preferencias.guardarSoloProblemas(valor) }
    }

    fun iniciarSesion(usuario: String, perfil: Perfil) {
        val nueva = Sesion(usuario, perfil)
        _sesion.value = nueva
        viewModelScope.launch { preferencias.guardarSesion(nueva) }
        refrescar()
    }

    fun cerrarSesion() {
        _sesion.value = null
        viewModelScope.launch { preferencias.cerrarSesion() }
    }

    suspend fun obtenerAccion(id: Long): AccionEntity? = repo.accion(id)

    fun registrarAccion(puntoId: Int, datos: AccionUiState, onListo: () -> Unit) {
        val s = _sesion.value ?: return
        viewModelScope.launch {
            repo.registrarAccion(
                AccionEntity(
                    puntoId = puntoId,
                    tipo = datos.tipo,
                    descripcion = datos.descripcion.trim(),
                    usuario = s.usuario,
                    perfil = s.perfil.name,
                    resuelta = datos.resuelta,
                    timestamp = System.currentTimeMillis(),
                    fotoUri = datos.fotoUri,
                    latitud = datos.latitud,
                    longitud = datos.longitud
                )
            )
            mensajes.tryEmit("Acción registrada")
            onListo()
        }
    }

    fun actualizarAccion(original: AccionEntity, datos: AccionUiState, onListo: () -> Unit) {
        viewModelScope.launch {
            repo.actualizarAccion(
                original.copy(
                    tipo = datos.tipo,
                    descripcion = datos.descripcion.trim(),
                    resuelta = datos.resuelta,
                    fotoUri = datos.fotoUri,
                    latitud = datos.latitud,
                    longitud = datos.longitud
                )
            )
            // Si se cambió o quitó la foto, se borra el archivo anterior
            if (original.fotoUri != null && original.fotoUri != datos.fotoUri) {
                Evidencias.borrar(getApplication(), original.fotoUri)
            }
            mensajes.tryEmit("Cambios guardados")
            onListo()
        }
    }

    fun eliminarAccion(accion: AccionEntity) {
        viewModelScope.launch {
            repo.eliminarAccion(accion)
            Evidencias.borrar(getApplication(), accion.fotoUri)
            mensajes.tryEmit("Acción eliminada")
        }
    }
}
