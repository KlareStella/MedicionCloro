package cl.duoc.cloro.notif

import cl.duoc.cloro.MainActivity
import cl.duoc.cloro.R
import cl.duoc.cloro.model.AlertaEntity
import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.repository.ClorRepository
import cl.duoc.cloro.ui.theme.nivelTexto
import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object Notificador {
    private const val CANAL = "alertas_cloro"

    fun crearCanal(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL, "Alertas de cloro", NotificationManager.IMPORTANCE_HIGH)
            canal.description = "Avisos cuando el cloro libre está fuera de rango"
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    suspend fun notificarNuevas(ctx: Context, repo: ClorRepository, nuevas: List<AlertaEntity>) {
        nuevas.forEach { notificar(ctx, it, repo.nombrePunto(it.puntoId)) }
    }

    @SuppressLint("MissingPermission")
    private fun notificar(ctx: Context, alerta: AlertaEntity, nombre: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendiente = PendingIntent.getActivity(
            ctx, alerta.puntoId, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val estado = Estado.valueOf(alerta.estado).etiqueta
        val notificacion = NotificationCompat.Builder(ctx, CANAL)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Cloro $estado: $nombre")
            .setContentText("Nivel ${nivelTexto(alerta.nivel)} fuera del rango normal")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(alerta.id.toInt(), notificacion)
    }
}
