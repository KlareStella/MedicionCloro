@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.cloro.ui.components

import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.model.MedicionEntity
import cl.duoc.cloro.model.Rangos
import cl.duoc.cloro.ui.theme.colorEstado
import cl.duoc.cloro.ui.theme.formatoFecha
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

val LocalMenu = compositionLocalOf<(() -> Unit)?> { null }

@Composable
fun Pantalla(
    titulo: String,
    onBack: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(titulo) },
            navigationIcon = {
                val menu = LocalMenu.current
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                } else if (menu != null) {
                    IconButton(onClick = menu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú")
                    }
                }
            },
            actions = acciones,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
        contenido()
    }
}

@Composable
fun ChipEstado(estado: Estado?) {
    val color = colorEstado(estado)
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(
            text = estado?.etiqueta ?: "Sin datos",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BannerSinConexion(ultimaMedicion: Long?) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Text(
                text = "Sin conexión · mostrando datos guardados" +
                    (ultimaMedicion?.let { " (última medición ${formatoFecha(it)})" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun GraficoCloro(datos: List<MedicionEntity>) {
    if (datos.size < 2) {
        Text("Sin datos suficientes para el gráfico", style = MaterialTheme.typography.bodySmall)
        return
    }
    val maxV = maxOf(2.5, datos.maxOf { it.nivel } + 0.2)
    val colorLinea = MaterialTheme.colorScheme.primary
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val w = size.width
        val h = size.height
        fun y(v: Double): Float = (h - (v / maxV * h)).toFloat()
        fun x(i: Int): Float = i * (w / (datos.size - 1))

        drawRect(
            color = Color(0x332E7D32),
            topLeft = Offset(0f, y(Rangos.NORMAL_MAX)),
            size = Size(w, y(Rangos.NORMAL_MIN) - y(Rangos.NORMAL_MAX))
        )
        val ruta = Path()
        datos.forEachIndexed { i, m ->
            if (i == 0) ruta.moveTo(x(i), y(m.nivel)) else ruta.lineTo(x(i), y(m.nivel))
        }
        drawPath(ruta, colorLinea, style = Stroke(width = 4f))
        datos.forEachIndexed { i, m ->
            drawCircle(colorEstado(Rangos.estadoDe(m.nivel)), radius = 6f, center = Offset(x(i), y(m.nivel)))
        }
    }
}
