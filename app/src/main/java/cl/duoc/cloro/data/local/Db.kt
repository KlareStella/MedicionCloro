package cl.duoc.cloro.data.local

import cl.duoc.cloro.model.AccionEntity
import cl.duoc.cloro.model.AlertaEntity
import cl.duoc.cloro.model.MedicionEntity
import cl.duoc.cloro.model.PuntoEntity
import android.content.Context
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPuntos(lista: List<PuntoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediciones(lista: List<MedicionEntity>)

    @Query("SELECT * FROM puntos ORDER BY granja, id")
    fun puntos(): Flow<List<PuntoEntity>>

    @Query("SELECT * FROM puntos WHERE id = :id")
    fun punto(id: Int): Flow<PuntoEntity?>

    @Query("SELECT * FROM puntos WHERE id = :id")
    suspend fun puntoOnce(id: Int): PuntoEntity?

    @Query(
        "SELECT m.* FROM mediciones m INNER JOIN " +
            "(SELECT puntoId, MAX(timestamp) AS ts FROM mediciones GROUP BY puntoId) u " +
            "ON m.puntoId = u.puntoId AND m.timestamp = u.ts"
    )
    fun ultimas(): Flow<List<MedicionEntity>>

    @Query("SELECT * FROM mediciones WHERE puntoId = :id ORDER BY timestamp DESC LIMIT :limite")
    fun historial(id: Int, limite: Int): Flow<List<MedicionEntity>>

    @Query("SELECT * FROM alertas ORDER BY activa DESC, timestamp DESC")
    fun alertas(): Flow<List<AlertaEntity>>

    @Query("SELECT * FROM alertas WHERE puntoId = :id AND activa = 1 LIMIT 1")
    suspend fun alertaActiva(id: Int): AlertaEntity?

    @Insert
    suspend fun insertAlerta(alerta: AlertaEntity): Long

    @Query("UPDATE alertas SET activa = 0 WHERE puntoId = :id AND activa = 1")
    suspend fun cerrarAlertas(id: Int)

    @Insert
    suspend fun insertAccion(accion: AccionEntity)

    @Query("SELECT * FROM acciones ORDER BY timestamp DESC")
    fun todasAcciones(): Flow<List<AccionEntity>>

    @Query("SELECT * FROM acciones WHERE id = :id")
    suspend fun accionOnce(id: Long): AccionEntity?

    @Update
    suspend fun actualizarAccion(accion: AccionEntity)

    @Delete
    suspend fun eliminarAccion(accion: AccionEntity)

    @Query("SELECT * FROM acciones WHERE puntoId = :id ORDER BY timestamp DESC")
    fun acciones(id: Int): Flow<List<AccionEntity>>

    @Query("SELECT * FROM acciones ORDER BY timestamp DESC LIMIT 20")
    fun ultimasAcciones(): Flow<List<AccionEntity>>
}

@Database(
    entities = [PuntoEntity::class, MedicionEntity::class, AlertaEntity::class, AccionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ClorDb : RoomDatabase() {
    abstract fun dao(): ClorDao

    companion object {
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE acciones ADD COLUMN fotoUri TEXT")
                db.execSQL("ALTER TABLE acciones ADD COLUMN latitud REAL")
                db.execSQL("ALTER TABLE acciones ADD COLUMN longitud REAL")
            }
        }

        @Volatile
        private var INSTANCE: ClorDb? = null
        fun getDatabase(ctx: Context): ClorDb =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(ctx.applicationContext, ClorDb::class.java, "cloro.db")
                    .addMigrations(MIGRACION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
