@file:OptIn(ExperimentalMaterial3Api::class)

package cl.duoc.cloro.ui.screens

import cl.duoc.cloro.model.Estado
import cl.duoc.cloro.ui.components.ChipEstado
import cl.duoc.cloro.ui.components.Pantalla
import cl.duoc.cloro.ui.theme.colorEstado
import cl.duoc.cloro.ui.theme.formatoFecha
import cl.duoc.cloro.ui.theme.nivelTexto
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AlertasScreen(vm: MainViewModel, onPunto: (Int) -> Unit) {
    val alertas by vm.alertas.collectAsStateWithLifecycle()
    val puntos by vm.puntos.collectAsStateWithLifecycle()
    var verActivas by remember { mutableStateOf(true) }

    val nombres = puntos.associate { it.punto.id to "${it.punto.granja} · ${it.punto.galpon}" }
    val lista = alertas.filter { it.activa == verActivas }

    Pantalla("Alertas") {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = verActivas, onClick = { verActivas = true }, label = { Text("Activas") })
            FilterChip(selected = !verActivas, onClick = { verActivas = false }, label = { Text("Historial") })
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (lista.isEmpty()) {
                item(key = "vacio", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        if (verActivas) "No hay alertas activas" else "Sin historial de alertas",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            items(lista, key = { it.id }) { a ->
                val estado = Estado.valueOf(a.estado)
                val fondo by animateColorAsState(
                    colorEstado(estado).copy(alpha = if (a.activa) 0.12f else 0.04f)
                        .compositeOver(MaterialTheme.colorScheme.surface),
                    tween(600),
                    label = "fondoAlerta"
                )
                Card(
                    onClick = { onPunto(a.puntoId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = fondo)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(nombres[a.puntoId] ?: "Punto ${a.puntoId}", style = MaterialTheme.typography.titleSmall)
                            Text("Detectada ${formatoFecha(a.timestamp)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                nivelTexto(a.nivel),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorEstado(estado)
                            )
                            ChipEstado(estado)
                        }
                    }
                }
            }
        }
    }
}
