package com.mestizo.uute.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "flowmeters")
data class FlowmeterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val model: String,
    val dn: Int,
    val qMin: Double,
    val qNom: Double,
    val qMax: Double,
    val kvs: Double,
    val pulseWeight: String,
    val straightSections: String,
    val isCustom: Boolean = false
)

@Dao
interface FlowmeterDao {
    @Query("SELECT * FROM flowmeters ORDER BY model ASC, dn ASC")
    suspend fun getAllFlowmeters(): List<FlowmeterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(flowmeters: List<FlowmeterEntity>)
}

@Database(entities = [FlowmeterEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flowmeterDao(): FlowmeterDao
}
