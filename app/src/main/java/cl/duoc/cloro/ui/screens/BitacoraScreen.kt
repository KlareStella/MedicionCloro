@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.model.AccionEntity
import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.ui.components.EvidenciaAccion
import cl.duoc.cloro.ui.components.Pantalla
import cl.duoc.cloro.ui.theme.formatoFecha
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun BitacoraScreen(vm: MainViewModel, onEditar: (puntoId: Int, accionId: Long) -> Unit) {
    val acciones by vm.bitacora.collectAsStateWithLifecycle()
    val puntos by vm.puntos.collectAsStateWithLifecycle()
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    var soloMias by remember { mutableStateOf(false) }
    var porEliminar by remember { mutableStateOf<AccionEntity?>(null) }

    val nombres = puntos.associate { it.punto.id to "${it.punto.granja} · ${it.punto.galpon}" }
    val lista = if (soloMias) acciones.filter { it.usuario == sesion?.usuario } else acciones

    Pantalla("Bitácora de acciones") {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !soloMias, onClick = { soloMias = false }, label = { Text("Todas") })
            FilterChip(selected = soloMias, onClick = { soloMias = true }, label = { Text("Mis registros") })
        }
        Text(
            "Acciones registradas: ${lista.size}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (lista.isEmpty()) {
                item(key = "vacio", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Aún no hay acciones. Regístralas desde el detalle de un punto de medición.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(lista, key = { it.id }) { a ->
                val puedeModificar = sesion?.puedeModificar(a) == true
                Card(Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            a.tipo + if (a.resuelta) " · Resuelta" else "",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(nombres[a.puntoId] ?: "Punto ${a.puntoId}", style = MaterialTheme.typography.labelMedium)
                        Text(a.descripcion, style = MaterialTheme.typography.bodyMedium)
                        EvidenciaAccion(a, nombres[a.puntoId])
                        Text(
                            "${a.usuario} (${Perfil.valueOf(a.perfil).etiqueta}) · ${formatoFecha(a.timestamp)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (puedeModificar) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onEditar(a.puntoId, a.id) }) {
                                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Editar")
                                }
                                OutlinedButton(onClick = { porEliminar = a }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    porEliminar?.let { a ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar acción") },
            text = { Text("¿Seguro que quieres eliminar \"${a.tipo}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.eliminarAccion(a)
                    porEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}
