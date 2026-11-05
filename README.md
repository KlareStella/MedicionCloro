# Medición de Cloro en Línea · DSY1105 (Caso 2)

MVP Android (Kotlin + Jetpack Compose) para consultar niveles de cloro libre por granja y punto de medición.

## Cómo abrirlo
1. Android Studio (Koala o superior) → *Open* → seleccionar esta carpeta.
2. Esperar el *Gradle Sync* (requiere internet la primera vez).
3. Ejecutar en un emulador o celular Android 8.0+ (API 26+). Para probar el diseño adaptable, usar también un emulador *Tablet* o *Foldable*.

## Estructura (Guía 9)
```
cl.duoc.cloro
├── model/        Entidades, enums, DTOs (Models.kt, Dtos.kt)
├── data/local    Room (Db.kt)
├── data/remote   Retrofit + API simulada (Remote.kt)
├── repository/   ClorRepository, PreferenciasRepository (DataStore)
├── notif/        Notificaciones + WorkManager
└── ui/
    ├── theme/  components/  navigation/  screens/  viewmodel/
```

## Requisitos del caso
| Requisito | Implementación |
|---|---|
| Navegación | Navigation Compose + `AppRoutes`; Drawer lateral, barra inferior y TopAppBar |
| Gestión de estado | ViewModels con StateFlow (`MainViewModel`, `LoginViewModel`, `AccionViewModel`) |
| Persistencia local | Room/SQLite con CRUD de acciones (datos) + DataStore (sesión y preferencia "solo fuera de rango") |
| Consumo de API | Retrofit + OkHttp; `MockInterceptor` responde datos ficticios |
| Notificaciones | Notificación local al detectar valor fuera de rango; WorkManager cada 15 min |
| Conectividad intermitente | Siempre se muestra lo guardado en Room; banner animado "Sin conexión" |

## Dónde se aplica cada guía
- **Guía 9 – Diseño adaptable:** `MainActivity` calcula `WindowSizeClass`. Celular = lista + pantalla de detalle; tablet/plegable = lista y detalle lado a lado (`HomeScreen`); alertas en grilla adaptable (`AlertasScreen`); drawer permanente en pantallas expandidas.
- **Guía 10 – Navegación:** `ui/navigation/` (`AppRoutes`, `CloroApp`): NavHost, TopAppBar, NavigationBar, NavigationDrawer.
- **Guía 11 – Formularios:** `LoginScreen` (nombre, correo, clave con `PasswordVisualTransformation`, checkbox) y `AccionScreen`, con `UiState` + `Errores`, validación en tiempo real y `isError`/`supportingText`.
- **Guía 14 – CRUD con SQLite/Room:** tabla `acciones` (controles y acciones correctivas) con CRUD completo. Create: formulario desde el detalle de un punto. Read: *Bitácora de acciones* (Flow que actualiza la lista sola). Update: botón *Editar* (el mismo formulario en modo edición, conserva el id). Delete: botón *Eliminar* con confirmación. Flujo: Pantalla → `MainViewModel`/`AccionViewModel` → `ClorRepository` → `ClorDao` (`@Insert`, `@Query`, `@Update`, `@Delete`) → Room/SQLite. La base es un singleton (`ClorDb.getDatabase`). Permisos: el supervisor edita/elimina cualquier registro, el operario solo los suyos, la jefatura solo consulta.
- **Guía 12 – DataStore y animaciones:** `PreferenciasRepository`; `animateColorAsState`, `AnimatedVisibility` y `animateContentSize` en tarjetas, detalle y banner.

## Perfiles
- **Operario**: puntos y alertas; registra controles/acciones.
- **Supervisor**: lo anterior + resumen; puede marcar una desviación como resuelta.
- **Jefatura**: puntos, alertas, resumen e históricos (solo consulta).
La clave del formulario es solo de práctica (no se valida contra un servidor).

## Demo
- Nube tachada (barra superior de *Puntos*): simula pérdida de conexión.
- Actualizar: consulta la API simulada; los valores cambian cada 5 minutos.
- Puntos 2, 4 y 7 tienden a salir de rango (alertas y notificaciones).
- Rangos en `model/Models.kt` (`Rangos`): normal 0,30–1,50 ppm. Son referenciales; ajústalos si tu docente indica otros.

## Usar una API real
En `ClorApp.kt` quitar `.addInterceptor(MockInterceptor())` y cambiar `baseUrl` (endpoints `GET puntos` y `GET mediciones`).

## Cumplimiento de la rúbrica (IE)
| IE | Dónde se cumple |
|---|---|
| 2.1.1 Interfaz coherente y navegación | Material 3, Drawer + barra inferior + TopAppBar (`ui/navigation`), diseño adaptable a tablet |
| 2.1.2 Formularios con validación visual e íconos | `LoginScreen`, `AccionScreen` con `CampoValidado` (`ui/components/Campos.kt`): ícono inicial, ícono de error/válido, mensaje por campo, mostrar/ocultar clave |
| 2.2.1 Validación centralizada y desacoplada | `validation/Validadores.kt` (sin dependencias de Android ni de la UI) usado por `LoginViewModel` y `AccionViewModel`; pruebas en `src/test/.../ValidadoresTest.kt` |
| 2.2.2 Animaciones | Transiciones entre pantallas (`CloroApp`), colores animados por estado, banner y paneles con `AnimatedVisibility`, `animateContentSize` |
| 2.3.1 Modularidad y persistencia local | Paquetes `model / data / repository / ui / util / validation`; Room (v2 con migración) + DataStore |
| 2.3.2 Colaboración y control de versiones | Ver `GUIA_GIT.md` (el repositorio debe mostrar commits y ramas de cada integrante) |
| 2.4.1 Dos o más recursos del dispositivo | **Cámara** (foto de evidencia con FileProvider, sin permisos de almacenamiento) y **ubicación GPS** (permiso en tiempo de ejecución, manejo de rechazo), además de **notificaciones**. Se guardan en la acción (Room) y se ven en Detalle y Bitácora, con botón "Ver en mapa" |

Notas: la clave del login solo se valida por formato (mínimo 6 caracteres y un número). La foto y la ubicación son opcionales al registrar o editar una acción. Para ejecutar las pruebas unitarias: `./gradlew test`.
