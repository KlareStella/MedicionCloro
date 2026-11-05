package cl.duoc.cloro.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume


@SuppressLint("MissingPermission")
suspend fun obtenerUbicacion(context: Context): Location? {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val fina = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val candidatos = if (fina) {
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    } else {
        listOf(LocationManager.NETWORK_PROVIDER)
    }
    val proveedores = candidatos.filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
    if (proveedores.isEmpty()) return null

    for (p in proveedores) {
        val ultima = runCatching { lm.getLastKnownLocation(p) }.getOrNull()
        if (ultima != null) return ultima
    }
    return try {
        withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                val senal = CancellationSignal()
                cont.invokeOnCancellation { senal.cancel() }
                LocationManagerCompat.getCurrentLocation(
                    lm, proveedores.first(), senal, ContextCompat.getMainExecutor(context)
                ) { loc -> if (cont.isActive) cont.resume(loc) }
            }
        }
    } catch (e: SecurityException) {
        null
    }
}
