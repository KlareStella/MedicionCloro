package cl.duoc.cloro.repository

import cl.duoc.cloro.data.local.ClorDao
import cl.duoc.cloro.data.remote.ClorApi
import cl.duoc.cloro.model.AccionEntity
import cl.duoc.cloro.model.AlertaEntity
import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.model.MedicionEntity
import cl.duoc.cloro.model.PuntoEntity
import cl.duoc.cloro.model.Rangos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClorRepository(private val dao: ClorDao, private val api: ClorApi) {

    val puntos = dao.puntos()
    val ultimas = dao.ultimas()
    val alertas = dao.alertas()
    val ultimasAcciones = dao.ultimasAcciones()

    val todasAcciones = dao.todasAcciones()

    fun punto(id: Int) = dao.punto(id)
    fun historial(id: Int) = dao.historial(id, 48)
    fun acciones(id: Int) = dao.acciones(id)

    suspend fun nombrePunto(id: Int): String =
        dao.puntoOnce(id)?.let { "${it.granja} · ${it.galpon}" } ?: "Punto $id"

    suspend fun accion(id: Long): AccionEntity? = dao.accionOnce(id)

    suspend fun actualizarAccion(accion: AccionEntity) = withContext(Dispatchers.IO) {
        dao.actualizarAccion(accion)
        if (accion.resuelta) dao.cerrarAlertas(accion.puntoId)
    }

    suspend fun eliminarAccion(accion: AccionEntity) = withContext(Dispatchers.IO) {
        dao.eliminarAccion(accion)
    }

    suspend fun registrarAccion(accion: AccionEntity) = withContext(Dispatchers.IO) {
        dao.insertAccion(accion)
        if (accion.resuelta) dao.cerrarAlertas(accion.puntoId)
    }

    suspend fun refrescar(): Result<List<AlertaEntity>> = withContext(Dispatchers.IO) {
        runCatching {
            val puntosApi = api.puntos()
            val medicionesApi = api.mediciones()

            dao.upsertPuntos(puntosApi.map { PuntoEntity(it.id, it.granja, it.galpon, it.nombre) })
            dao.insertMediciones(
                medicionesApi.map { MedicionEntity(puntoId = it.puntoId, nivel = it.nivel, timestamp = it.timestamp) }
            )

            val nuevas = mutableListOf<AlertaEntity>()
            medicionesApi.groupBy { it.puntoId }.forEach { (id, lista) ->
                val ultima = lista.maxBy { it.timestamp }
                val estado = Rangos.estadoDe(ultima.nivel)
                val activa = dao.alertaActiva(id)
                if (estado == Estado.NORMAL) {
                    if (activa != null) dao.cerrarAlertas(id)
                } else if (activa == null || activa.estado != estado.name) {
                    dao.cerrarAlertas(id)
                    val alerta = AlertaEntity(
                        puntoId = id,
                        nivel = ultima.nivel,
                        estado = estado.name,
                        timestamp = ultima.timestamp
                    )
                    nuevas += alerta.copy(id = dao.insertAlerta(alerta))
                }
            }
            nuevas.toList()
        }
    }
}
