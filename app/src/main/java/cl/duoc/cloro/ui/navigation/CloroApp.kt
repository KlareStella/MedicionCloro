@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)

package cl.duoc.cloro.ui.navigation

import cl.duoc.cloro.model.Perfil
import cl.duoc.cloro.model.Sesion
import cl.duoc.cloro.ui.components.LocalMenu
import cl.duoc.cloro.ui.screens.AccionScreen
import cl.duoc.cloro.ui.screens.AlertasScreen
import cl.duoc.cloro.ui.screens.BitacoraScreen
import cl.duoc.cloro.ui.screens.DetalleScreen
import cl.duoc.cloro.ui.screens.HomeScreen
import cl.duoc.cloro.ui.screens.LoginScreen
import cl.duoc.cloro.ui.screens.ResumenScreen
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch


@Composable
fun CloroApp(vm: MainViewModel, ancho: WindowWidthSizeClass) {
    val lista by vm.sesionLista.collectAsStateWithLifecycle()
    if (!lista) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        CloroNavegacion(vm, ancho)
    }
}

@Composable
private fun CloroNavegacion(vm: MainViewModel, ancho: WindowWidthSizeClass) {
    val nav = rememberNavController()
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val alertas by vm.alertas.collectAsStateWithLifecycle()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route ?: ""
    val snack = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val inicio = remember { if (vm.sesion.value == null) AppRoutes.LOGIN else AppRoutes.HOME }

    LaunchedEffect(Unit) { vm.mensajes.collect { snack.showSnackbar(it) } }

    val permanente = ancho == WindowWidthSizeClass.Expanded
    val enPestanas = ruta in AppRoutes.PESTANAS && sesion != null
    val perfil = sesion?.perfil ?: Perfil.OPERARIO
    val nAlertas = alertas.count { it.activa }

    val irAPestana: (String) -> Unit = { destino ->
        irA(nav, destino)
        scope.launch { drawerState.close() }
    }
    val salir: () -> Unit = {
        scope.launch { drawerState.close() }
        vm.cerrarSesion()
        nav.navigate(AppRoutes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
    }
    val abrirMenu: (() -> Unit)? =
        if (enPestanas && !permanente) ({ scope.launch { drawerState.open() } }) else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = enPestanas && !permanente,
        drawerContent = {
            ModalDrawerSheet {
                ItemsDrawer(ruta, sesion, perfil, nAlertas, irAPestana, salir)
            }
        }
    ) {
        CompositionLocalProvider(LocalMenu provides abrirMenu) {
            Scaffold(
                snackbarHost = { SnackbarHost(snack) },
                bottomBar = {
                    if (enPestanas && !permanente) BarraInferior(ruta, perfil, nAlertas, irAPestana)
                }
            ) { pad ->
                Row(Modifier.padding(pad).fillMaxSize()) {
                    if (enPestanas && permanente) {
                        PermanentDrawerSheet(Modifier.fillMaxHeight().width(280.dp)) {
                            ItemsDrawer(ruta, sesion, perfil, nAlertas, irAPestana, salir)
                        }
                    }
                    NavHost(
                        navController = nav,
                        startDestination = inicio,
                        modifier = Modifier.weight(1f),
                        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 } },
                        exitTransition = { fadeOut(tween(200)) },
                        popEnterTransition = { fadeIn(tween(300)) },
                        popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 8 } }
                    ) {
                        composable(AppRoutes.LOGIN) {
                            LoginScreen(onEntrar = { usuario, p ->
                                vm.iniciarSesion(usuario, p)
                                nav.navigate(AppRoutes.HOME) { popUpTo(AppRoutes.LOGIN) { inclusive = true } }
                            })
                        }
                        composable(AppRoutes.HOME) {
                            HomeScreen(
                                vm = vm,
                                ancho = ancho,
                                onPunto = { nav.navigate(AppRoutes.detalle(it)) },
                                onRegistrar = { nav.navigate(AppRoutes.accion(it)) }
                            )
                        }
                        composable(AppRoutes.ALERTAS) {
                            AlertasScreen(vm) { nav.navigate(AppRoutes.detalle(it)) }
                        }
                        composable(AppRoutes.BITACORA) {
                            BitacoraScreen(vm) { puntoId, accionId ->
                                nav.navigate(AppRoutes.accion(puntoId, accionId))
                            }
                        }
                        composable(AppRoutes.RESUMEN) { ResumenScreen(vm) }
                        composable(
                            AppRoutes.DETALLE,
                            arguments = listOf(navArgument("id") { type = NavType.IntType })
                        ) { e ->
                            val id = e.arguments?.getInt("id") ?: 0
                            DetalleScreen(
                                vm, id,
                                onBack = { nav.popBackStack() },
                                onRegistrar = { nav.navigate(AppRoutes.accion(id)) }
                            )
                        }
                        composable(
                            AppRoutes.ACCION,
                            arguments = listOf(
                                navArgument("id") { type = NavType.IntType },
                                navArgument("accionId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { e ->
                            val id = e.arguments?.getInt("id") ?: 0
                            val accionId = e.arguments?.getLong("accionId") ?: -1L
                            AccionScreen(vm, id, accionId, onBack = { nav.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemsDrawer(
    ruta: String,
    sesion: Sesion?,
    perfil: Perfil,
    nAlertas: Int,
    onIr: (String) -> Unit,
    onSalir: () -> Unit
) {
    val padding = NavigationDrawerItemDefaults.ItemPadding
    Column(Modifier.padding(16.dp)) {
        Text("Control de cloro", style = MaterialTheme.typography.titleLarge)
        Text(
            sesion?.let { "${it.usuario} · ${it.perfil.etiqueta}" } ?: "",
            style = MaterialTheme.typography.bodySmall
        )
    }
    HorizontalDivider()
    Spacer(Modifier.height(8.dp))
    NavigationDrawerItem(
        label = { Text("Puntos de medición") },
        icon = { Icon(Icons.Filled.Opacity, contentDescription = null) },
        selected = ruta == AppRoutes.HOME,
        onClick = { onIr(AppRoutes.HOME) },
        modifier = Modifier.padding(padding)
    )
    NavigationDrawerItem(
        label = { Text("Alertas") },
        icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
        badge = { if (nAlertas > 0) Text("$nAlertas") },
        selected = ruta == AppRoutes.ALERTAS,
        onClick = { onIr(AppRoutes.ALERTAS) },
        modifier = Modifier.padding(padding)
    )
    NavigationDrawerItem(
        label = { Text("Bitácora de acciones") },
        icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
        selected = ruta == AppRoutes.BITACORA,
        onClick = { onIr(AppRoutes.BITACORA) },
        modifier = Modifier.padding(padding)
    )
    if (perfil != Perfil.OPERARIO) {
        NavigationDrawerItem(
            label = { Text("Resumen e históricos") },
            icon = { Icon(Icons.Filled.Assessment, contentDescription = null) },
            selected = ruta == AppRoutes.RESUMEN,
            onClick = { onIr(AppRoutes.RESUMEN) },
            modifier = Modifier.padding(padding)
        )
    }
    HorizontalDivider(Modifier.padding(vertical = 8.dp))
    NavigationDrawerItem(
        label = { Text("Cerrar sesión") },
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
        selected = false,
        onClick = onSalir,
        modifier = Modifier.padding(padding)
    )
}

@Composable
private fun BarraInferior(ruta: String, perfil: Perfil, nAlertas: Int, onIr: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = ruta == AppRoutes.HOME,
            onClick = { onIr(AppRoutes.HOME) },
            icon = { Icon(Icons.Filled.Opacity, contentDescription = null) },
            label = { Text("Puntos") }
        )
        NavigationBarItem(
            selected = ruta == AppRoutes.ALERTAS,
            onClick = { onIr(AppRoutes.ALERTAS) },
            icon = {
                BadgedBox(badge = { if (nAlertas > 0) Badge { Text("$nAlertas") } }) {
                    Icon(Icons.Filled.Notifications, contentDescription = null)
                }
            },
            label = { Text("Alertas") }
        )
        NavigationBarItem(
            selected = ruta == AppRoutes.BITACORA,
            onClick = { onIr(AppRoutes.BITACORA) },
            icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            label = { Text("Bitácora") }
        )
        if (perfil != Perfil.OPERARIO) {
            NavigationBarItem(
                selected = ruta == AppRoutes.RESUMEN,
                onClick = { onIr(AppRoutes.RESUMEN) },
                icon = { Icon(Icons.Filled.Assessment, contentDescription = null) },
                label = { Text("Resumen") }
            )
        }
    }
}

private fun irA(nav: NavHostController, destino: String) {
    nav.navigate(destino) {
        popUpTo(AppRoutes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
