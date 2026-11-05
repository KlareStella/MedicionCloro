package cl.duoc.cloro.ui.components

import cl.duoc.cloro.model.AccionEntity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ImagenEvidencia(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imagen by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { cargarBitmap(context, uri) }
    }
    imagen?.let {
        Image(
            bitmap = it,
            contentDescription = "Foto de evidencia",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun cargarBitmap(context: Context, uri: String): ImageBitmap? = runCatching {
    val u = Uri.parse(uri)
    val orientacion = context.contentResolver.openInputStream(u)?.use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    } ?: ExifInterface.ORIENTATION_NORMAL
    val grados = when (orientacion) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
    var bmp: Bitmap? = context.contentResolver.openInputStream(u)?.use {
        BitmapFactory.decodeStream(it, null, opciones)
    }
    if (bmp != null && grados != 0f) {
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(grados) }, true)
    }
    bmp?.asImageBitmap()
}.getOrNull()

@Composable
fun EvidenciaAccion(accion: AccionEntity, nombrePunto: String? = null) {
    val context = LocalContext.current
    val lat = accion.latitud
    val lng = accion.longitud
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        accion.fotoUri?.let {
            ImagenEvidencia(
                uri = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        if (lat != null && lng != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    String.format(Locale.US, "%.5f, %.5f", lat, lng),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { abrirMapa(context, lat, lng, nombrePunto) }) { Text("Ver en mapa") }
            }
        }
    }
}

private fun abrirMapa(context: Context, lat: Double, lng: Double, etiqueta: String?) {
    val q = Uri.encode(etiqueta ?: "Control de cloro")
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng?q=$lat,$lng($q)"))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay una app de mapas instalada", Toast.LENGTH_SHORT).show()
    }
}
