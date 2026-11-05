package cl.duoc.cloro.notif

import cl.duoc.cloro.ClorApp
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

class ChequeoCloroWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): ListenableWorker.Result {
        val repo = (applicationContext as ClorApp).repo
        repo.refrescar().onSuccess { nuevas ->
            Notificador.notificarNuevas(applicationContext, repo, nuevas)
        }
        return ListenableWorker.Result.success()
    }
}
