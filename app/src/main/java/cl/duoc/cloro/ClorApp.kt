package cl.duoc.cloro

import cl.duoc.cloro.data.local.ClorDb
import cl.duoc.cloro.data.remote.ClorApi
import cl.duoc.cloro.data.remote.MockInterceptor
import cl.duoc.cloro.notif.ChequeoCloroWorker
import cl.duoc.cloro.notif.Notificador
import cl.duoc.cloro.repository.ClorRepository
import cl.duoc.cloro.repository.PreferenciasRepository
import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ClorApp : Application() {

    lateinit var repo: ClorRepository
        private set

    lateinit var preferencias: PreferenciasRepository
        private set

    override fun onCreate() {
        super.onCreate()

        val cliente = OkHttpClient.Builder()
            .addInterceptor(MockInterceptor()) // quitar al conectar una API real
            .build()
        val api = Retrofit.Builder()
            .baseUrl("https://api.cloro.simulado/api/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClorApi::class.java)

        repo = ClorRepository(ClorDb.getDatabase(this).dao(), api)
        preferencias = PreferenciasRepository(applicationContext)

        Notificador.crearCanal(this)
        val trabajo = PeriodicWorkRequestBuilder<ChequeoCloroWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("chequeo_cloro", ExistingPeriodicWorkPolicy.KEEP, trabajo)
    }
}
