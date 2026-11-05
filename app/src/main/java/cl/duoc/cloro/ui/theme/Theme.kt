package cl.duoc.cloro.ui.theme

import cl.duoc.cloro.model.Estado
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0277BD),
            secondary = Color(0xFF00838F)
        ),
        content = content
    )
}

fun colorEstado(estado: Estado?): Color = when (estado) {
    Estado.NORMAL -> Color(0xFF2E7D32)
    Estado.ADVERTENCIA -> Color(0xFFEF6C00)
    Estado.CRITICO -> Color(0xFFC62828)
    null -> Color.Gray
}

private val localeCl = Locale("es", "CL")

fun formatoFecha(ts: Long): String = SimpleDateFormat("dd/MM HH:mm", localeCl).format(Date(ts))

fun nivelTexto(nivel: Double): String = String.format(localeCl, "%.2f ppm", nivel)
