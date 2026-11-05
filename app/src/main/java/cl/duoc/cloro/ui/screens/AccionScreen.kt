package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.ui.components.CampoValidado
import cl.duoc.cloro.ui.components.ImagenEvidencia
import cl.duoc.cloro.ui.components.Pantalla
import cl.duoc.cloro.ui.viewmodel.AccionViewModel
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import cl.duoc.cloro.ui.viewmodel.TIPOS_ACCION
import cl.duoc.cloro.util.Evidencias
import cl.duoc.cloro.util.obtenerUbicacion
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun AccionScreen(
    vm: MainViewModel,
    id: Int,
    accionId: Long,
    onBack: () -> Unit,
    form: AccionViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val editando = accionId > 0
    val punto by remember(id) { vm.punto(id) }.collectAsStateWithLifecycle(initialValue = null)
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val estado by form.estado.collectAsStateWithLifecycle()
    val errores by form.errores.collectAsStateWithLifecycle()
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var buscandoUbicacion by remember { mutableStateOf(false) }

    // En modo edición se cargan los datos guardados en el formulario (una sola vez).
    LaunchedEffect(accionId) {
        if (editando && form.original == null) vm.obtenerAccion(accionId)?.let { form.cargar(it) }
    }

    // ---- Recurso 1: cámara (app del sistema + FileProvider, sin permisos de almacenamiento) ----
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val nueva = uriPendiente
        if (ok && nueva != null) {
            // Si se reemplaza una foto tomada en esta pantalla, se borra la anterior
            if (estado.fotoUri != null && estado.fotoUri != form.original?.fotoUri) {
                Evidencias.borrar(context, estado.fotoUri)
            }
            form.onFoto(nueva)
        } else {
            Evidencias.borrar(context, nueva)
        }
        uriPendiente = null
    }

    fun capturarUbicacion() {
        scope.launch {
            buscandoUbicacion = true
            val loc = obtenerUbicacion(context)
            buscandoUbicacion = false
            if (loc != null) {
                form.onUbicacion(loc.latitude, loc.longitude)
            } else {
                vm.mensajes.tryEmit("No se pudo obtener la ubicación. Revisa que el GPS esté activo.")
            }
        }
    }

    val permisoUbicacion = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) {
            capturarUbicacion()
        } else {
            vm.mensajes.tryEmit("Sin permiso de ubicación: no se guardará dónde se hizo el control")
        }
    }

    Pantalla(if (editando) "Editar acción" else "Registrar acción", onBack = onBack) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .widthIn(max = 600.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                punto?.let { "${it.granja} · ${it.galpon}" } ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Tipo de registro", style = MaterialTheme.typography.labelLarge)
            TIPOS_ACCION.forEach { t ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = estado.tipo == t, onClick = { form.onTipo(t) }, role = Role.RadioButton),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.tipo == t, onClick = null)
                    Text(t, modifier = Modifier.padding(start = 8.dp))
                }
            }

            CampoValidado(
                valor = estado.descripcion,
                onCambio = form::onDescripcion,
                etiqueta = "Descripción de la acción",
                icono = Icons.Filled.Description,
                error = errores.descripcion,
                singleLine = false,
                minLines = 3,
                ayuda = "${estado.descripcion.length}/${AccionViewModel.MAX_DESCRIPCION}"
            )

            Text("Evidencia (opcional)", style = MaterialTheme.typography.labelLarge)
            Text(
                "La ubicación solo se usa para registrar dónde se hizo el control.",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val uri = Evidencias.nuevaUri(context)
                        uriPendiente = uri.toString()
                        try {
                            camara.launch(uri)
                        } catch (e: ActivityNotFoundException) {
                            Evidencias.borrar(context, uri.toString())
                            uriPendiente = null
                            vm.mensajes.tryEmit("No hay una app de cámara disponible")
                        }
                    }
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (estado.fotoUri == null) "Tomar foto" else "Repetir foto")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !buscandoUbicacion,
                    onClick = {
                        val permisos = arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        val concedido = permisos.any {
                            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                        }
                        if (concedido) capturarUbicacion() else permisoUbicacion.launch(permisos)
                    }
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (buscandoUbicacion) "Buscando…" else "Ubicación")
                }
            }

            AnimatedVisibility(visible = estado.fotoUri != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ImagenEvidencia(
                        uri = estado.fotoUri.orEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    TextButton(onClick = {
                        if (estado.fotoUri != form.original?.fotoUri) Evidencias.borrar(context, estado.fotoUri)
                        form.onFoto(null)
                    }) { Text("Quitar foto") }
                }
            }
            AnimatedVisibility(visible = estado.latitud != null && estado.longitud != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        String.format(Locale.US, "%.5f, %.5f", estado.latitud ?: 0.0, estado.longitud ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { form.onUbicacion(null, null) }) { Text("Quitar") }
                }
            }

            // Solo el supervisor puede dar por resuelta una desviación.
            if (sesion?.perfil == Perfil.SUPERVISOR) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = estado.resuelta, onCheckedChange = form::onResuelta)
                    Text("Desviación resuelta (cierra la alerta)")
                }
            }
            Button(
                onClick = {
                    if (form.validarFormulario()) {
                        if (editando) {
                            form.original?.let { vm.actualizarAccion(it, estado, onBack) }
                        } else {
                            vm.registrarAccion(id, estado, onBack)
                        }
                    }
                },
                enabled = !editando || form.original != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (editando) "Guardar cambios" else "Guardar") }
        }
    }
}
