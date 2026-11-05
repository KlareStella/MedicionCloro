package cl.duoc.cloro.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object Evidencias {
    private const val CARPETA = "evidencias"

    fun nuevaUri(context: Context): Uri {
        val dir = File(context.filesDir, CARPETA).apply { mkdirs() }
        val archivo = File(dir, "accion_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    fun borrar(context: Context, uri: String?) {
        if (uri == null) return
        val nombre = Uri.parse(uri).lastPathSegment ?: return
        runCatching { File(File(context.filesDir, CARPETA), nombre).delete() }
    }
}
