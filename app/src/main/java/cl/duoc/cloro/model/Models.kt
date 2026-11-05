package cl.duoc.cloro.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Perfil(val etiqueta: String) {
    OPERARIO("Operario"),
    SUPERVISOR("Supervisor"),
    JEFATURA("Jefatura")
}

enum class Estado(val etiqueta: String) {
    NORMAL("Normal"),
    ADVERTENCIA("Advertencia"),
    CRITICO("Crítico")
}

object Rangos {
    const val NORMAL_MIN = 0.3
    const val NORMAL_MAX = 1.5
    const val CRITICO_MIN = 0.2
    const val CRITICO_MAX = 2.0

    fun estadoDe(nivel: Double): Estado = when {
        nivel in NORMAL_MIN..NORMAL_MAX -> Estado.NORMAL
        nivel in CRITICO_MIN..CRITICO_MAX -> Estado.ADVERTENCIA
        else -> Estado.CRITICO
    }
}

@Entity(tableName = "puntos")
data class PuntoEntity(
    @PrimaryKey val id: Int,
    val granja: String,
    val galpon: String,
    val nombre: String
)

@Entity(
    tableName = "mediciones",
    indices = [Index(value = ["puntoId", "timestamp"], unique = true)]
)
data class MedicionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val puntoId: Int,
    val nivel: Double,
    val timestamp: Long
)

@Entity(tableName = "alertas")
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val puntoId: Int,
    val nivel: Double,
    val estado: String,
    val timestamp: Long,
    val activa: Boolean = true
)

@Entity(tableName = "acciones")
data class AccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val puntoId: Int,
    val tipo: String,
    val descripcion: String,
    val usuario: String,
    val perfil: String,
    val resuelta: Boolean,
    val timestamp: Long,
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
)

data class PuntoConEstado(val punto: PuntoEntity, val ultima: MedicionEntity?) {
    val estado: Estado? get() = ultima?.let { Rangos.estadoDe(it.nivel) }
}

data class Sesion(val usuario: String, val perfil: Perfil) {
    fun puedeModificar(accion: AccionEntity): Boolean =
        perfil == Perfil.SUPERVISOR || (perfil == Perfil.OPERARIO && accion.usuario == usuario)
}
