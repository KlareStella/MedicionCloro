# Guía rápida de Git para el trabajo grupal (IE 2.3.2)

La evaluación mira que **cada integrante** tenga commits propios, trabaje en ramas y se vea colaboración real.

## 1. Preparar el repositorio (una persona)
```bash
cd MedicionCloro
git init
git add .
git commit -m "chore: proyecto base Medición de Cloro"
git branch -M main
git remote add origin https://github.com/<usuario>/<repo>.git
git push -u origin main
```
En GitHub: *Settings > Collaborators* → agregar a todos los integrantes.

## 2. Cada integrante
```bash
git clone https://github.com/<usuario>/<repo>.git
git checkout -b feature/<nombre>-<tarea>     # ej: feature/ana-formulario-validaciones
# ... trabajar ...
git add .
git commit -m "feat: descripción corta de lo que hiciste"
git push -u origin feature/<nombre>-<tarea>
```
Luego abrir un **Pull Request** hacia `main` y pedir revisión a otro integrante.

## 3. Mantener la rama al día
```bash
git checkout main && git pull
git checkout feature/<nombre>-<tarea> && git merge main
```

## Buenas prácticas
- Commits pequeños y frecuentes, con prefijos: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`.
- Una rama por tarea; no trabajar directo en `main`.
- Usar *Issues* o un tablero (GitHub Projects / Trello) para repartir las tareas.
- No subir `local.properties` ni carpetas `build/` (ya están en `.gitignore`).

## Reparto de tareas sugerido (ajustar al tamaño del equipo)
| Área | Archivos principales |
|---|---|
| Interfaz y navegación | `ui/navigation`, `ui/screens/HomeScreen`, `AlertasScreen`, `ResumenScreen` |
| Formularios y validación | `ui/components/Campos.kt`, `validation/Validadores.kt`, `LoginScreen`, `AccionScreen` |
| Persistencia (Room / DataStore) | `data/local`, `repository`, `model` |
| API simulada y notificaciones | `data/remote`, `notif` |
| Recursos del dispositivo | `util/Evidencias.kt`, `util/Ubicacion.kt`, `ui/components/Evidencia.kt` |
| Pruebas y documentación | `src/test`, `README.md` |
