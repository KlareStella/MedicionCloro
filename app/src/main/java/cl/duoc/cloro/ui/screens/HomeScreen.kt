@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.model.PuntoConEstado
import cl.duoc.cloro.ui.components.BannerSinConexion
import cl.duoc.cloro.ui.components.ChipEstado
import cl.duoc.cloro.ui.components.Pantalla
import cl.duoc.cloro.ui.theme.colorEstado
import cl.duoc.cloro.ui.theme.formatoFecha
import cl.duoc.cloro.ui.theme.nivelTexto
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    vm: MainViewModel,
    ancho: WindowWidthSizeClass,
    onPunto: (Int) -> Unit,
    onRegistrar: (Int) -> Unit
) {
    val puntos by vm.puntos.collectAsStateWithLifecycle()
    val cargando by vm.cargando.collectAsStateWithLifecycle()
    val sinConexion by vm.sinConexion.collectAsStateWithLifecycle()
    val simula by vm.simulaOffline.collectAsStateWithLifecycle()
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val soloProblemas by vm.soloProblemas.collectAsStateWithLifecycle()
    var seleccion by rememberSaveable { mutableStateOf(-1) }

    val dosPaneles = ancho != WindowWidthSizeClass.Compact
    val visibles = if (soloProblemas) {
        puntos.filter { it.estado != null && it.estado != Estado.NORMAL }
    } else puntos
    val idSeleccionado = visibles.firstOrNull { it.punto.id == seleccion }?.punto?.id
        ?: visibles.firstOrNull()?.punto?.id

    Pantalla(
        titulo = "Puntos de medición",
        acciones = {
            IconButton(onClick = { vm.alternarSimulacionSinConexion() }) {
                Icon(
                    Icons.Filled.CloudOff,
                    contentDescription = "Simular sin conexión",
                    tint = if (simula) Color.Yellow else Color.White
                )
            }
            IconButton(onClick = { vm.refrescar() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar")
            }
        }
    ) {
        AnimatedVisibility(visible = sinConexion) {
            BannerSinConexion(puntos.mapNotNull { it.ultima?.timestamp }.maxOrNull())
        }
        if (cargando) LinearProgressIndicator(Modifier.fillMaxWidth())

        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = sesion?.let { "${it.usuario} · ${it.perfil.etiqueta}" } ?: "",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = soloProblemas,
                onClick = { vm.cambiarSoloProblemas(!soloProblemas) },
                label = { Text("Solo fuera de rango") }
            )
        }

        if (dosPaneles) {
            Row(Modifier.weight(1f)) {
                ListaPuntos(visibles, soloProblemas, idSeleccionado, Modifier.weight(1f)) { seleccion = it }
                Box(Modifier.weight(1.3f).fillMaxHeight()) {
                    if (idSeleccionado != null) {
                        DetalleContenido(vm, idSeleccionado, onRegistrar = { onRegistrar(idSeleccionado) })
                    } else {
                        Text("Selecciona un punto", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        } else {
            ListaPuntos(visibles, soloProblemas, null, Modifier.weight(1f)) { onPunto(it) }
        }
    }
}

@Composable
private fun ListaPuntos(
    visibles: List<PuntoConEstado>,
    soloProblemas: Boolean,
    seleccionado: Int?,
    modifier: Modifier,
    onPunto: (Int) -> Unit
) {
    LazyColumn(
        modifier,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (visibles.isEmpty()) {
            item(key = "vacio") {
                Text(
                    if (soloProblemas) "No hay puntos fuera de rango" else "Sin datos todavía. Toca actualizar.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        visibles.groupBy { it.punto.granja }.forEach { (granja, lista) ->
            item(key = "g_$granja") {
                Text(granja, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(lista, key = { it.punto.id }) { p ->
                TarjetaPunto(p, seleccionada = p.punto.id == seleccionado) { onPunto(p.punto.id) }
            }
        }
    }
}

@Composable
private fun TarjetaPunto(p: PuntoConEstado, seleccionada: Boolean, onClick: () -> Unit) {
    // Animaciones declarativas (Guía 12): el color de fondo y del nivel cambian suavemente con el estado.
    val fondo by animateColorAsState(
        targetValue = colorEstado(p.estado)
            .copy(alpha = if (seleccionada) 0.22f else 0.07f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        animationSpec = tween(600),
        label = "fondoTarjeta"
    )
    val colorNivel by animateColorAsState(
        targetValue = colorEstado(p.estado),
        animationSpec = tween(600),
        label = "colorNivel"
    )
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = fondo)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(p.punto.galpon, style = MaterialTheme.typography.titleSmall)
                Text(p.punto.nombre, style = MaterialTheme.typography.bodySmall)
                Text(
                    p.ultima?.let { "Actualizado ${formatoFecha(it.timestamp)}" } ?: "Sin mediciones",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    p.ultima?.let { nivelTexto(it.nivel) } ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorNivel
                )
                ChipEstado(p.estado)
            }
        }
    }
}
