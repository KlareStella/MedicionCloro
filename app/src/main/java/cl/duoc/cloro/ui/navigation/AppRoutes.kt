package cl.duoc.cloro.ui.navigation


object AppRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val ALERTAS = "alertas"
    const val RESUMEN = "resumen"
    const val BITACORA = "bitacora"
    const val DETALLE = "detalle/{id}"
    const val ACCION = "accion/{id}?accionId={accionId}"

    fun detalle(id: Int) = "detalle/$id"
    /** Sin [accionId] abre el formulario para crear; con [accionId] abre el modo edición. */
    fun accion(id: Int, accionId: Long? = null) =
        if (accionId == null) "accion/$id" else "accion/$id?accionId=$accionId"

    val PESTANAS = setOf(HOME, ALERTAS, BITACORA, RESUMEN)
}
