package cl.duoc.cloro.data.remote

import cl.duoc.cloro.model.MedicionDto
import cl.duoc.cloro.model.PuntoDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.http.GET
import java.io.IOException
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random


interface ClorApi {
    @GET("puntos")
    suspend fun puntos(): List<PuntoDto>

    @GET("mediciones")
    suspend fun mediciones(): List<MedicionDto>
}

object Simulador {
    @Volatile
    var sinConexion = false
}

class MockInterceptor : Interceptor {
    private val gson = Gson()
    private val json = "application/json".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (Simulador.sinConexion) throw IOException("Sin conexión (simulada)")
        Thread.sleep(500) // latencia simulada
        val request = chain.request()
        val (codigo, cuerpo) = when (request.url.encodedPath) {
            "/api/puntos" -> 200 to gson.toJson(MockData.puntos())
            "/api/mediciones" -> 200 to gson.toJson(MockData.mediciones(System.currentTimeMillis()))
            else -> 404 to "{}"
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(codigo)
            .message(if (codigo == 200) "OK" else "No encontrado")
            .body(cuerpo.toResponseBody(json))
            .build()
    }
}

object MockData {
    private val granjas = listOf("Granja Los Aromos", "Granja El Roble", "Granja Santa Marta")
    private val ubicaciones = listOf("Galpón 1", "Galpón 2", "Estanque principal")

    private val base = mapOf(
        1 to 0.9, 2 to 0.4, 3 to 1.1,
        4 to 0.28, 5 to 0.85, 6 to 1.0,
        7 to 1.9, 8 to 0.8, 9 to 1.2
    )

    fun puntos(): List<PuntoDto> = granjas.flatMapIndexed { gi, granja ->
        ubicaciones.mapIndexed { ui, ubicacion ->
            val id = gi * 3 + ui + 1
            PuntoDto(id, granja, ubicacion, "PM-%02d".format(id))
        }
    }

    fun mediciones(ahora: Long): List<MedicionDto> {
        val minuto = ahora / 60_000L * 60_000L
        return (1..9).flatMap { id ->
            (0 until 24).map { i ->
                val ts = minuto - i * 3_600_000L
                val rnd = Random(id * 7919L + ts / (5 * 60_000L))
                val ruido = (rnd.nextDouble() - 0.5) * 0.5
                val nivel = ((base.getValue(id) + ruido) * 100).roundToInt() / 100.0
                MedicionDto(id, max(0.0, nivel), ts)
            }
        }
    }
}
