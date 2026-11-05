@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package cl.duoc.cloro

import cl.duoc.cloro.ui.navigation.CloroApp
import cl.duoc.cloro.ui.theme.CloroTheme
import cl.duoc.cloro.ui.viewmodel.MainViewModel
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CloroTheme {
                val vm: MainViewModel = viewModel()
                // Window Size Classes (Guía 9): Compact / Medium / Expanded según el ancho disponible
                val tamano = calculateWindowSizeClass(this@MainActivity)
                PedirPermisoNotificaciones()
                CloroApp(vm, tamano.widthSizeClass)
            }
        }
    }
}

@Composable
private fun PedirPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT >= 33) {
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        LaunchedEffect(Unit) { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    }
}
