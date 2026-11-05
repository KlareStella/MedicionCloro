package cl.duoc.cloro.model
data class PuntoDto(val id: Int, val granja: String, val galpon: String, val nombre: String)
data class MedicionDto(val puntoId: Int, val nivel: Double, val timestamp: Long)
