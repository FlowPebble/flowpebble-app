package com.flowpebble.microguides.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MicroGuideDao {

    @Query("SELECT * FROM micro_guides WHERE isBonusVault = 0 ORDER BY isCustom DESC, createdAt ASC")
    fun observeStandardGuides(): Flow<List<MicroGuideEntity>>

    @Query("SELECT * FROM micro_guides WHERE isBonusVault = 1 ORDER BY createdAt ASC")
    fun observeBonusGuides(): Flow<List<MicroGuideEntity>>

    @Query("SELECT * FROM micro_guides WHERE isSaved = 1 ORDER BY createdAt DESC")
    fun observeSavedGuides(): Flow<List<MicroGuideEntity>>

    @Query("SELECT COUNT(*) FROM micro_guides WHERE isCompleted = 1")
    fun observeCompletedCount(): Flow<Int>

    @Query("SELECT * FROM micro_guides WHERE id = :guideId LIMIT 1")
    fun observeGuideById(guideId: String): Flow<MicroGuideEntity?>

    @Query("SELECT COUNT(*) FROM micro_guides")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(guides: List<MicroGuideEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuide(guide: MicroGuideEntity)

    @Update
    suspend fun updateGuide(guide: MicroGuideEntity)

    @Query("UPDATE micro_guides SET isSaved = :saved WHERE id = :guideId")
    suspend fun setSaved(guideId: String, saved: Boolean)

    @Query("UPDATE micro_guides SET isCompleted = :completed, checkedStepsMask = :mask WHERE id = :guideId")
    suspend fun setCompleted(guideId: String, completed: Boolean, mask: Int)

    @Query("DELETE FROM micro_guides WHERE id = :guideId AND isCustom = 1")
    suspend fun deleteCustomGuide(guideId: String)
}
